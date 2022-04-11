package rs.edu.raf.banka.berza.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AkcijeTimeseriesUpdateRequest {

    String type;
    String symbol;
    String interval;
    Integer months;

    @JsonIgnore
    String requestType;

    public static AkcijeTimeseriesUpdateRequest getForType(String type, String symbol) {
        AkcijeTimeseriesUpdateRequest req = new AkcijeTimeseriesUpdateRequest();
        req.symbol = symbol;
        req.requestType = type;
        switch (type) {
            case "1d":
                req.type = "intraday";
                req.interval = "5min";
                req.months = 1;
                break;
            case "5d":
                req.type = "intraday";
                req.interval = "30min";
                req.months = 1;
                break;
            case "1m":
                req.type = "daily";
                req.months = 1;
                break;
            case "6m":
                req.type = "daily";
                req.months = 6;
                break;
            case "1y":
            case "ytd":
                req.type = "daily";
                req.months = 12;
                break;
            case "2y":
                req.type = "daily";
                req.months = 24;
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
