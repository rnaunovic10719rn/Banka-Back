package rs.edu.raf.banka.racun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RacunApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(RacunApplication.class, args);
	}

}
