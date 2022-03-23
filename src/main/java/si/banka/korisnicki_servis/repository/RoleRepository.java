package si.banka.korisnicki_servis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import si.banka.korisnicki_servis.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByName(String name);
}
