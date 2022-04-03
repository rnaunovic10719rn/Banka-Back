package si.banka.korisnicki_servis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import si.banka.korisnicki_servis.mail.PasswordResetToken;

public interface PasswordTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token);
}
