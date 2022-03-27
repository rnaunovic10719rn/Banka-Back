package si.banka.korisnicki_servis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.banka.korisnicki_servis.controller.response_forms.OtpQRForm;
import si.banka.korisnicki_servis.controller.response_forms.OtpToUserForm;
import si.banka.korisnicki_servis.controller.response_forms.RoleToUserForm;
import si.banka.korisnicki_servis.model.Role;
import si.banka.korisnicki_servis.model.User;
import si.banka.korisnicki_servis.security.OTPUtilities;
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

    @PostMapping("/user/save")
    public ResponseEntity<User>saveUser(@RequestBody User user) {
        return ResponseEntity.ok().body(userService.saveUser(user));
    }

    @PostMapping("/role/save")
    public ResponseEntity<Role>saveUser(@RequestBody Role role) {
        return ResponseEntity.ok().body(userService.saveRole(role));
    }

    @PostMapping("/role/addtouser")
    public ResponseEntity<?>addRoleToUser(@RequestBody RoleToUserForm form) {
        userService.setRoleToUser(form.getUsername(), form.getRole_name());
        return ResponseEntity.ok().build();
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

    @PostMapping("/otp/setSeecret")
    public ResponseEntity<?>setOtpSeecret(@RequestBody OtpToUserForm form) {
        userService.setUserOtp(form.getUsername(), form.getSeecret());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/otp/clear")
    public ResponseEntity<?>clearOtp(@RequestBody String username) {
        userService.clearUserOtp(username);
        return ResponseEntity.ok().build();
    }
}

