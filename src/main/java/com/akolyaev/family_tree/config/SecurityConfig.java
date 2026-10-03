package com.akolyaev.family_tree.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String adminUsername;
    private final String adminPassword;
    private final String adminRoles;
    private final String userUsername;
    private final String userPassword;
    private final String userRoles;

    public SecurityConfig(
            @Value("${admin.username}") String adminUsername,
            @Value("${admin.password}") String adminPassword,
            @Value("${admin.roles}") String adminRoles,
            @Value("${user.username}") String userUsername,
            @Value("${user.password}") String userPassword,
            @Value("${user.roles}") String userRoles) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminRoles = adminRoles;
        this.userUsername = userUsername;
        this.userPassword = userPassword;
        this.userRoles = userRoles;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        var admin = User.builder()
                .username(adminUsername)
                .password(encoder.encode(adminPassword))
                .roles(adminRoles.split(","))
                .build();
        var user = User.builder()
                .username(userUsername)
                .password(encoder.encode(userPassword))
                .roles(userRoles.split(","))
                .build();
        return new InMemoryUserDetailsManager(admin, user);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/tree", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/persons/**").permitAll()
                .requestMatchers("/login", "/register", "/", "/tree", "/persons/**").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
