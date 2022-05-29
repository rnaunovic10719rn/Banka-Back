package rs.edu.raf.banka.racun.dto;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyCSV {

    @CsvBindByPosition(position = 0)
    private String isoCode;

    @CsvBindByPosition(position = 1)
    private String description;
}
