package si.banka.korisnicki_servis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.banka.korisnicki_servis.controller.response_forms.CreateUserForm;
import si.banka.korisnicki_servis.model.User;
import si.banka.korisnicki_servis.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<User>>getUsers() {
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @PostMapping("/user/create")
    public ResponseEntity<User>createUser(@RequestBody CreateUserForm createUserForm) {
        return ResponseEntity.ok().body(userService.createUser(createUserForm));
    }

    @DeleteMapping("/user/delete/{id}")
    public ResponseEntity<?>deleteUser(@PathVariable long id) {
        Optional<User> user= userService.getUserById(id);
        if(user.get() == null){ResponseEntity.badRequest().build();}
        userService.deleteUser(user.get());
        return ResponseEntity.ok().body(user.get().getUsername() + " disabled");
    }

    @PostMapping("/user/edit/{id}")
    public ResponseEntity<?>editUser(@PathVariable long id, @RequestHeader("Authorization") String token) {
        String username = userService.getUserById(id).get().getUsername();
        if(username.equalsIgnoreCase("admin")) return ResponseEntity.badRequest().build();

        userService.editUser(userService.getUser(username), token.substring("Bearer ".length()));
        return ResponseEntity.ok().body(username + " edited");
    }


}

