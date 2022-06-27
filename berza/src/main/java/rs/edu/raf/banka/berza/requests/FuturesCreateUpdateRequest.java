package rs.edu.raf.banka.berza.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FuturesCreateUpdateRequest {

    private Long id;

    private String oznaka;
    private String opis;
    private String berzaOznaka;

    private Double contractSize;
    private String contractUnit;
    private Double maintenanceMargin;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date settlementDate;

}
