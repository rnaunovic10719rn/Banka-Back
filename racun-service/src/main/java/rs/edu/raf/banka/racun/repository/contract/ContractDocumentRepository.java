package rs.edu.raf.banka.racun.repository.contract;

import org.springframework.data.mongodb.repository.MongoRepository;
import rs.edu.raf.banka.racun.model.contract.ContractDocument;

public interface ContractDocumentRepository extends MongoRepository<ContractDocument, String> {
}
