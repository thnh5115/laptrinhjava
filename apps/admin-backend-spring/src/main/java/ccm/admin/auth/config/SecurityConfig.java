package ccm.admin.auth.config;

import ccm.admin.auth.security.CustomAccessDeniedHandler;
import ccm.admin.auth.security.JwtAuthenticationEntryPoint;
import ccm.admin.auth.security.JwtAuthenticationFilter;
import ccm.admin.auth.security.CustomUserDetailsService;
import ccm.admin.security.RateLimitFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Enhanced Security Configuration with Hardening
 * - JWT Authentication & Authorization
 * - Role-Based Access Control (RBAC)
 * - HTTP Security Headers (HSTS, XSS Protection, Frame Options, CSP, Referrer-Policy, Permissions-Policy)
 * - Custom Exception Handlers
 * - Stateless Session Management
 * - CORS Configuration (configurable via environment variable)
 * - OpenAPI/Swagger Documentation Access
 * 
 * PR-7: FE Integration Blockers - All Security Enhancements
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${app.frontend.origins:http://localhost:3000}")
    private String frontendOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Provider DAO dùng CustomUserDetailsService từ DB */
    @Bean
    @SuppressWarnings("deprecation")
    public AuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService, 
            PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(encoder);
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    /** 
     * CORS Configuration - Configurable via environment variable
     * PR-7: Fix BLOCKER #2 - CORS origin configurable via FRONTEND_ORIGIN env variable
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // Support comma-separated origins for multi-env (local, Docker, etc.)
        java.util.List<String> origins = java.util.Arrays.stream(frontendOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        cfg.setAllowedOrigins(origins);
        cfg.setAllowedMethods(java.util.List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        cfg.setAllowedHeaders(java.util.List.of("Authorization","Content-Type","Accept"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L); // cache preflight 1 giờ

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    /** 
     * Enhanced Security Filter Chain with Hardening
     * - Stateless session (JWT-based)
     * - Role-based access control
     * - Custom exception handlers
     * - HTTP security headers (CSP, Referrer-Policy, Permissions-Policy)
     * - CORS enabled
     * - Rate limiting on authentication endpoints (PR-6: SEC-002)
     * - OpenAPI/Swagger documentation access (PR-7)
     * - Public /api/ping endpoint (PR-7)
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            AuthenticationProvider authProvider,
            JwtAuthenticationFilter jwtFilter,
            JwtAuthenticationEntryPoint jwtAuthEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler,
            RateLimitFilter rateLimitFilter
    ) throws Exception {

        http
            // Enable CORS
            .cors(Customizer.withDefaults())
            
            // Disable CSRF (not needed for stateless JWT)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Stateless session management
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/actuator/health",
                    "/actuator/info",
                    "/api/auth/**",
                    "/auth/**",
                    "/api/test/**",
                    "/api/ping",              // PR-7: Public ping endpoint
                    "/error",
                    // OpenAPI/Swagger endpoints (PR-7: BLOCKER #1)
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                
                // Allow OPTIONS for CORS preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Admin endpoints - require ADMIN role
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Actuator endpoints - require ADMIN role (except health/info above)
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Exception handling
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthEntryPoint)  // Handle 401
                .accessDeniedHandler(accessDeniedHandler)      // Handle 403
            )
            
            // HTTP Security Headers (PR-7: BLOCKER #5)
            .headers(headers -> headers
                // HTTP Strict Transport Security (HSTS)
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)  // 1 year
                )
                // XSS Protection
                .xssProtection(Customizer.withDefaults())
                // Content Type Options (prevent MIME sniffing)
                .contentTypeOptions(Customizer.withDefaults())
                // Frame Options (prevent clickjacking)
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                // Content Security Policy (PR-7: BLOCKER #5)
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline'; " +  // unsafe-inline needed for Swagger UI
                        "style-src 'self' 'unsafe-inline'; " +    // unsafe-inline needed for Swagger UI
                        "img-src 'self' data:; " +
                        "font-src 'self'; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'none'"
                    )
                )
                // Referrer Policy (PR-7: BLOCKER #5)
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
                // Permissions Policy (PR-7: BLOCKER #5)
                .permissionsPolicy(permissions -> permissions
                    .policy("geolocation=(), microphone=(), camera=()")
                )
            )
            
            // Authentication provider
            .authenticationProvider(authProvider)
            
            // Rate limiting filter (PR-6: SEC-002) - BEFORE authentication
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            
            // JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
