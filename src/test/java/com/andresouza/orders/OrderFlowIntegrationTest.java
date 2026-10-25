package com.andresouza.orders;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste ponta a ponta: sobe a aplicação inteira contra um PostgreSQL real (Testcontainers),
 * aplica as migrations do Flyway e exercita o fluxo pela API HTTP.
 */
@SpringBootTest(properties = {"app.admin.email=admin@test.com", "app.admin.password=admin-password"})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class OrderFlowIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void fullOrderLifecycle() throws Exception {
        String admin = login("admin@test.com", "admin-password");
        String customer = registerAndLogin();

        String productJson = """
                {"name": "Cadeira Gamer", "description": "Reclinável", "price": 1200.00, "stockQuantity": 3}
                """;
        String created = mvc.perform(post("/api/products").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(productJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer productId = JsonPath.read(created, "$.id");

        String order = mvc.perform(post("/api/orders").header("Authorization", customer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\": [{\"productId\": %d, \"quantity\": 2}]}".formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.total").value(2400.00))
                .andReturn().getResponse().getContentAsString();
        Integer orderId = JsonPath.read(order, "$.id");

        mvc.perform(get("/api/products/{id}", productId))
                .andExpect(jsonPath("$.stockQuantity").value(1));

        mvc.perform(patch("/api/orders/{id}/status", orderId).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"PAID\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mvc.perform(post("/api/orders/{id}/cancel", orderId).header("Authorization", customer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mvc.perform(get("/api/products/{id}", productId))
                .andExpect(jsonPath("$.stockQuantity").value(3));

        mvc.perform(get("/api/orders/me").header("Authorization", customer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void rejectsOrderAboveAvailableStock() throws Exception {
        String customer = registerAndLogin();

        mvc.perform(post("/api/orders").header("Authorization", customer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\": [{\"productId\": 1, \"quantity\": 999}]}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Regra de negócio violada"));
    }

    @Test
    void enforcesAuthenticationAndRoles() throws Exception {
        mvc.perform(get("/api/orders/me")).andExpect(status().isUnauthorized());

        String customer = registerAndLogin();
        mvc.perform(post("/api/products").header("Authorization", customer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"X\", \"price\": 10, \"stockQuantity\": 1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void validatesRequestBody() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\", \"email\": \"invalido\", \"password\": \"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    private String registerAndLogin() throws Exception {
        String email = "cliente-" + UUID.randomUUID() + "@test.com";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Cliente\", \"email\": \"%s\", \"password\": \"senha-segura\"}".formatted(email)))
                .andExpect(status().isCreated());
        return login(email, "senha-segura");
    }

    private String login(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"%s\", \"password\": \"%s\"}".formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.accessToken");
    }
}
