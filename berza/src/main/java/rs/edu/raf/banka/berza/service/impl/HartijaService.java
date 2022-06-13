package rs.edu.raf.banka.berza.service.impl;

import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.model.HartijaOdVrednosti;
import rs.edu.raf.banka.berza.repository.AkcijeRepository;
import rs.edu.raf.banka.berza.repository.ForexRepository;
import rs.edu.raf.banka.berza.repository.FuturesUgovoriRepository;
import rs.edu.raf.banka.berza.repository.HartijaRepository;
import rs.edu.raf.banka.berza.utils.HttpUtils;

import java.util.List;

@Service
public class HartijaService
{
    private HartijaRepository hartijaRepository;
    private ForexRepository forexRepository;
    private FuturesUgovoriRepository futuresUgovoriRepository;
    private AkcijeRepository akcijeRepository;

    public HartijaService(HartijaRepository hartijaRepository, ForexRepository forexRepository, FuturesUgovoriRepository futuresUgovoriRepository, AkcijeRepository akcijeRepository){
        this.hartijaRepository = hartijaRepository;
        this.forexRepository = forexRepository;
        this.futuresUgovoriRepository = futuresUgovoriRepository;
        this.akcijeRepository = akcijeRepository;
    }

    //TODO: Call influx update on selected types
    public List<HartijaOdVrednosti> getAllNearSettlement() {
        return hartijaRepository.getAllNearSettlement();
    }

    public HartijaOdVrednosti findHartijaByIdAndType(Long id, String hartijaType)
    {
        if(hartijaType.equalsIgnoreCase("AKCIJA"))
            return akcijeRepository.findAkcijeById(id);
        else if(hartijaType.equalsIgnoreCase("FOREX"))
            return forexRepository.findForexById(id);
        else if (hartijaType.equalsIgnoreCase("FUTURE_UGOVOR"))
            return futuresUgovoriRepository.findFuturesById(id);
        else
            throw new ArrayIndexOutOfBoundsException("hartijaType");
    }
}
