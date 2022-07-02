package rs.edu.raf.banka.berza.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForexTimeseriesDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1604089805055806707L;

    String symbolFrom;
    String symbolTo;
    String time;
    String date;
    Double open;
    Double high;
    Double low;
    Double close;

}
