package rs.edu.raf.banka.racun.service.impl;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.edu.raf.banka.racun.exceptions.ContractDocumentException;
import rs.edu.raf.banka.racun.model.contract.ContractDocument;
import rs.edu.raf.banka.racun.model.contract.Ugovor;
import rs.edu.raf.banka.racun.repository.contract.ContractDocumentRepository;

import java.io.IOException;
import java.util.Optional;

@Service
public class ContractDocumentService {

    private ContractDocumentRepository documentRepository;

    @Autowired
    public ContractDocumentService(ContractDocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public String saveDocument(Ugovor contract, MultipartFile document) throws IOException, ContractDocumentException {
        if (contract == null || document == null || document.isEmpty()) {
            throw new ContractDocumentException("Invalid add document request");
        }

        ContractDocument contractDocument = new ContractDocument();
        contractDocument.setContract(contract);
        contractDocument.setDocument(new Binary(BsonBinarySubType.BINARY, document.getBytes()));
        contractDocument = documentRepository.save(contractDocument);

        return contractDocument.getId();
    }

    public ContractDocument getDocument(String id) throws ContractDocumentException {
        Optional<ContractDocument> contractDocument = documentRepository.findById(id);
        if(contractDocument.isEmpty()) {
            throw new ContractDocumentException("Document with given ID not found");
        }
        return contractDocument.get();
    }

}
