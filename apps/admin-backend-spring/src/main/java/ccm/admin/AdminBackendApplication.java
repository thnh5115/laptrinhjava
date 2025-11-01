package ccm.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// AUD-002: Enable scheduling for audit log cleanup job
@EnableScheduling
@SpringBootApplication
public class AdminBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminBackendApplication.class, args);
	}

}

