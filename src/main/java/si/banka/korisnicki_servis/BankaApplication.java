package si.banka.korisnicki_servis;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
	BCryptPasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}

	@Bean
	CommandLineRunner run(UserService userService){
		return args -> {
			//Punimo Role permisijama
			Collection<String> admin_permissions = new ArrayList<>();
			admin_permissions.add(String.valueOf(Permissions.CREATE_USER));
			admin_permissions.add(String.valueOf(Permissions.DELETE_USER));
			admin_permissions.add(String.valueOf(Permissions.LIST_USERS));
			admin_permissions.add(String.valueOf(Permissions.EDIT_USER));
			admin_permissions.add(String.valueOf(Permissions.MY_EDIT));

			Collection<String> neka_pozicija_permissions = new ArrayList<>();
			neka_pozicija_permissions.add(String.valueOf(Permissions.MANAGE_STUFF));

			//Punimo bazu Rolama
			userService.saveRole(new Role(null, "ROLE_GL_ADMIN", admin_permissions));
			userService.saveRole(new Role(null, "ROLE_ADMIN", admin_permissions));

			//Cuvamo glavnog admina
			userService.createUserAdmin(new User("admin", "Admin123"));

			//Setujemo Rolu adminu
			userService.setRoleToUser("admin", "ROLE_GL_ADMIN");
			//userService.setRoleToUser("arnold", "ROLE_ADMIN");
		};
	}

}
