package si.banka.korisnicki_servis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    //TODO: Dodati permisije na otp urleove

    @PostMapping("/otp/generateSeecret")
    public ResponseEntity<String>saveUser() {
        var seecret = OTPUtilities.generateTOTPSecretKey();
        return ResponseEntity.ok().body(seecret);
    }

    @PostMapping("/otp/setSeecret")
    public ResponseEntity<?>setOtpSeecret(@RequestBody OtpToUserForm form) {
        userService.setUserOtp(form.getUsername(), form.getOtpSeecret());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/otp/clear")
    public ResponseEntity<?>clearOtp(@RequestBody String username) {
        userService.clearUserOtp(username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/otp/getQrcode")
    public ResponseEntity<String>getOtpToUser(@RequestBody OtpToUserForm form) {

        var otpUrl = OTPUtilities.getTOTPUrl(form.getOtpSeecret(), form.getUsername(), "Banka");
        var qr = OTPUtilities.createTOTPQRCodeBase64Png(otpUrl, 50 , 50);

        return ResponseEntity.ok().body(qr);
    }

}

