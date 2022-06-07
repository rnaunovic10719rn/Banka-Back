package rs.edu.raf.banka.racun.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentSredstvaKapitalDto {
    Double limit;
    Double limitUsed;
    Double raspolozivoAgentu;
}
