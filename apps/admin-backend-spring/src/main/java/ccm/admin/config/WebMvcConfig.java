package ccm.admin.config;
import ccm.admin.audit.web.AuditInterceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuditInterceptor auditInterceptor;

    @Autowired
    public WebMvcConfig(AuditInterceptor auditInterceptor) {
        this.auditInterceptor = auditInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditInterceptor)
                .addPathPatterns("/api/**")     // chỉ áp dụng cho các endpoint API
                .excludePathPatterns(
                        "/api/auth/**",          // bỏ qua login/signup
                        "/actuator/**"           // bỏ qua healthcheck
                );
    }
}
