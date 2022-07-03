package rs.edu.raf.banka.racun;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import rs.edu.raf.banka.racun.model.contract.ContractDocument;
import rs.edu.raf.banka.racun.model.contract.Ugovor;
import rs.edu.raf.banka.racun.repository.company.CompanyRepository;
import rs.edu.raf.banka.racun.repository.contract.ContractDocumentRepository;
import rs.edu.raf.banka.racun.repository.contract.UgovorRepository;
import rs.edu.raf.banka.racun.service.impl.ContractDocumentService;
import rs.edu.raf.banka.racun.service.impl.UgovorService;
import rs.edu.raf.banka.racun.service.impl.UserService;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContractDocumentTests {

    @InjectMocks
    ContractDocumentService contractDocumentService;

    @Mock
    ContractDocumentRepository documentRepository;

    @Test
    void saveDocumentTest() throws IOException {
        var ugovor = new Ugovor();
        ugovor.setId(1L);
        var document = new MockMultipartFile("Test", new byte[] {1,2,3});

        var contractDocument = new ContractDocument();
        contractDocument.setId("ok");

        when(documentRepository.save(any())).thenReturn(contractDocument);

        assertEquals(contractDocumentService.saveDocument(ugovor, document), "ok");
    }


    @Test
    void getDocumentTest() throws IOException {
        var contractDocument = new ContractDocument();
        contractDocument.setId("ok");

        when(documentRepository.findById("ok")).thenReturn(Optional.of(contractDocument));

        assertEquals(contractDocumentService.getDocument("ok"), contractDocument);
    }
}
