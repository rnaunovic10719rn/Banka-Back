package rs.edu.raf.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.dto.FuturesPodaciDto;
import rs.edu.raf.banka.berza.dto.FuturesTimeseriesDto;
import rs.edu.raf.banka.berza.dto.request.FuturesTimeseriesReadRequest;
import rs.edu.raf.banka.berza.model.FuturesUgovori;
import rs.edu.raf.banka.berza.repository.FuturesUgovoriRepository;
import rs.edu.raf.banka.berza.service.remote.InfluxScrapperService;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;

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

    public ZonedDateTime getZonedDateTime() {
        return ZonedDateTime.now().plusDays(2);
    }

    public List<FuturesTimeseriesDto> getFuturesTimeseries(String type, String symbol) {
        DateTimeFormatter startFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00.000'Z'");
        DateTimeFormatter endFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        ZonedDateTime zonedDateTime = getZonedDateTime();
        String endDate = zonedDateTime.format(endFormatter);

        if(type.equals("1d")) {
            switch (zonedDateTime.getDayOfWeek()) {
                case SATURDAY:
                case SUNDAY:
                    zonedDateTime = zonedDateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY));
                    break;
                case MONDAY:
                    if (zonedDateTime.getHour() < 16) {
                        zonedDateTime = zonedDateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY));
                    }
                    break;
            }
        } else if(type.equals("5d")) {
            switch (zonedDateTime.getDayOfWeek()) {
                case SATURDAY:
                case SUNDAY:
                    zonedDateTime = zonedDateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    break;
                case MONDAY:
                    zonedDateTime = zonedDateTime.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
                    break;
                default:
                    zonedDateTime = zonedDateTime.minusDays(7); // 7 zbog vikenda
            }
        } else {
            switch (type) {
                case "1m":
                    zonedDateTime = zonedDateTime.minusMonths(1);
                    break;
                case "6m":
                    zonedDateTime = zonedDateTime.minusMonths(6);
                    break;
                case "1y":
                    zonedDateTime = zonedDateTime.minusMonths(12);
                    break;
                case "2y":
                    zonedDateTime = zonedDateTime.minusMonths(24);
                    break;
                case "ytd":
                    zonedDateTime = zonedDateTime.with(firstDayOfYear());
                    break;
            }
        }

        String startDate = zonedDateTime.format(startFormatter);

        FuturesTimeseriesReadRequest readReq = new FuturesTimeseriesReadRequest();
        readReq.setSymbol(symbol);
        readReq.setTimeFrom(startDate);
        readReq.setTimeTo(endDate);

        return influxScrapperService.getFuturesTimeseries(readReq);
    }

}
