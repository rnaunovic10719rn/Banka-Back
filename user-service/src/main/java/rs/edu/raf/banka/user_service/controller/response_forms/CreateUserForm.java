package rs.edu.raf.banka.user_service.controller.response_forms;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CreateUserForm {
    private String ime;
    private String prezime;
    private String email;
    private String jmbg;
    private String brTelefon;
    private String pozicija;
    private Double limit;
    private boolean needsSupervisorPermission;
}
