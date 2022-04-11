package rs.edu.raf.banka.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.edu.raf.banka.user_service.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u from User u where u.email=:email")
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
}
