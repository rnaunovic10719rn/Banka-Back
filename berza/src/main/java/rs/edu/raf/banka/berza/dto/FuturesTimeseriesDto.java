package rs.edu.raf.banka.berza.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuturesTimeseriesDto {

    String symbol;
    String time;
    String date;
    Double open;
    Double high;
    Double low;
    Double settle;
    Long volumeDouble;
    Long previousDouble;
    Long volume;
    Long previous;

}
