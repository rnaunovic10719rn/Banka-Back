package rs.edu.raf.banka.berza.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AkcijeTimeseriesReadRequest {

    String type;
    String symbol;
    String interval;
    String timeFrom;
    String timeTo;

}
