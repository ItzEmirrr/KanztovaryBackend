package kg.kanztovary.kanztovarybackend.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Публичные эндпоинты
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        // Просмотр товаров и категорий — для всех
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/brands/**").permitAll()
                        // Чтение отзывов и статистика — для всех
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/stats").permitAll()
                        // Управление товарами и категориями — только ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/brands/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/brands/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/brands/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/files/**").hasRole("ADMIN")
                        // KPI и прочие admin-only эндпоинты
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // Удаление заказов — только ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/orders/**").hasRole("ADMIN")
                        // Всё остальное — только авторизованные
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
