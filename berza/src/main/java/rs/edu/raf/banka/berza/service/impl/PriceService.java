package rs.edu.raf.banka.berza.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.dto.AkcijePodaciDto;
import rs.edu.raf.banka.berza.dto.AskBidPriceDto;
import rs.edu.raf.banka.berza.dto.ForexPodaciDto;
import rs.edu.raf.banka.berza.dto.FuturesPodaciDto;
import rs.edu.raf.banka.berza.enums.HartijaOdVrednostiType;
import rs.edu.raf.banka.berza.model.Akcije;
import rs.edu.raf.banka.berza.model.Forex;
import rs.edu.raf.banka.berza.model.FuturesUgovori;
import rs.edu.raf.banka.berza.repository.BerzaRepository;

@Service
@Slf4j
public class PriceService {

    private BerzaRepository berzaRepository;
    private AkcijePodaciService akcijePodaciService;
    private ForexPodaciService forexPodaciService;
    private FuturesUgovoriPodaciService futuresUgovoriPodaciService;

    @Autowired
    public PriceService(AkcijePodaciService akcijePodaciService, ForexPodaciService forexPodaciService, FuturesUgovoriPodaciService futuresUgovoriPodaciService, BerzaRepository berzaRepository) {
        this.berzaRepository = berzaRepository;
        this.akcijePodaciService = akcijePodaciService;
        this.forexPodaciService = forexPodaciService;
        this.futuresUgovoriPodaciService = futuresUgovoriPodaciService;
    }

    public AskBidPriceDto getAskBidPrice(HartijaOdVrednostiType hartijaTip, Long id) {
        if(hartijaTip.equals(HartijaOdVrednostiType.AKCIJA)){
            Akcije akcija = akcijePodaciService.getByID(id);

            return getAskBidPrice(hartijaTip, akcija.getOznakaHartije());
        } else if(hartijaTip.equals(HartijaOdVrednostiType.FUTURES_UGOVOR)) {
            FuturesUgovori futuresUgovor = futuresUgovoriPodaciService.getById(id);

            return getAskBidPrice(hartijaTip, futuresUgovor.getOznakaHartije());
        } else if(hartijaTip.equals(HartijaOdVrednostiType.FOREX)) {
            Forex forex = forexPodaciService.getById(id);

            return getAskBidPrice(hartijaTip, forex.getOznakaHartije());
        }

        return null;
    }

    public AskBidPriceDto getAskBidPrice(HartijaOdVrednostiType hartijaTip, String symbol) {
        AskBidPriceDto askBidPrice = new AskBidPriceDto();

        if(hartijaTip.equals(HartijaOdVrednostiType.AKCIJA)){
            AkcijePodaciDto akcije = akcijePodaciService.getAkcijaByTicker(symbol);
            if(akcije != null) {
                askBidPrice.setHartijaId(akcije.getId());
                askBidPrice.setBerza(berzaRepository.getById(akcije.getBerzaId()));

                // NB: Ne postoje podaci o asku, bidu, uzima se trenutna cena
                askBidPrice.setAsk(akcije.getPrice());
                askBidPrice.setBid(akcije.getPrice());
            }
        }
        else if(hartijaTip.equals(HartijaOdVrednostiType.FUTURES_UGOVOR)){
            FuturesPodaciDto futuresUgovori = futuresUgovoriPodaciService.getFuturesUgovor(symbol);
            if(futuresUgovori != null) {
                askBidPrice.setHartijaId(futuresUgovori.getId());
                askBidPrice.setAsk(futuresUgovori.getHigh());
                askBidPrice.setBid(futuresUgovori.getHigh());
            }
        }
        else if(hartijaTip.equals(HartijaOdVrednostiType.FOREX)){
            String[] split = symbol.split(" ");
            ForexPodaciDto forex = forexPodaciService.getForexBySymbol(split[1], split[0]);
            if(forex != null) {
                askBidPrice.setHartijaId(forex.getId());
                askBidPrice.setAsk(forex.getAsk());
                askBidPrice.setBid(forex.getBid());
            }
        }

        return askBidPrice;
    }

}
