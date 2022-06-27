package rs.edu.raf.banka.racun.model.contract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.Binary;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@Document(collection = "contracts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractDocument {

    @Id
    private String id;

    private Long ugovorId;

    private Binary document;

}
