package com.andresouza.orders.config;

import com.andresouza.orders.domain.AppUser;
import com.andresouza.orders.domain.Role;
import com.andresouza.orders.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Cria o usuário administrador na primeira subida, a partir de variáveis de ambiente.
 * Nenhuma senha fica versionada no código ou nas migrations.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminBootstrap(UserRepository users, PasswordEncoder passwordEncoder,
                          @Value("${app.admin.email:}") String email,
                          @Value("${app.admin.password:}") String password) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password) || users.existsByEmailIgnoreCase(email)) {
            return;
        }
        users.save(new AppUser("Administrador", email, passwordEncoder.encode(password), Role.ADMIN));
        log.info("Usuário administrador {} criado", email);
    }
}
