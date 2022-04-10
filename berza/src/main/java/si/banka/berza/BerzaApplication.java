package si.banka.berza;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class BerzaApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(BerzaApplication.class, args);
	}

}
