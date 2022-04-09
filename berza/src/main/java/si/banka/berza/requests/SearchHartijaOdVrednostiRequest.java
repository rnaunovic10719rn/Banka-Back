package si.banka.berza.requests;

import lombok.Data;

@Data
public class SearchHartijaOdVrednostiRequest {

    private String oznaka_hartije;
    private String opis_hartije;
}
