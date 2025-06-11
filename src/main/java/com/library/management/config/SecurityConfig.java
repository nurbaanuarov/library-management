package com.library.management.config;

import com.library.management.dao.UserDAO;
import com.library.management.dao.UserRoleDAO;
import com.library.management.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.List;

@Configuration
@EnableWebSecurity
@ComponentScan("com.library.management")
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDAO userDao;
    private final UserRoleDAO userRoleDAO;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/css/**"),
                                new AntPathRequestMatcher("/register")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/admin/**")).hasRole("ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/librarian/**")).hasRole("LIBRARIAN")
                        .requestMatchers(new AntPathRequestMatcher("/reader/**")).hasAnyRole("READER","LIBRARIAN","ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/do-login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .failureUrl("/login?error")
                        .permitAll()
                        .successHandler((req, res, auth) -> {
                            var auths = auth.getAuthorities();
                            if (auths.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                                res.sendRedirect(req.getContextPath() + "/admin");
                            } else if (auths.stream().anyMatch(a -> a.getAuthority().equals("ROLE_LIBRARIAN"))) {
                                res.sendRedirect(req.getContextPath() + "/librarian");
                            } else {
                                res.sendRedirect(req.getContextPath() + "/books");
                            }
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userDao.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            user.setRoles(userRoleDAO.findByUserId(user.getId()));

            List<SimpleGrantedAuthority> auths = user.getRoles().stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
                    .toList();

            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPasswordHash(),
                    user.isEnabled(), true, true, true,
                    auths
            );
        };
    }
}
