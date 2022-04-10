package rs.edu.raf.banka.berza.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForexTimeseriesDto {

    String symbolFrom;
    String symbolTo;
    String time;
    String date;
    Double open;
    Double high;
    Double low;
    Double close;

}
