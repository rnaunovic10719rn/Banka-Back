package rs.edu.raf.banka.berza.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuturesPodaciDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 8672435550879998864L;

    private String symbol;
    private String time;
    private String date;
    private Double open;
    private Double high;
    private Double low;
    private Double settle;
    private Integer volumeDouble;
    private Integer previousDouble;
    private Integer volume;
    private Integer previous;
    private Long id;

}
