package ccm.admin.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@Slf4j
/** config - Configuration - Redis cache configuration */

public class CacheConfig {

    
    @Bean
    public CacheManager cacheManager() {
        log.info("Configuring Caffeine cache manager for reports and analytics");
        
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "reports:summary",      
                "reports:monthly",      
                "analytics:kpis",       
                "analytics:trends",     
                "analytics:disputes"    
        );
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)  
                .maximumSize(1000)                        
                .recordStats());                          
        
        log.info("Cache manager configured: TTL=10min, MaxSize=1000 entries");
        return cacheManager;
    }
}
