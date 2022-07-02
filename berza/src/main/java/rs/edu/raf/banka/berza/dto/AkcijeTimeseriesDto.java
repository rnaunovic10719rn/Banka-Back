package rs.edu.raf.banka.berza.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AkcijeTimeseriesDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -8817517538349426365L;

    String ticker;
    String time;
    String date;
    Double open;
    Double high;
    Double low;
    Double close;
    Long volume;

}
