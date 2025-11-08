package ccm.cva.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final SecurityProperties properties;

    public SecurityConfig(SecurityProperties properties) {
        this.properties = properties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.security", name = "mode", havingValue = "BASIC", matchIfMissing = true)
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        SecurityProperties.DevUser devUser = properties.getDevUser();

        // Dev friendly in-memory user; replace with shared auth provider when integrated
        return new InMemoryUserDetailsManager(
            User.builder()
                .username(devUser.getUsername())
                .password(encoder.encode(devUser.getPassword()))
                .roles("CVA_OFFICER")
                .build()
        );
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> allowedOrigins = properties.getAllowedOrigins();
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Idempotency-Key", "Idempotency-Key", "X-Correlation-Id"));
        configuration.setExposedHeaders(List.of("X-Correlation-Id"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectProvider<JwtDecoder> jwtDecoderProvider) throws Exception {
        SecurityProperties.Mode mode = properties.getMode();

        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/actuator/health").permitAll();
                if (mode == SecurityProperties.Mode.BASIC) {
                    auth.requestMatchers("/actuator/info").permitAll();
                } else {
                    auth.requestMatchers("/actuator/**").hasRole("CVA_OFFICER");
                }
                auth.requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll();
                auth.requestMatchers("/api/cva/**").hasRole("CVA_OFFICER");
                auth.anyRequest().authenticated();
            });

        if (mode == SecurityProperties.Mode.BASIC) {
            http.httpBasic(Customizer.withDefaults());
        } else {
            http.httpBasic(httpBasic -> httpBasic.disable());
            JwtDecoder decoder = jwtDecoderProvider.getIfAvailable();
            Assert.notNull(decoder, "JWT mode requires a JwtDecoder bean");
            // Spring Security 6 expects decoder(...) instead of the older jwtDecoder(...) API
            http.oauth2ResourceServer(
                oauth2 -> oauth2.jwt(jwt -> jwt.decoder(decoder).jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );
        }

        return http.build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.security", name = "mode", havingValue = "JWT")
    public JwtDecoder jwtDecoder() {
        SecurityProperties.Jwt jwt = properties.getJwt();
        Assert.hasText(jwt.getJwkSetUri(), "app.security.jwt.jwk-set-uri must be provided when JWT mode is enabled");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwt.getJwkSetUri()).build();

        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> configuredValidator = defaultValidator;

        if (StringUtils.hasText(jwt.getAudience())) {
            OAuth2TokenValidator<Jwt> audienceValidator = token -> {
                Object audClaim = token.getClaims().get("aud");
                if (audClaim instanceof String aud && jwt.getAudience().equals(aud)) {
                    return OAuth2TokenValidatorResult.success();
                }
                if (audClaim instanceof List<?> audList && audList.contains(jwt.getAudience())) {
                    return OAuth2TokenValidatorResult.success();
                }
                return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Token audience mismatch", null));
            };
            configuredValidator = new DelegatingOAuth2TokenValidator<>(configuredValidator, audienceValidator);
        }

        decoder.setJwtValidator(configuredValidator);
        return decoder;
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter delegate = new JwtGrantedAuthoritiesConverter();
        delegate.setAuthorityPrefix("ROLE_");
        delegate.setAuthoritiesClaimName(properties.getJwt().getRolesClaim());

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<String> roles = new HashSet<>();

            Object directRoles = jwt.getClaim(properties.getJwt().getRolesClaim());
            extractRoles(directRoles, roles);

            Object realmAccess = jwt.getClaims().get("realm_access");
            if (realmAccess instanceof Map<?, ?> realmMap) {
                extractRoles(realmMap.get("roles"), roles);
            }

            Object resourceAccess = jwt.getClaims().get("resource_access");
            if (resourceAccess instanceof Map<?, ?> resourceMap) {
                resourceMap.values().forEach(value -> {
                    if (value instanceof Map<?, ?> appMap) {
                        extractRoles(appMap.get("roles"), roles);
                    }
                });
            }

            if (roles.isEmpty()) {
                Collection<GrantedAuthority> authorities = delegate.convert(jwt);
                return authorities != null ? authorities : Set.of();
            }

            return roles.stream()
                .filter(StringUtils::hasText)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(String::toUpperCase)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        });
        return converter;
    }

    private void extractRoles(Object claim, Set<String> target) {
        if (claim instanceof String role) {
            target.add(role);
        } else if (claim instanceof List<?> list) {
            list.stream().filter(String.class::isInstance).map(String.class::cast).forEach(target::add);
        }
    }
}
