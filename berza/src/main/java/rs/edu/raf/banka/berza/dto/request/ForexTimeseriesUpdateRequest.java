package rs.edu.raf.banka.berza.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForexTimeseriesUpdateRequest {

    String type;
    String symbolTo;
    String symbolFrom;
    String interval;

    @JsonIgnore
    String requestType;

    public static ForexTimeseriesUpdateRequest getForType(String type, String symbolTo, String symbolFrom) {
        ForexTimeseriesUpdateRequest req = new ForexTimeseriesUpdateRequest();
        req.symbolTo = symbolTo;
        req.symbolFrom = symbolFrom;
        req.requestType = type;
        switch (type) {
            case "1d":
                req.type = "intraday";
                req.interval = "5min";
                break;
            case "5d":
                req.type = "intraday";
                req.interval = "30min";
                break;
            case "1m":
            case "6m":
            case "1y":
            case "ytd":
            case "2y":
                req.type = "daily";
                break;
            default:
                return null;
        }
        return req;
    }

    public String getInterval() {
        if(interval == null) {
            return "";
        }
        return interval;
    }
}
