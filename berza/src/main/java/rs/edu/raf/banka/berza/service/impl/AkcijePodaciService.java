package rs.edu.raf.banka.berza.service.impl;

import com.crazzyghost.alphavantage.fundamentaldata.response.CompanyOverview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.dto.AkcijePodaciDto;
import rs.edu.raf.banka.berza.dto.AkcijeTimeseriesDto;
import rs.edu.raf.banka.berza.dto.request.AkcijeTimeseriesReadRequest;
import rs.edu.raf.banka.berza.dto.request.AkcijeTimeseriesUpdateRequest;
import rs.edu.raf.banka.berza.model.Akcije;
import rs.edu.raf.banka.berza.model.Berza;
import rs.edu.raf.banka.berza.repository.AkcijeRepository;
import rs.edu.raf.banka.berza.repository.BerzaRepository;
import rs.edu.raf.banka.berza.service.remote.AlphaVantageService;
import rs.edu.raf.banka.berza.service.remote.InfluxScrapperService;
import rs.edu.raf.banka.berza.utils.DateUtils;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class AkcijePodaciService {

    private final InfluxScrapperService influxScrapperService;
    private final AlphaVantageService alphaVantageService;

    private BerzaRepository berzaRepository;
    private AkcijeRepository akcijeRepository;

    private final List<String> odabraneAkcije = Arrays.asList("AAPL", "MSFT", "GOOG", "BA", "AXP");

    @Autowired
    public AkcijePodaciService(AkcijeRepository akcijeRepository, BerzaRepository berzaRepository,
                               InfluxScrapperService influxScrapperService,
                               AlphaVantageService alphaVantageService){
        this.akcijeRepository = akcijeRepository;
        this.berzaRepository = berzaRepository;
        this.influxScrapperService = influxScrapperService;
        this.alphaVantageService = alphaVantageService;
    }

    public List<AkcijePodaciDto> getOdabraneAkcije() {
        ArrayList<AkcijePodaciDto> dtos = new ArrayList<>();
        for(String akcija: odabraneAkcije) {
            AkcijePodaciDto dto = getAkcijaByTicker(akcija);
            dtos.add(dto);
        }

        return dtos;
    }

    public AkcijePodaciDto getAkcijaByTicker(String ticker) {
        Akcije akcija = akcijeRepository.findAkcijeByOznakaHartije(ticker);
        if(akcija == null || DateUtils.isDateInDecayDays(akcija.getLastUpdated(), 1) || akcija.getCustom()) {
            CompanyOverview co = alphaVantageService.getCompanyOverview(ticker);
            if(co == null) {
                return null;
            }
            if(akcija == null) {
                akcija = new Akcije();
            }

            Berza berza = berzaRepository.findBerzaByOznakaBerze(co.getExchange());
            akcija.setBerza(berza);
            akcija.setOznakaHartije(co.getSymbol());
            akcija.setOpisHartije(co.getName());
            akcija.setLastUpdated(new Date());
            akcija.setOutstandingShares(co.getSharesOutstanding());
            akcija.setCustom(false);

            akcijeRepository.save(akcija);
        }

        List<String> symbols = Arrays.asList(ticker);
        List<AkcijePodaciDto> dtoList = influxScrapperService.getStocksQuote(symbols);
        if(dtoList == null || dtoList.size() == 0) {
            return null;
        }

        AkcijePodaciDto dto = dtoList.get(dtoList.size()-1);
        if(akcija.getBerza() != null)
            dto.setBerzaId(akcija.getBerza().getId());
        else
            dto.setBerzaId(-1L);
        dto.setTicker(ticker);
        dto.setId(akcija.getId());
        dto.setOpisHartije(akcija.getOpisHartije());
        dto.setOutstandingShares(akcija.getOutstandingShares());

        return dto;
    }

    public AkcijePodaciDto getAkcijaById(Long id) {
        Akcije akcije = akcijeRepository.findAkcijeById(id);
        return this.getAkcijaByTicker(akcije.getOznakaHartije());
    }

    public List<AkcijeTimeseriesDto> getAkcijeTimeseries(AkcijeTimeseriesUpdateRequest req) {
        ZoneId zoneId = null;
        String openHours = null;

        Akcije akcija = akcijeRepository.findAkcijeByOznakaHartije(req.getSymbol());
        if(akcija != null && akcija.getBerza() != null) {
            zoneId = ZoneId.of(akcija.getBerza().getVremenskaZona());
            openHours = akcija.getBerza().getOpenTime();
        }

        DateUtils.StartEndDateTime dt = DateUtils.getStartEndDateTime(req.getType(), req.getInterval(), req.getRequestType(), zoneId, openHours);

        AkcijeTimeseriesReadRequest readReq = new AkcijeTimeseriesReadRequest();
        readReq.setType(req.getType());
        readReq.setSymbol(req.getSymbol());
        readReq.setInterval(req.getInterval());
        readReq.setTimeFrom(dt.startDate);
        readReq.setTimeTo(dt.endDate);

        return influxScrapperService.getStocksTimeseries(readReq);
    }

    public Akcije getByID(Long id){
        return akcijeRepository.findAkcijeById(id);
    }
}
