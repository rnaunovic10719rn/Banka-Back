package si.banka.korisnicki_servis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import si.banka.korisnicki_servis.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
}
