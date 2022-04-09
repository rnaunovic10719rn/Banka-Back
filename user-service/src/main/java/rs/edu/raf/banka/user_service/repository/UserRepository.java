package rs.edu.raf.banka.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.edu.raf.banka.user_service.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u from User u where u.email=:email")
    User findByEmail(String email);

    User findByUsername(String username);
}
