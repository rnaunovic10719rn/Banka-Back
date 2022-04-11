package rs.edu.raf.banka.berza.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AkcijeTimeseriesDto {

    String ticker;
    String time;
    String date;
    Double open;
    Double high;
    Double low;
    Double close;
    Long volume;

}
