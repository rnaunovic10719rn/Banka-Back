package si.banka.korisnicki_servis;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import si.banka.korisnicki_servis.model.Permissions;
import si.banka.korisnicki_servis.model.Role;
import si.banka.korisnicki_servis.model.User;
import si.banka.korisnicki_servis.service.UserService;

import java.util.ArrayList;
import java.util.Collection;

@SpringBootApplication()
public class BankaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankaApplication.class, args);
	}

	@Bean
	PasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}

	@Bean
	CommandLineRunner run(UserService userService){
		return args -> {
			//Punimo Rolu permisijama
			Collection<String> admin_permissions = new ArrayList<>();
			admin_permissions.add(String.valueOf(Permissions.LIST_USERS));

			Collection<String> manager_permissions = new ArrayList<>();
			manager_permissions.add(String.valueOf(Permissions.MANAGE_STUFF));

			//Punimo bazu Rolama
			userService.saveRole(new Role(null, "ROLE_ADMIN", admin_permissions));
			userService.saveRole(new Role(null, "ROLE_MANAGER", manager_permissions));

			//Punimo bazu userima
			userService.saveUser(new User(null, "John Travolta", "john", "1234", null, null, false));
			userService.saveUser(new User(null, "Will Boyden", "will", "1234", null, null, false));
			userService.saveUser(new User(null, "Jim Team", "jim", "1234", null, null, false));
			userService.saveUser(new User(null, "Arnold Marakesh", "arnold", "1234", null, null, false));

			//Setujemo Role userima
			userService.setRoleToUser("will", "ROLE_MANAGER");
			userService.setRoleToUser("arnold", "ROLE_ADMIN");
		};
	}

}
