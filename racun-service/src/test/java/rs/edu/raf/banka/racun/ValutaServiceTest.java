package rs.edu.raf.banka.racun;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.model.company.Company;
import rs.edu.raf.banka.racun.repository.ValutaRepository;
import rs.edu.raf.banka.racun.service.impl.ValutaService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ValutaServiceTest {

    @InjectMocks
    ValutaService valutaService;

    @Mock
    ValutaRepository valutaRepository;

    @Test
    void getValute() {
        List<Valuta> valute = new ArrayList<>();

        for(int i = 0; i < 2; i++){
            valute.add(new Valuta());
        }

        given(valutaRepository.findAll()).willReturn(valute);

        assertEquals(valutaService.getValute(), valute);
    }
}
