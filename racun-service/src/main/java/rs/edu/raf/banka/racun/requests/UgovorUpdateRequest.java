package rs.edu.raf.banka.racun.requests;

import lombok.Data;

@Data
public class UgovorUpdateRequest
{
    Long id;

    Long companyId;

    String delodavniBroj;

    String description;

}
