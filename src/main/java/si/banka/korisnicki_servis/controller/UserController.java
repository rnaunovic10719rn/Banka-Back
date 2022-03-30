package si.banka.korisnicki_servis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.banka.korisnicki_servis.controller.response_forms.CreateUserForm;
import si.banka.korisnicki_servis.controller.response_forms.OtpQRForm;
import si.banka.korisnicki_servis.controller.response_forms.OtpToUserForm;
import si.banka.korisnicki_servis.controller.response_forms.RoleToUserForm;
import si.banka.korisnicki_servis.model.Role;
import si.banka.korisnicki_servis.controller.response_forms.CreateUserForm;
import si.banka.korisnicki_servis.model.User;
import si.banka.korisnicki_servis.security.OTPUtilities;
import si.banka.korisnicki_servis.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Api(value = "UserControllerAPI", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;

    @GetMapping("/users")
    @ApiOperation("Gets all users")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = User.class)})
    public ResponseEntity<List<User>>getUsers() {
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @PostMapping("/user/create")
    @ApiOperation("Create user with username, ime, prezime, email, jmbg, br_telefona, password, aktivan, pozicija")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = User.class)})
    public ResponseEntity<User>createUser(@RequestBody CreateUserForm createUserForm) {
        return ResponseEntity.ok().body(userService.createUser(createUserForm));
    }

    @DeleteMapping("/user/delete/{id}")
    @ApiOperation("Delete user with specific id")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = User.class)})
    public ResponseEntity<?>deleteUser(@PathVariable long id) {
        Optional<User> user= userService.getUserById(id);
        if(user.get() == null){ResponseEntity.badRequest().build();}
        userService.deleteUser(user.get());
        return ResponseEntity.ok().body(user.get().getUsername() + " disabled");
    }

    @PostMapping("/user/edit/{id}")
    @ApiOperation("Edit user with specific id,text fields are with existing data, the user can change them")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = User.class)})
    public ResponseEntity<?>editUser(@PathVariable long id, @RequestHeader("Authorization") String token) {
        String username = userService.getUserById(id).get().getUsername();
        if(username.equalsIgnoreCase("admin")) return ResponseEntity.badRequest().build();

        userService.editUser(userService.getUser(username), token.substring("Bearer ".length()));
        return ResponseEntity.ok().body(username + " edited");
    }

    @GetMapping("/otp/generateSeecret")
    public ResponseEntity<String>generateSeecret() {
        var seecret = OTPUtilities.generateTOTPSecretKey();
        return ResponseEntity.ok().body(seecret);
    }

    @PostMapping("/otp/generateQrImage")
    public ResponseEntity<String>generateOtpQrImage(@RequestBody OtpQRForm form) {

        var qr = OTPUtilities.createTOTPQRCodeBase64Png(form.getSeecret(), form.getLabel(), "Banka");
        return ResponseEntity.ok().body(qr);
    }

    @PostMapping("/otp/generateQrUri")
    public ResponseEntity<String>generateOtpQrUri(@RequestBody OtpQRForm form) {

        var qr = OTPUtilities.createTOTPQrUri(form.getSeecret(), form.getLabel(), "Banka");
        return ResponseEntity.ok().body(qr);
    }

    //TODO: Dodati permisije na otp urleove

    @PostMapping("/otp/set/{id}")
    public ResponseEntity<?>setOtpSeecret(@PathVariable long id, @RequestBody OtpToUserForm form) {
        userService.setUserOtp(form.getUsername(), form.getSeecret());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/otp/clear")
    public ResponseEntity<?>clearOtp(@RequestBody String username) {
        userService.clearUserOtp(username);
        return ResponseEntity.ok().build();
    }

}

