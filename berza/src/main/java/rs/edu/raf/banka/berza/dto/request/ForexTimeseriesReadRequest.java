package rs.edu.raf.banka.berza.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForexTimeseriesReadRequest {

    String type;
    String symbolTo;
    String symbolFrom;
    String interval;
    String timeFrom;
    String timeTo;

}
