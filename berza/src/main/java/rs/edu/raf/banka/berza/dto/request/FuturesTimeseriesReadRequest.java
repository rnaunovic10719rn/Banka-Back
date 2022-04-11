package rs.edu.raf.banka.berza.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuturesTimeseriesReadRequest {

    String symbol;
    String timeFrom;
    String timeTo;

}
