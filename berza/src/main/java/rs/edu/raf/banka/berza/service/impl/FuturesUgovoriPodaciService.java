package rs.edu.raf.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.dto.FuturesPodaciDto;
import rs.edu.raf.banka.berza.model.FuturesUgovori;
import rs.edu.raf.banka.berza.repository.FuturesUgovoriRepository;
import rs.edu.raf.banka.berza.service.remote.InfluxScrapperService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Service
public class FuturesUgovoriPodaciService {

    private final InfluxScrapperService influxScrapperService;

    private FuturesUgovoriRepository futuresUgovoriRepository;

    private List<String> odabraniFuturesUgovori = Arrays.asList("CONFH2022", "FBTPH2022", "FBTPM2022", "FBTSH2022", "FBUTQ2022");

    @Autowired
    public FuturesUgovoriPodaciService(FuturesUgovoriRepository futuresUgovoriRepository, InfluxScrapperService influxScrapperService){
        this.futuresUgovoriRepository = futuresUgovoriRepository;
        this.influxScrapperService = influxScrapperService;
    }

    public List<FuturesPodaciDto> getOdabraniFuturesUgovori() {
        List<FuturesPodaciDto> podaci = new ArrayList<>();

        for(String f: odabraniFuturesUgovori) {
           podaci.add(this.getFuturesUgovor(f));
        }

        return podaci;
    }

    public boolean isRelevant(Long id){
        if(futuresUgovoriRepository.findFuturesUgovoriByIdAndSettlementDateAfter(id, new Date()) == null)
            return false;
        return true;
    }

    public FuturesPodaciDto getFuturesUgovor(String symbol) {
        FuturesUgovori future = futuresUgovoriRepository.findFuturesUgovoriByOznakaHartije(symbol);
        if(future == null){
            future = new FuturesUgovori();
            future.setOznakaHartije(symbol);
            futuresUgovoriRepository.save(future);
        }

        List<FuturesPodaciDto> res = influxScrapperService.getFuturesQoute(symbol);
        if(res != null && res.size() > 0) {
            FuturesPodaciDto futuresPodaciDto = res.get(res.size()-1);
            futuresPodaciDto.setId(future.getId());
            return futuresPodaciDto;
        }

        return null;
    }

    public FuturesPodaciDto getFuturesUgovorById(Long id) {
        FuturesUgovori future = futuresUgovoriRepository.findFuturesById(id);
        return this.getFuturesUgovor(future.getOznakaHartije());
    }

    public FuturesUgovori getById(Long id) {
        return futuresUgovoriRepository.getById(id);
    }

}
