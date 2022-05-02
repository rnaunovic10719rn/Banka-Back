package rs.edu.raf.banka.user_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import rs.edu.raf.banka.user_service.model.Permissions;
import rs.edu.raf.banka.user_service.model.Role;
import rs.edu.raf.banka.user_service.model.User;
import rs.edu.raf.banka.user_service.service.UserService;

import java.util.ArrayList;
import java.util.Collection;

@SpringBootApplication()
@EnableDiscoveryClient
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
		//dummy check
		if (!userService.getUserById(1).isPresent()) {
			return args -> {
				//Pravim permisije za role
				Collection<String> adminPermissions = new ArrayList<>();
				adminPermissions.add(String.valueOf(Permissions.CREATE_USER));
				adminPermissions.add(String.valueOf(Permissions.DELETE_USER));
				adminPermissions.add(String.valueOf(Permissions.LIST_USERS));
				adminPermissions.add(String.valueOf(Permissions.EDIT_USER));
				adminPermissions.add(String.valueOf(Permissions.MY_EDIT));

				Collection<String> dummyPermissions = new ArrayList<>();
				dummyPermissions.add(String.valueOf(Permissions.MANAGE_STUFF));

				//Pravimo Role
				Role glAdminRole = new Role(null, "ROLE_GL_ADMIN", adminPermissions);
				Role adminRole = new Role(null, "ROLE_ADMIN", adminPermissions);

				//Stavljamo Role u bazu
				userService.saveRole(glAdminRole);
				userService.saveRole(adminRole);

				//Cuvamo glavnog admina
				userService.createUserAdmin(new User("admin", "Petar", "Petrovic", "email@email.com", "1111111111111", "060123456","Admin123", null, true, false, glAdminRole));

				//testni user za otp, obrisace se..?
				userService.createUserAdmin(new User("test", "1234", "5MYDN5OMDRTEVQPCED4F5VYKZRPZ4FRY"));
				userService.setRoleToUser("test", "ROLE_ADMIN");
			};
		}
		return null;
	}

}
