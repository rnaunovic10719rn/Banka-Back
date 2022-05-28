package rs.edu.raf.banka.user_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import rs.edu.raf.banka.user_service.model.Permissions;
import rs.edu.raf.banka.user_service.model.Role;
import rs.edu.raf.banka.user_service.model.User;
import rs.edu.raf.banka.user_service.service.UserService;

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

				Collection<String> supervisorPermissions = new ArrayList<>();
				adminPermissions.add(String.valueOf(Permissions.MANAGE_AGENTS));

				Collection<String> agentPermissions = new ArrayList<>();

				//Pravimo Role
				Role glAdminRole = new Role(null, "ROLE_GL_ADMIN", adminPermissions);
				Role adminRole = new Role(null, "ROLE_ADMIN", adminPermissions);
				Role supervisorRole = new Role(null, "ROLE_SUPERVISOR", supervisorPermissions);
				Role agentRole = new Role(null, "ROLE_AGENT", agentPermissions);


				//Stavljamo Role u bazu
				userService.saveRole(glAdminRole);
				userService.saveRole(adminRole);
				userService.saveRole(supervisorRole);
				userService.saveRole(agentRole);

				//Cuvamo glavnog admina
				userService.createUserAdmin(new User("admin", "Petar", "Petrovic", "email@email.com", "1111111111111", "060123456","Admin123", null, true, false, glAdminRole));

				//testni user za otp, obrisace se..?
				userService.createUserAdmin(new User("test", "1234", "5MYDN5OMDRTEVQPCED4F5VYKZRPZ4FRY"));
				userService.setRoleToUser("test", "ROLE_ADMIN");

				String hashPasswordAgent1 = BCrypt.hashpw("agent1agent1", BCrypt.gensalt());
				String hashPasswordAgent2 = BCrypt.hashpw("agent2agent2", BCrypt.gensalt());
				String hashPasswordSupervisor1 = BCrypt.hashpw("supervisor1", BCrypt.gensalt());
				String hashPasswordSupervisor2 = BCrypt.hashpw("supervisor2", BCrypt.gensalt());

				User agent1 = new User(
						"jovan.jovanovic",
						"Jovan",
						"Jovanovic",
						"jovan.jovanovic@gmail.com",
						"1203970250062",
						"+38165856952",
						hashPasswordAgent1,
						null,
						true,
						agentRole,
						false,
						1000.00,
						false
				);

				User agent2 = new User(
						"milica.mihajlovic",
						"Milica",
						"Mihajlovic",
						"milica.mihajlovic@gmail.com",
						"1502980855066",
						"+381647825632",
						hashPasswordAgent2,
						null,
						true,
						agentRole,
						false,
						1500.00,
						true
				);

				User supervisor1 = new User(
						"vladimir.vladimirovic",
						"Vladimir",
						"Vladimirovic",
						"vladimir.vladimirovic@gmail.com",
						"2502954220014",
						"+381689526358",
						hashPasswordSupervisor1,
						null,
						true,
						false,
						supervisorRole

				);

				User supervisor2 = new User(
						"marija.marijanovic",
						"Marija",
						"Marijanovic",
						"marija.marijanovic@gmail.com",
						"2205987255021",
						"+381675289856",
						hashPasswordSupervisor2,
						null,
						true,
						false,
						supervisorRole
				);

				userService.saveUser(agent1);
				userService.saveUser(agent2);
				userService.saveUser(supervisor1);
				userService.saveUser(supervisor2);
			};


		}
		return null;
	}

}
