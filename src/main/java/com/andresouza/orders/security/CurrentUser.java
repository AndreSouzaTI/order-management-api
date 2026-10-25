package com.andresouza.orders.security;

import org.springframework.security.oauth2.jwt.Jwt;

/** Dados do usuário autenticado extraídos do token. */
public record CurrentUser(Long id, boolean admin) {

    public static CurrentUser from(Jwt jwt) {
        boolean admin = jwt.getClaimAsStringList("roles").contains("ADMIN");
        return new CurrentUser(Long.valueOf(jwt.getSubject()), admin);
    }
}
