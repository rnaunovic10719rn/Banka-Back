package si.banka.korisnicki_servis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.banka.korisnicki_servis.controller.response_forms.CreateUserForm;
import si.banka.korisnicki_servis.model.User;
import si.banka.korisnicki_servis.service.UserService;

import java.util.List;

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
        String username = createUserForm.getIme() + createUserForm.getPrezime();
        String password = username + "123";
        return ResponseEntity.ok()
                .body(userService.createUser(new User(null, username, createUserForm.getIme(),
                        createUserForm.getPrezime(), createUserForm.getEmail(),
                        createUserForm.getJmbg(), createUserForm.getBr_telefon(),
                        password, userService.getRole(createUserForm.getPozicija()))));
    }

    @DeleteMapping("/user/delete/{username}")
    public ResponseEntity<?>deleteUser(@PathVariable String username) {
        userService.deleteUser(userService.getUser(username));
        return ResponseEntity.ok().body(username + " deleted");
    }

}

