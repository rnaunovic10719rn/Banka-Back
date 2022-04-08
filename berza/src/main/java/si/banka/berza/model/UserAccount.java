package si.banka.berza.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAccount {

    @Id
    private Long id;

    @Column(unique=true)
    private Long idUser;

    private String username;
//    private Long wallet;  // Treci deo spec

    @OneToMany
    private List<Order> orders;
}
