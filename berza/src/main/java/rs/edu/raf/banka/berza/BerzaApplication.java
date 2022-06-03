package rs.edu.raf.banka.berza;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BerzaApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(BerzaApplication.class, args);
	}

}
