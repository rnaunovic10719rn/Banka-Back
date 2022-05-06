package rs.edu.raf.banka.berza.dto;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InflacijaCSV {

    @CsvBindByPosition(position = 0)
    private String currency;

    @CsvBindByPosition(position = 1)
    private String year;

    @CsvBindByPosition(position = 2)
    private String inflationRate;

}
