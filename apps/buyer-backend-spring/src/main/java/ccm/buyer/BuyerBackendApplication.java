package ccm.buyer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "ccm.buyer",  
    "ccm.common"  
})
public class BuyerBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BuyerBackendApplication.class, args);
    }
}
