package rs.edu.raf.banka.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.edu.raf.banka.user_service.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
}
