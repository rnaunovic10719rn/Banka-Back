package si.banka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import si.banka.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
}
