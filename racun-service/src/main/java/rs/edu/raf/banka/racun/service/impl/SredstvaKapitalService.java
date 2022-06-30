package rs.edu.raf.banka.racun.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.dto.*;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.enums.MarginTransakcijaType;
import rs.edu.raf.banka.racun.enums.RacunType;
import rs.edu.raf.banka.racun.model.Racun;
import rs.edu.raf.banka.racun.model.SredstvaKapital;
import rs.edu.raf.banka.racun.model.Transakcija;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.model.margins.MarginTransakcija;
import rs.edu.raf.banka.racun.repository.*;
import rs.edu.raf.banka.racun.utils.HttpUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SredstvaKapitalService {

    private final SredstvaKapitalRepository sredstvaKapitalRepository;

    private final TransakcijaRepository transakcijaRepository;
    private final MarginTransakcijaRepository marginTransakcijaRepository;
    private final RacunRepository racunRepository;
    private final ValutaRepository valutaRepository;
    private final UserService userService;

    @Value("${racun.berza-service-baseurl}")
    private String BERZA_SERVICE_BASE_URL;

    @Autowired
    public SredstvaKapitalService(SredstvaKapitalRepository sredstvaKapitalRepository,
                                  TransakcijaRepository transakcijaRepository,
                                  MarginTransakcijaRepository marginTransakcijaRepository,
                                  RacunRepository racunRepository,
                                  ValutaRepository valutaRepository,
                                  UserService userService) {
        this.sredstvaKapitalRepository = sredstvaKapitalRepository;
        this.transakcijaRepository = transakcijaRepository;
        this.marginTransakcijaRepository = marginTransakcijaRepository;
        this.racunRepository = racunRepository;
        this.valutaRepository = valutaRepository;
        this.userService = userService;
    }

    public List<SredstvaKapital> getAll(UUID racun) {
        return sredstvaKapitalRepository.findAllByRacun(racunRepository.findByBrojRacuna(racun));
    }

    public SredstvaKapital get(UUID racun, String valuta) {
        return sredstvaKapitalRepository.findByRacunAndValuta(racunRepository.findByBrojRacuna(racun), valutaRepository.findValutaByKodValute(valuta));
    }

    public SredstvaKapital get(UUID racun, KapitalType hartijaType, Long hartijaId) {
        return sredstvaKapitalRepository.findByRacunAndHaritja(racunRepository.findByBrojRacuna(racun), hartijaType, hartijaId);
    }

    public SredstvaKapital pocetnoStanje(UUID uuidRacuna, String kodValute, double ukupno) {
        Racun racun = racunRepository.findByBrojRacuna(uuidRacuna);
        if(racun == null) {
            return null;
        }
        Valuta valuta = valutaRepository.findValutaByKodValute(kodValute);
        if(valuta == null) {
            return null;
        }

        SredstvaKapital sredstvaKapital;
        sredstvaKapital = new SredstvaKapital();
        sredstvaKapital.setRacun(racun);
        sredstvaKapital.setValuta(valuta);
        sredstvaKapital.setUkupno(ukupno);
        sredstvaKapital.setRezervisano(0);
        sredstvaKapital.setRaspolozivo(ukupno);
        sredstvaKapital.setKapitalType(KapitalType.NOVAC);
        sredstvaKapital.setHaritjeOdVrednostiID(-1L);
        return sredstvaKapitalRepository.save(sredstvaKapital);
    }

    public SredstvaKapital pocetnoStanje(UUID uuidRacuna, KapitalType kapitalType, Long hartijaId, double ukupno) {
        Racun racun = racunRepository.findByBrojRacuna(uuidRacuna);
        if(racun == null) {
            return null;
        }

        SredstvaKapital sredstvaKapital = new SredstvaKapital();
        sredstvaKapital.setRacun(racun);
        sredstvaKapital.setUkupno(ukupno);
        sredstvaKapital.setRezervisano(0);
        sredstvaKapital.setRaspolozivo(ukupno);
        sredstvaKapital.setKapitalType(kapitalType);
        sredstvaKapital.setHaritjeOdVrednostiID(hartijaId);
        sredstvaKapital.setKreditnaSredstva(0.0);
        sredstvaKapital.setMaintenanceMargin(0.0);
        return sredstvaKapitalRepository.save(sredstvaKapital);
    }

    public List<KapitalHartijeDto> getUkupnoStanjePoHartijama(String token, boolean margin) {
        Racun racun = getRacun(margin);
        List<SredstvaKapital> sredstvaKapitals = sredstvaKapitalRepository.findAllByRacun(racun);
        List<KapitalHartijeDto> toReturn = new ArrayList<>();
        KapitalHartijeDto khdAkcija = new KapitalHartijeDto(KapitalType.AKCIJA, 0.0);
        KapitalHartijeDto khdFuture = new KapitalHartijeDto(KapitalType.FUTURE_UGOVOR, 0.0);

        for (SredstvaKapital sredstvaKapital : sredstvaKapitals) {
            if (sredstvaKapital.getKapitalType().equals(KapitalType.AKCIJA)) {
                AkcijePodaciDto akcijePodaciDto = this.getAkcija(sredstvaKapital.getHaritjeOdVrednostiID());
                BerzaDto berzaDto = this.getBerza(akcijePodaciDto.getBerzaId());
                ForexPodaciDto forexPodaciDto = this.getForex(token, berzaDto.getKodValute());

                Double cenaTrenutneHartije = sredstvaKapital.getRaspolozivo() * akcijePodaciDto.getPrice();
                if(!margin) {
                     cenaTrenutneHartije *= forexPodaciDto.getExchangeRate();
                }
                khdAkcija.setUkupno(khdAkcija.getUkupno() + cenaTrenutneHartije);
            }

            if (sredstvaKapital.getKapitalType().equals(KapitalType.FUTURE_UGOVOR)) {
                FuturesPodaciDto futuresPodaciDto = this.getFuture(sredstvaKapital.getHaritjeOdVrednostiID());
                ForexPodaciDto forexPodaciDto = this.getForex(token,"USD");

                Double cenaTrenutneHartije = sredstvaKapital.getRaspolozivo() * futuresPodaciDto.getOpen();
                if(!margin) {
                    cenaTrenutneHartije *=  forexPodaciDto.getExchangeRate();
                }
                khdFuture.setUkupno(khdFuture.getUkupno() + cenaTrenutneHartije);
            }
        }
        toReturn.add(khdAkcija);
        toReturn.add(khdFuture);
        return toReturn;
    }

    public List<KapitalPoTipuHartijeDto> getStanjeJednogTipaHartije(String token, String kapitalType, boolean margin) {
        Racun racun = getRacun(margin);
        List<SredstvaKapital> sredstvaKapitals = sredstvaKapitalRepository.findAllByRacun(racun);

        List<KapitalPoTipuHartijeDto> toReturn = new ArrayList<>();

        for (SredstvaKapital sredstvaKapital : sredstvaKapitals) {
            if (sredstvaKapital.getKapitalType().equals(KapitalType.NOVAC))
                continue;
            if (sredstvaKapital.getKapitalType().equals(KapitalType.AKCIJA) && kapitalType.equals(KapitalType.AKCIJA.toString())) {
                AkcijePodaciDto akcijePodaciDto = this.getAkcija(sredstvaKapital.getHaritjeOdVrednostiID());
                BerzaDto berzaDto = this.getBerza(akcijePodaciDto.getBerzaId());

                KapitalPoTipuHartijeDto kapitalPoTipuHartijeDto = new KapitalPoTipuHartijeDto();
                kapitalPoTipuHartijeDto.setId(akcijePodaciDto.getId());
                kapitalPoTipuHartijeDto.setOznakaHartije(akcijePodaciDto.getTicker());
                kapitalPoTipuHartijeDto.setBerza(berzaDto.getOznakaBerze());
                kapitalPoTipuHartijeDto.setKolicinaUVlasnistvu((long) sredstvaKapital.getUkupno());
                kapitalPoTipuHartijeDto.setCena(akcijePodaciDto.getPrice());
                kapitalPoTipuHartijeDto.setKodValute(berzaDto.getKodValute());

                ForexPodaciDto forexPodaciDto = this.getForex(token, berzaDto.getKodValute());
                preracunajCene(kapitalPoTipuHartijeDto, sredstvaKapital, forexPodaciDto, kapitalType, margin);

                toReturn.add(kapitalPoTipuHartijeDto);
            }
            if (sredstvaKapital.getKapitalType().equals(KapitalType.FUTURE_UGOVOR) && kapitalType.equals(KapitalType.FUTURE_UGOVOR.toString())) {
                FuturesPodaciDto futuresPodaciDto = this.getFuture(sredstvaKapital.getHaritjeOdVrednostiID());

                KapitalPoTipuHartijeDto kapitalPoTipuHartijeDto = new KapitalPoTipuHartijeDto();
                kapitalPoTipuHartijeDto.setId(futuresPodaciDto.getId());
                kapitalPoTipuHartijeDto.setOznakaHartije(futuresPodaciDto.getSymbol());
                kapitalPoTipuHartijeDto.setBerza("EUREX");
                kapitalPoTipuHartijeDto.setKolicinaUVlasnistvu((long) sredstvaKapital.getUkupno());
                kapitalPoTipuHartijeDto.setCena(futuresPodaciDto.getOpen());
                kapitalPoTipuHartijeDto.setKodValute("USD");

                ForexPodaciDto forexPodaciDto = this.getForex(token, "USD");
                preracunajCene(kapitalPoTipuHartijeDto, sredstvaKapital, forexPodaciDto, kapitalType, margin);

                toReturn.add(kapitalPoTipuHartijeDto);
            }
        }
        return toReturn;
    }

    private void preracunajCene(KapitalPoTipuHartijeDto kapitalPoTipuHartijeDto, SredstvaKapital sredstvaKapital, ForexPodaciDto forexPodaciDto, String kapitalType, boolean margin) {
        Double vrednostValuta = kapitalPoTipuHartijeDto.getCena() * kapitalPoTipuHartijeDto.getKolicinaUVlasnistvu();
        if(!margin) {
            vrednostValuta *= forexPodaciDto.getExchangeRate();
        }
        kapitalPoTipuHartijeDto.setVrednostRSD(vrednostValuta);
        kapitalPoTipuHartijeDto.setVrednost(kapitalPoTipuHartijeDto.getCena() * kapitalPoTipuHartijeDto.getKolicinaUVlasnistvu()); // Vrednost u originalnoj valuti

        Double kupljenoZa;
        if(!margin) {
            kupljenoZa = transakcijaRepository.getKupljenoZa(sredstvaKapital.getHaritjeOdVrednostiID(), KapitalType.valueOf(kapitalType));
        } else {
            kupljenoZa = marginTransakcijaRepository.getKupljenoZa(sredstvaKapital.getHaritjeOdVrednostiID(), KapitalType.valueOf(kapitalType));
        }
        kapitalPoTipuHartijeDto.setKupljenoZa(kupljenoZa);

        Double profit = kapitalPoTipuHartijeDto.getVrednost() - kupljenoZa;
        kapitalPoTipuHartijeDto.setProfit(profit);
    }

    public List<TransakcijeHartijeDto> getTransakcijeHartijeKes(Long id, String strKapitalType) {
        Racun racun = getRacun(false);
        KapitalType kapitalType = KapitalType.valueOf(strKapitalType.toUpperCase());
        List<Transakcija> transakcijaList = transakcijaRepository.findByHaritjeOdVrednostiIDAndKapitalTypeAndRacun(id, kapitalType, racun);
        List<TransakcijeHartijeDto> toReturn = new ArrayList<>();
        for (Transakcija transakcija : transakcijaList) {
            TransakcijeHartijeDto transakcijeHartijeDto = new TransakcijeHartijeDto();
            transakcijeHartijeDto.setDatum(transakcija.getDatumVreme());
            if (transakcija.getUplata() > 0) {
                transakcijeHartijeDto.setTipOrdera("Kupovina");
                transakcijeHartijeDto.setKolicina(Math.round(transakcija.getUplata()));
            } else if (transakcija.getIsplata() > 0) {
                transakcijeHartijeDto.setTipOrdera("Prodaja");
                transakcijeHartijeDto.setKolicina(Math.round(transakcija.getIsplata()));
            } else {
                continue; // npr. rezervacija, nije nam bitna za ovo
            }
            transakcijeHartijeDto.setCena(transakcija.getUnitPrice());
            transakcijeHartijeDto.setUkupno(transakcija.getUnitPrice()*transakcijeHartijeDto.getKolicina());
            toReturn.add(transakcijeHartijeDto);
        }
        return toReturn;
    }

    public List<MarginTransakcijeHartijeDto> getTransakcijeHartijeMargins(Long id, String strKapitalType) {
        Racun racun = getRacun(true);
        KapitalType kapitalType = KapitalType.valueOf(strKapitalType.toUpperCase());
        List<MarginTransakcija> transakcijaList = marginTransakcijaRepository.findByHaritjeOdVrednostiIDAndKapitalTypeAndRacun(id, kapitalType, racun);
        List<MarginTransakcijeHartijeDto> toReturn = new ArrayList<>();
        for (MarginTransakcija transakcija : transakcijaList) {
            MarginTransakcijeHartijeDto transakcijeHartijeDto = new MarginTransakcijeHartijeDto();
            transakcijeHartijeDto.setDatum(transakcija.getDatumVreme());
            if(transakcija.getTip() == MarginTransakcijaType.UPLATA) {
                transakcijeHartijeDto.setTipOrdera("Kupovina");
            } else {
                transakcijeHartijeDto.setTipOrdera("Prodaja");
            }
            transakcijeHartijeDto.setKolicina(Math.round(transakcija.getKolicina()));
            transakcijeHartijeDto.setCena(transakcija.getUnitPrice());
            transakcijeHartijeDto.setUkupno(transakcija.getUnitPrice()*transakcijeHartijeDto.getKolicina());
            toReturn.add(transakcijeHartijeDto);
        }
        return toReturn;
    }

    public AkcijePodaciDto getAkcija(Long id) {
        ResponseEntity<AkcijePodaciDto> apdResp = HttpUtils.getAkcijeById(BERZA_SERVICE_BASE_URL, id);
        if (apdResp.getBody() == null) {
            return null;
        }
        return apdResp.getBody();
    }

    public BerzaDto getBerza(Long id) {
        ResponseEntity<BerzaDto> berzaResp = HttpUtils.getBerzaById(BERZA_SERVICE_BASE_URL, id);
        if (berzaResp.getBody() == null) {
            return null;
        }
        return berzaResp.getBody();
    }

    public ForexPodaciDto getForex(String token, String from) {
        ResponseEntity<ForexPodaciDto> fpdResp = HttpUtils.getExchangeRate(BERZA_SERVICE_BASE_URL, token, from, "RSD");
        if (fpdResp.getBody() == null) {
            return null;
        }
        return fpdResp.getBody();
    }

    public FuturesPodaciDto getFuture(Long id) {
        ResponseEntity<FuturesPodaciDto> futureResp = HttpUtils.getFuturesById(BERZA_SERVICE_BASE_URL, id);
        if (futureResp.getBody() == null) {
            return null;
        }
        return futureResp.getBody();
    }


    public List<SupervisorSredstvaKapitalDto> findSredstvaKapitalSupervisor(String token, boolean margin) {
        String role = userService.getRoleByToken(token);
        if (role.equals("ROLE_AGENT"))
            return null;

        Racun racun = getRacun(margin);
        List<SredstvaKapital> sredstvaKapitals = sredstvaKapitalRepository.findAllByRacun(racun);
        List<SupervisorSredstvaKapitalDto> sredstvaKapitalDtos = new ArrayList<>();

        for (SredstvaKapital sredstvaKapital : sredstvaKapitals) {
            if (!sredstvaKapital.getKapitalType().equals(KapitalType.NOVAC)) {
                continue;
            }
            SupervisorSredstvaKapitalDto s = new SupervisorSredstvaKapitalDto();
            s.setKodValute(sredstvaKapital.getValuta().getKodValute());
            s.setUkupno(sredstvaKapital.getUkupno());
            s.setRezervisano(sredstvaKapital.getRezervisano());
            s.setRaspolozivo(sredstvaKapital.getRaspolozivo());

            if(margin) {
                s.setKredit(sredstvaKapital.getKreditnaSredstva());
                s.setMaintenanceMargin(sredstvaKapital.getMaintenanceMargin());
                s.setMarginCall(sredstvaKapital.getMarginCall());
            }

            sredstvaKapitalDtos.add(s);
        }
        return sredstvaKapitalDtos;
    }

    public AgentSredstvaKapitalDto findSredstvaKapitalAgent(String token) {
        String role = userService.getRoleByToken(token);
        if (role.equals("ROLE_AGENT")) {
            AgentSredstvaKapitalDto agentSredstvaKapitalDto = new AgentSredstvaKapitalDto();
            agentSredstvaKapitalDto.setLimit(userService.getUserByToken(token).getLimit());
            agentSredstvaKapitalDto.setLimitUsed(userService.getUserByToken(token).getLimitUsed());
            agentSredstvaKapitalDto.setRaspolozivoAgentu(agentSredstvaKapitalDto.getLimit() - agentSredstvaKapitalDto.getLimitUsed());
            return agentSredstvaKapitalDto;
        }
           return null;
    }

    public void pocetnoStanjeMarzniRacun(UUID uuidRacuna){
        Racun racun = racunRepository.findByBrojRacuna(uuidRacuna);
        if(racun == null) {
            return;
        }

        Valuta valuta = valutaRepository.findValutaByKodValute("USD");
        if(valuta == null) {
            return;
        }

        SredstvaKapital mRacun = new SredstvaKapital();
        mRacun.setRacun(racun);
        mRacun.setValuta(valuta);
        mRacun.setHaritjeOdVrednostiID(-1L);
        mRacun.setKapitalType(KapitalType.MARGIN);
        mRacun.setKreditnaSredstva(0.0);
        mRacun.setMaintenanceMargin(0.0);
        mRacun.setMarginCall(false);
        sredstvaKapitalRepository.save(mRacun);
    }

    private Racun getRacun(boolean margin) {
        if(!margin) {
            return racunRepository.findRacunByTipRacuna(RacunType.KES);
        }
        return racunRepository.findRacunByTipRacuna(RacunType.MARGINS_RACUN);
    }
}
