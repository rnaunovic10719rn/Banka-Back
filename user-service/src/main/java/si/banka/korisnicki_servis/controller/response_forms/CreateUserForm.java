package si.banka.korisnicki_servis.controller.response_forms;

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
    private String br_telefon;
    private String pozicija;
}
