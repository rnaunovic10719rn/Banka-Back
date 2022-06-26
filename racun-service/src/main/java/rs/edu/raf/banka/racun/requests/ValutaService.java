package rs.edu.raf.banka.racun.requests;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.repository.ValutaRepository;

import java.util.List;

@Service
public class ValutaService {

    private final ValutaRepository valutaRepository;

    @Autowired
    public ValutaService(ValutaRepository valutaRepository) {
        this.valutaRepository = valutaRepository;
    }

    public List<Valuta> getValute() {
        return valutaRepository.findAll();
    }
}
