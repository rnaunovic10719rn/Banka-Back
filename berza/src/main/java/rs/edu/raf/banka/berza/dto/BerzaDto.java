package rs.edu.raf.banka.berza.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BerzaDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 6815245786261176445L;

    String oznakaBerze;
    String kodValute;

}
