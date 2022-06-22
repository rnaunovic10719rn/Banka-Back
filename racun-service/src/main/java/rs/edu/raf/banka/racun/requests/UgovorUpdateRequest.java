package rs.edu.raf.banka.racun.requests;

import lombok.Data;

@Data
public class UgovorUpdateRequest
{
    Long id;

    String company;

    String delodavniBroj;

    String description;

}
