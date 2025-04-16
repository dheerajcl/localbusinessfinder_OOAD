// src/main/java/com/example/localbusinessfinder/config/SecurityConfig.java
package com.example.localbusinessfinder.config;

import com.example.localbusinessfinder.service.CustomUserDetailsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Collection;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests((authorize) ->
                    authorize
                            .requestMatchers("/", "/search", "/results", "/businesses/**").permitAll()
                            .requestMatchers("/register", "/register/**").permitAll()
                            .requestMatchers("/admin/register", "/admin/register/**").permitAll()
                            .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                            .requestMatchers("/error").permitAll()
                            .requestMatchers("/login", "/admin/login").permitAll()
                            .requestMatchers("/book/**", "/bookings/**", "/profile/**").hasRole("CUSTOMER")
                            .requestMatchers("/admin/**").hasRole("BUSINESS_ADMIN")
                            .anyRequest().authenticated()
            )
            .formLogin(
                    form -> form
                            .loginProcessingUrl("/login")
                            .successHandler(customAuthenticationSuccessHandler())
                            .permitAll()
            )
            .logout(
                    logout -> logout
                            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                            .logoutSuccessUrl("/login?logout")
                            .permitAll()
            )
            .exceptionHandling(exceptions ->
                    exceptions.authenticationEntryPoint(customAuthenticationEntryPoint())
            );

        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                  Authentication authentication) throws IOException, ServletException {
                String redirectUrl = "/";
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                for (GrantedAuthority grantedAuthority : authorities) {
                    if (grantedAuthority.getAuthority().equals("ROLE_BUSINESS_ADMIN")) {
                        redirectUrl = "/admin/dashboard";
                        break;
                    } else if (grantedAuthority.getAuthority().equals("ROLE_CUSTOMER")) {
                        redirectUrl = "/search";
                        break;
                    }
                }
                response.sendRedirect(redirectUrl);
            }
        };
    }
}