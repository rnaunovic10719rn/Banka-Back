package rs.edu.raf.banka.berza.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuturesPodaciDto {

    private String symbol;
    private String time;
    private String date;
    private Double open;
    private Double high;
    private Double low;
    private Double settle;
    private Integer volumeDouble;
    private Integer previousDouble;
    private Integer volume;
    private Integer previous;

}
