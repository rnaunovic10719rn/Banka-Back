package rs.edu.raf.banka.berza.service.impl;

import com.crazzyghost.alphavantage.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import rs.edu.raf.banka.berza.dto.AkcijePodaciDto;
import rs.edu.raf.banka.berza.dto.ForexPodaciDto;
import rs.edu.raf.banka.berza.dto.ForexTimeseriesDto;
import rs.edu.raf.banka.berza.dto.request.*;
import rs.edu.raf.banka.berza.model.Forex;
import rs.edu.raf.banka.berza.model.Valuta;
import rs.edu.raf.banka.berza.repository.ForexRepository;
import rs.edu.raf.banka.berza.repository.ValutaRepository;
import rs.edu.raf.banka.berza.service.remote.InfluxScrapperService;
import rs.edu.raf.banka.berza.utils.DateUtils;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;

@Service
public class ForexPodaciService {

    private ForexRepository forexRepository;
    private ValutaRepository valutaRepository;

    private final InfluxScrapperService influxScrapperService;
    private final Config alphavantageApiClient;

    private final List<String> odabaraniParovi = Arrays.asList("EUR/USD", "EUR/RSD", "USD/RSD");

    @Autowired
    public ForexPodaciService(ForexRepository forexRepository, ValutaRepository valutaRepository,
                              InfluxScrapperService influxScrapperService,
                              Config alphavantageApiClient){
        this.forexRepository = forexRepository;
        this.valutaRepository = valutaRepository;
        this.influxScrapperService = influxScrapperService;
        this.alphavantageApiClient = alphavantageApiClient;
    }

    public List<ForexPodaciDto> getOdabraniParovi() {
        List<ForexPodaciDto> fpd = new ArrayList<>();
        for(String p: odabaraniParovi) {
            String[] ps = p.split("/");
            if(ps.length != 2) {
                continue;
            }
            fpd.add(getForexBySymbol(ps[0], ps[1]));
        }
        return fpd;
    }

    public ForexPodaciDto getForexBySymbol(String symbolFrom, String symbolTo) {
        Valuta valuta1 = valutaRepository.findByOznakaValute(symbolFrom);
        Valuta valuta2 = valutaRepository.findByOznakaValute(symbolTo);
        Forex forex = null;
        if(valuta1 != null && valuta2 != null)
            forex = forexRepository.findForexByBaseCurrencyAndQuoteCurrency(valuta1, valuta2);
        if(forex == null){
            forex = new Forex();
            forex.setBaseCurrency(valuta1);
            forex.setQuoteCurrency(valuta2);
            forexRepository.save(forex);
        }

        List<ForexPodaciDto> dtoList = influxScrapperService.getForexQuote(symbolFrom, symbolTo);
        if(dtoList == null || dtoList.size() == 0) {
            return null;
        }

        ForexPodaciDto forexPodaciDto = dtoList.get(dtoList.size()-1);
        forexPodaciDto.setId(forex.getId());
        return forexPodaciDto;
    }

    public List<ForexTimeseriesDto> getForexTimeseries(ForexTimeseriesUpdateRequest req) {
        DateUtils.StartEndDateTime dt = DateUtils.getStartEndDateTime(req.getType(), req.getInterval(), req.getRequestType());

        ForexTimeseriesReadRequest readReq = new ForexTimeseriesReadRequest();
        readReq.setType(req.getType());
        readReq.setSymbolTo(req.getSymbolTo());
        readReq.setSymbolFrom(req.getSymbolFrom());
        readReq.setInterval(req.getInterval());
        readReq.setTimeFrom(dt.startDate);
        readReq.setTimeTo(dt.endDate);

        return influxScrapperService.getForexTimeseries(readReq);
    }

    public List<Forex> getAllForex(){
        return forexRepository.findAll();
    }

}
