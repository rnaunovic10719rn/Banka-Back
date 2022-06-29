package rs.edu.raf.banka.racun.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.enums.MarginTransakcijaType;
import rs.edu.raf.banka.racun.enums.RacunType;
import rs.edu.raf.banka.racun.model.Racun;
import rs.edu.raf.banka.racun.model.SredstvaKapital;
import rs.edu.raf.banka.racun.model.Transakcija;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.model.margins.MarginTransakcija;
import rs.edu.raf.banka.racun.repository.MarginTransakcijaRepository;
import rs.edu.raf.banka.racun.repository.RacunRepository;
import rs.edu.raf.banka.racun.repository.SredstvaKapitalRepository;
import rs.edu.raf.banka.racun.repository.ValutaRepository;
import rs.edu.raf.banka.racun.requests.MarginTransakcijaRequest;
import rs.edu.raf.banka.racun.requests.TransakcijaRequest;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class MarginTransakcijaService {

    private final MarginTransakcijaRepository marginTransakcijaRepository;
    private final RacunRepository racunRepository;
    private final SredstvaKapitalRepository sredstvaKapitalRepository;
    private final SredstvaKapitalService sredstvaKapitalService;
    private final ValutaRepository valutaRepository;
    private final TransakcijaService transakcijaService;
    private final UserService userService;

    private final EntityManager entityManager;

    @Autowired
    public MarginTransakcijaService(MarginTransakcijaRepository marginTransakcijaRepository,
                                    RacunRepository racunRepository,
                                    SredstvaKapitalRepository sredstvaKapitalRepository,
                                    SredstvaKapitalService sredstvaKapitalService,
                                    ValutaRepository valutaRepository,
                                    UserService userService,
                                    TransakcijaService transakcijaService,
                                    EntityManager entityManager) {
        this.marginTransakcijaRepository = marginTransakcijaRepository;
        this.racunRepository = racunRepository;
        this.sredstvaKapitalRepository = sredstvaKapitalRepository;
        this.sredstvaKapitalService = sredstvaKapitalService;
        this.valutaRepository = valutaRepository;
        this.userService = userService;
        this.transakcijaService = transakcijaService;
        this.entityManager = entityManager;
    }

    @Transactional
    public MarginTransakcija dodajTransakciju(String token, MarginTransakcijaRequest request){
        // Ekstraktuj username iz Bearer tokena
        String username;
        if(token.equals("Bearer BERZA-SERVICE")) {
            username = request.getUsername();
        } else {
            username = userService.getUsernameByToken(token);
        }

        // Uzmi objekat Margins racuna
        Racun racun = racunRepository.findRacunByTipRacuna(RacunType.MARGINS_RACUN);
        if (racun == null) {
            log.error("dodajTransakciju: failed to get racun");
            return null;
        }

        // Proveri da li je SredstvaKaptail inicijalizovan za zeljenu hartiju od vrednosti
        Valuta valuta = null;
        if(request.getTipKapitala() == KapitalType.NOVAC) {
            valuta = valutaRepository.findValutaByKodValute(request.getValutaOznaka());
            if(valuta == null) {
                log.error("dodajTransakciju: failed to get valuta");
            }

            SredstvaKapital sredstvaKapital = sredstvaKapitalRepository.findByRacunAndValuta(racun, valuta);
            if (sredstvaKapital == null) {
                sredstvaKapitalService.pocetnoStanje(racun.getBrojRacuna(), request.getValutaOznaka(), 0);
            }
        } else if(request.getTipKapitala() != KapitalType.MARGIN) {
            SredstvaKapital sredstvaKapital = sredstvaKapitalRepository.findByRacunAndHaritja(racun, request.getTipKapitala(), request.getHartijaId());
            if (sredstvaKapital == null) {
                sredstvaKapitalService.pocetnoStanje(racun.getBrojRacuna(), request.getTipKapitala(), request.getHartijaId(), 0);
            }
        }

        // Preuzmi i ZAKLJUCAJ sredstva za zeljenu hartiju
        Query query;
        SredstvaKapital sredstvaKapital = null;
        if(request.getTipKapitala() != KapitalType.MARGIN) {
            if (request.getTipKapitala() == KapitalType.NOVAC) {
                query = entityManager.createQuery("from SredstvaKapital where racun = :racun and valuta = :valuta and kapitalType = rs.edu.raf.banka.racun.enums.KapitalType.NOVAC");
                query.setParameter("valuta", valuta);
            } else {
                query = entityManager.createQuery("from SredstvaKapital where racun = :racun and haritjeOdVrednostiID = :hartijaId and kapitalType = :kapitalType");
                query.setParameter("hartijaId", request.getHartijaId());
                query.setParameter("kapitalType", request.getTipKapitala());
            }
            query.setParameter("racun", racun);
            query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            List<SredstvaKapital> skList = query.getResultList();
            if (skList.size() != 1) {
                log.error("dodajTransakciju: unable to find sredstvaKapital for {} and hartija", racun.getBrojRacuna().toString());
                return null;
            }
            sredstvaKapital = skList.get(0);
        }

        // Preuzmi i ZAKLJUCAJ sredstva za Margins racun
        Query qryMarginsSredstva;
        qryMarginsSredstva = entityManager.createQuery("from SredstvaKapital where racun = :racun and kapitalType = rs.edu.raf.banka.racun.enums.KapitalType.MARGIN");
        qryMarginsSredstva.setParameter("racun", racun);
        List<SredstvaKapital> skMarginList = qryMarginsSredstva.getResultList();
        if(skMarginList.size() != 1) {
            log.error("dodajTransakciju: unable to find sredstvaKapital for {}", racun.getBrojRacuna().toString());
            return null;
        }

        SredstvaKapital sredstvaKapitalMargin = skMarginList.get(0);

        // Azuriraj sredstva i kapital sa novim stanjem
        if(request.getTipKapitala() != KapitalType.MARGIN) {
            if (request.getTipTranskacije() == MarginTransakcijaType.UPLATA) {
                // Uplata, tj. kupvoina hartije od vrednosti
                double hartijaNovoUkupno = sredstvaKapital.getUkupno() + request.getKolicina();

                sredstvaKapital.setUkupno(hartijaNovoUkupno);
                sredstvaKapital.setRaspolozivo(hartijaNovoUkupno);
                sredstvaKapital.setKreditnaSredstva(sredstvaKapital.getKreditnaSredstva() + request.getKredit());
                sredstvaKapital.setMaintenanceMargin(sredstvaKapital.getMaintenanceMargin() + request.getMaintenanceMargin());

                sredstvaKapitalMargin.setUkupno(sredstvaKapitalMargin.getUkupno() + request.getIznos());
                sredstvaKapitalMargin.setRaspolozivo(sredstvaKapitalMargin.getUkupno());
                sredstvaKapitalMargin.setKreditnaSredstva(sredstvaKapitalMargin.getKreditnaSredstva() + request.getKredit());
                sredstvaKapitalMargin.setMaintenanceMargin(sredstvaKapitalMargin.getMaintenanceMargin() + request.getMaintenanceMargin());
            } else if (request.getTipTranskacije() == MarginTransakcijaType.ISPLATA) {
                double hartijaNovoUkupno = sredstvaKapital.getUkupno() - request.getKolicina();
                if (hartijaNovoUkupno < 0) {
                    log.error("dodajTransakciju: novo ukupno is < 0 ({})", hartijaNovoUkupno);
                    return null;
                }

                sredstvaKapital.setUkupno(hartijaNovoUkupno);
                sredstvaKapital.setRaspolozivo(hartijaNovoUkupno);

                if (request.getIznos() >= sredstvaKapital.getKreditnaSredstva()) {
                    Double stariKredit = sredstvaKapital.getKreditnaSredstva();
                    sredstvaKapital.setKreditnaSredstva(0.0);

                    Double stariMMR = sredstvaKapital.getMaintenanceMargin();
                    sredstvaKapital.setMaintenanceMargin(0.0);

                    double razlika = request.getIznos() - stariKredit;
                    // Od margine oduzimam ono sto sam otplatio
                    sredstvaKapitalMargin.setKreditnaSredstva(sredstvaKapitalMargin.getKreditnaSredstva() - stariKredit);
                    sredstvaKapitalMargin.setUkupno(sredstvaKapitalMargin.getUkupno() + razlika);
                    sredstvaKapitalMargin.setRaspolozivo(sredstvaKapitalMargin.getUkupno());
                    sredstvaKapitalMargin.setMaintenanceMargin(sredstvaKapitalMargin.getMaintenanceMargin() - stariMMR);
                } else {
                    // Aproksimacija: maintenance margin umanjujem za procenat kapitala koji sam prodao.
                    double procent = request.getIznos() / sredstvaKapital.getKreditnaSredstva();
                    double noviMaintenanceMargin = sredstvaKapital.getMaintenanceMargin() * procent;
                    double razlikaMaintenanceMargin = sredstvaKapital.getMaintenanceMargin() - noviMaintenanceMargin;
                    sredstvaKapital.setMaintenanceMargin(noviMaintenanceMargin);
                    sredstvaKapitalMargin.setMaintenanceMargin(sredstvaKapitalMargin.getMaintenanceMargin() - razlikaMaintenanceMargin);

                    sredstvaKapital.setKreditnaSredstva(sredstvaKapital.getKreditnaSredstva() - request.getIznos());
                    sredstvaKapitalMargin.setKreditnaSredstva(sredstvaKapitalMargin.getKreditnaSredstva() - request.getIznos());
                }
            }
        } else {
            if (request.getTipTranskacije() == MarginTransakcijaType.UPLATA) {
                sredstvaKapitalMargin.setUkupno(sredstvaKapitalMargin.getUkupno() + request.getIznos());
            } else {
                sredstvaKapitalMargin.setUkupno(sredstvaKapitalMargin.getUkupno() - request.getIznos());
            }
            sredstvaKapitalMargin.setRaspolozivo(sredstvaKapitalMargin.getUkupno());
        }

        // Pravljenje margins transakcije
        MarginTransakcija mt = new MarginTransakcija();
        mt.setTip(request.getTipTranskacije());
        mt.setRacun(racun);
        mt.setUsername(username);
        mt.setKapitalType(request.getTipKapitala());
        if(request.getTipKapitala() == KapitalType.NOVAC) {
            mt.setHaritjeOdVrednostiID(valuta.getId());
        } else {
            mt.setHaritjeOdVrednostiID(request.getHartijaId());
        }
        mt.setOrderId(request.getOrderId());
        mt.setDatumVreme(new Date());
        mt.setOpis(request.getOpis());
        mt.setIznos(request.getIznos());
        mt.setIznosKredita(request.getKredit());
        mt.setMaintenanceMargin(request.getMaintenanceMargin());
        mt.setKolicina(request.getKolicina());
        mt.setUnitPrice(request.getUnitPrice());

        // Pravljenje transakcije za povlacenje uloga sa kes racuna
        if(request.getTipTranskacije() == MarginTransakcijaType.UPLATA) {
            TransakcijaRequest tr = new TransakcijaRequest();
            tr.setOpis("Pokrivanje inicijalne margine");
            tr.setType(KapitalType.NOVAC);
            tr.setValutaOznaka(sredstvaKapitalMargin.getValuta().getKodValute());
            tr.setUplata(0.0);
            tr.setIsplata(mt.getIznos());
            tr.setRezervisano(0.0);
            tr.setMargins(false);
            tr.setOrderId(null);
            tr.setUsername(request.getUsername());

            Transakcija transakcija = transakcijaService.dodajTransakciju(token, tr);
            if (transakcija == null) {
                log.error("dodajTransakciju: failed to add transaction for ulog");
                return null;
            }
        } else if (request.getTipTranskacije() == MarginTransakcijaType.ISPLATA && request.getTipKapitala() == KapitalType.MARGIN) {
            TransakcijaRequest tr = new TransakcijaRequest();
            tr.setOpis("Povlacenje sredstava sa marznog racuna");
            tr.setType(KapitalType.NOVAC);
            tr.setValutaOznaka(sredstvaKapitalMargin.getValuta().getKodValute());
            tr.setUplata(mt.getIznos());
            tr.setIsplata(0.0);
            tr.setRezervisano(0.0);
            tr.setMargins(false);
            tr.setOrderId(null);
            tr.setUsername(request.getUsername());

            Transakcija transakcija = transakcijaService.dodajTransakciju(token, tr);
            if (transakcija == null) {
                log.error("dodajTransakciju: failed to add transaction for ulog");
                return null;
            }
        }

        // Cuvanje podataka
        mt = marginTransakcijaRepository.save(mt);
        if(sredstvaKapital != null) {
            sredstvaKapitalRepository.save(sredstvaKapital);
        }
        sredstvaKapitalRepository.save(sredstvaKapitalMargin);

        return mt;
    }
}
