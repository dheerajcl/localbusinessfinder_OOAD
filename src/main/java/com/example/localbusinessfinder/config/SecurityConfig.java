// src/main/java/com/example/localbusinessfinder/config/SecurityConfig.java
package com.example.localbusinessfinder.config;

import com.example.localbusinessfinder.service.CustomerUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomerUserDetailsService userDetailsService;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity (enable in production with proper handling)
                .authorizeHttpRequests((authorize) ->
                        authorize.requestMatchers("/register/**", "/login", "/", "/search", "/results", "/businesses/**", "/css/**", "/js/**", "/images/**").permitAll()
                                .requestMatchers("/book/**", "/bookings/**", "/rate/**", "/pay/**", "/cancel/**").authenticated() // Require login for booking/managing
                                .anyRequest().authenticated() // All other requests need authentication (adjust as needed)
                ).formLogin(
                        form -> form
                                .loginPage("/login")
                                .loginProcessingUrl("/login") // Spring Security handles this POST URL
                                .defaultSuccessUrl("/search", true) // Redirect after successful login
                                .permitAll()
                ).logout(
                        logout -> logout
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .logoutSuccessUrl("/login?logout") // Redirect after logout
                                .permitAll()
                );
        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }
}