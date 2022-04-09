package rs.edu.raf.banka.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.edu.raf.banka.user_service.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByName(String name);
}
