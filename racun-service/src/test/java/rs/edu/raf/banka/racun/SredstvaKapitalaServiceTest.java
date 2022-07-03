package rs.edu.raf.banka.racun;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
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
import rs.edu.raf.banka.racun.service.impl.SredstvaKapitalService;
import rs.edu.raf.banka.racun.service.impl.UserService;
import rs.edu.raf.banka.racun.utils.HttpUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SredstvaKapitalaServiceTest {
    @Spy
    @InjectMocks
    private SredstvaKapitalService sredstvaKapitalService;

    @Mock
    private UserService userService;

    @Mock
    private RacunRepository racunRepository;

    @Mock
    private MarginTransakcijaRepository marginTransakcijaRepository;

    @Mock
    private ValutaRepository valutaRepository;

    @Mock
    private TransakcijaRepository transakcijaRepository;

    @Mock
    private SredstvaKapitalRepository sredstvaKapitalRepository;

    @Test
    void testPocetnoStanje() {
        Racun r = new Racun();
        r.setBrojRacuna(UUID.randomUUID());

        SredstvaKapital sredstvaKapital = new SredstvaKapital();
        sredstvaKapital.setUkupno(1000);

        given(racunRepository.findByBrojRacuna(any())).willReturn(r);
        given(valutaRepository.findValutaByKodValute("RSD")).willReturn(new Valuta());
        given(sredstvaKapitalRepository.save(any())).willReturn(sredstvaKapital);

        assertEquals(sredstvaKapitalService.pocetnoStanje(r.getBrojRacuna(),"RSD",1000).getUkupno(),1000);
    }

    @Test
    void testPocetnoStanje2() {
        Racun r = new Racun();
        r.setBrojRacuna(UUID.randomUUID());

        SredstvaKapital sredstvaKapital = new SredstvaKapital();
        sredstvaKapital.setUkupno(1000);

        given(racunRepository.findByBrojRacuna(any())).willReturn(r);
        given(sredstvaKapitalRepository.save(any())).willReturn(sredstvaKapital);

        assertEquals(sredstvaKapitalService.pocetnoStanje(r.getBrojRacuna(),sredstvaKapital.getKapitalType(),1L,1000).getUkupno(),1000);
    }

    @Test
    void testPocetnoStanjeMargin() {
        Racun r = new Racun();
        r.setBrojRacuna(UUID.randomUUID());

        SredstvaKapital sredstvaKapital = new SredstvaKapital();
        sredstvaKapital.setUkupno(1000);

        given(racunRepository.findByBrojRacuna(any())).willReturn(r);
        given(valutaRepository.findValutaByKodValute("USD")).willReturn(new Valuta());
        given(sredstvaKapitalRepository.save(any())).willReturn(sredstvaKapital);

        assertDoesNotThrow(() -> sredstvaKapitalService.pocetnoStanjeMarzniRacun(r.getBrojRacuna()));
    }


    @Test
    void testGetUkupnoStanjePoHartijama() {
        List<SredstvaKapital> sredstvaKapitals = new ArrayList<>();
        SredstvaKapital sredstvaKapital = new SredstvaKapital();
        sredstvaKapital.setUkupno(1000);
        sredstvaKapital.setRaspolozivo(1.0);
        sredstvaKapital.setKapitalType(KapitalType.AKCIJA);
        sredstvaKapitals.add(sredstvaKapital);
        sredstvaKapitals.add(sredstvaKapital);

        SredstvaKapital sredstvaKapital2 = new SredstvaKapital();
        sredstvaKapital2.setUkupno(1000);
        sredstvaKapital2.setRaspolozivo(1.0);
        sredstvaKapital2.setKapitalType(KapitalType.FUTURE_UGOVOR);
        sredstvaKapitals.add(sredstvaKapital2);


        when(sredstvaKapitalRepository.findAllByRacun(any())).thenReturn(sredstvaKapitals);
        List<KapitalHartijeDto> toReturn = new ArrayList<>();
        KapitalHartijeDto khdAkcija = new KapitalHartijeDto(KapitalType.AKCIJA, 2.0);
        KapitalHartijeDto khdFuture = new KapitalHartijeDto(KapitalType.FUTURE_UGOVOR, 1.0);
        toReturn.add(khdAkcija);
        toReturn.add(khdFuture);

        try (MockedStatic<HttpUtils> utilities = Mockito.mockStatic(HttpUtils.class)){
            var akcija = new AkcijePodaciDto();
            akcija.setId(1L);
            akcija.setPrice(1.0);
            var berza = new BerzaDto();
            berza.setKodValute("USD");
            var berza2 = new BerzaDto();
            berza2.setKodValute("RSD");
            var forex = new ForexPodaciDto();
            forex.setExchangeRate(1.0);
            var future = new FuturesPodaciDto();
            future.setOpen(1.0);
            utilities.when(() -> HttpUtils.getAkcijeById(any(), any()))
                    .thenReturn(ResponseEntity.ok(akcija));
            utilities.when(() -> HttpUtils.getBerzaById(any(), any()))
                    .thenReturn(ResponseEntity.ok(berza))
                    .thenReturn(ResponseEntity.ok(berza2));
            utilities.when(() -> HttpUtils.getExchangeRate(any(), any(),any(),any()))
                    .thenReturn(ResponseEntity.ok(forex));
            utilities.when(() -> HttpUtils.getFuturesById(any(), any()))
                    .thenReturn(ResponseEntity.ok(future));

            assertEquals(sredstvaKapitalService.getUkupnoStanjePoHartijama("", false),toReturn);
            assertEquals(sredstvaKapitalService.getUkupnoStanjePoHartijama("", true),toReturn);
        }

    }

    @Test
    void testGetStanjeJednogTipaHartijeFture() {

        List<SredstvaKapital> sredstvaKapitals = new ArrayList<>();
        SredstvaKapital sredstvaKapital = new SredstvaKapital();
        sredstvaKapital.setUkupno(1000);
        sredstvaKapital.setRaspolozivo(1.0);
        sredstvaKapital.setKapitalType(KapitalType.AKCIJA);
        sredstvaKapitals.add(sredstvaKapital);

        SredstvaKapital sredstvaKapital2 = new SredstvaKapital();
        sredstvaKapital2.setUkupno(1000);
        sredstvaKapital2.setRaspolozivo(1.0);
        sredstvaKapital2.setKapitalType(KapitalType.FUTURE_UGOVOR);
        sredstvaKapitals.add(sredstvaKapital2);

        SredstvaKapital sredstvaKapital3 = new SredstvaKapital();
        sredstvaKapital3.setKapitalType(KapitalType.NOVAC);
        sredstvaKapitals.add(sredstvaKapital3);

        when(sredstvaKapitalRepository.findAllByRacun(any())).thenReturn(sredstvaKapitals);
        when(racunRepository.findRacunByTipRacuna(any())).thenReturn(new Racun());

        KapitalPoTipuHartijeDto toReturn1 = new KapitalPoTipuHartijeDto();
        toReturn1.setBerza("EUREX");
        toReturn1.setKolicinaUVlasnistvu(1000L);
        toReturn1.setCena(1.0);
        toReturn1.setVrednostRSD(1000.0);
        toReturn1.setVrednost(1000.0);
        toReturn1.setKupljenoZa(0.0);
        toReturn1.setProfit(1000.0);
        toReturn1.setKodValute("USD");

        var toReturn1List = new ArrayList<>();
        toReturn1List.add(toReturn1);


        try (MockedStatic<HttpUtils> utilities = Mockito.mockStatic(HttpUtils.class)){
            var akcija = new AkcijePodaciDto();
            akcija.setId(1L);
            akcija.setPrice(1.0);
            var berza = new BerzaDto();
            berza.setKodValute("USD");
            var forex = new ForexPodaciDto();
            forex.setExchangeRate(1.0);
            var future = new FuturesPodaciDto();
            future.setOpen(1.0);
            utilities.when(() -> HttpUtils.getAkcijeById(any(), any()))
                    .thenReturn(ResponseEntity.ok(akcija));
            utilities.when(() -> HttpUtils.getBerzaById(any(), any()))
                    .thenReturn(ResponseEntity.ok(berza));
            utilities.when(() -> HttpUtils.getExchangeRate(any(), any(),any(),any()))
                    .thenReturn(ResponseEntity.ok(forex));
            utilities.when(() -> HttpUtils.getFuturesById(any(), any()))
                    .thenReturn(ResponseEntity.ok(future));



            assertEquals(sredstvaKapitalService.getStanjeJednogTipaHartije("","FUTURE_UGOVOR", false),toReturn1List);
            //assertEquals(sredstvaKapitalService.getStanjeJednogTipaHartije("","AKCIJA", false),toReturn);
        }
    }

    @Test
    void testGetStanjeJednogTipaHartijeAkcija() {

        List<SredstvaKapital> sredstvaKapitals = new ArrayList<>();
        SredstvaKapital sredstvaKapital = new SredstvaKapital();
        sredstvaKapital.setUkupno(1000);
        sredstvaKapital.setRaspolozivo(1.0);
        sredstvaKapital.setKapitalType(KapitalType.AKCIJA);
        sredstvaKapitals.add(sredstvaKapital);

        SredstvaKapital sredstvaKapital2 = new SredstvaKapital();
        sredstvaKapital2.setUkupno(1000);
        sredstvaKapital2.setRaspolozivo(1.0);
        sredstvaKapital2.setKapitalType(KapitalType.FUTURE_UGOVOR);
        sredstvaKapitals.add(sredstvaKapital2);

        SredstvaKapital sredstvaKapital3 = new SredstvaKapital();
        sredstvaKapital3.setKapitalType(KapitalType.NOVAC);
        sredstvaKapitals.add(sredstvaKapital3);

        when(sredstvaKapitalRepository.findAllByRacun(any())).thenReturn(sredstvaKapitals);
        when(racunRepository.findRacunByTipRacuna(any())).thenReturn(new Racun());

        KapitalPoTipuHartijeDto toReturn1 = new KapitalPoTipuHartijeDto();
        toReturn1.setId(1L);
        toReturn1.setKolicinaUVlasnistvu(1000L);
        toReturn1.setCena(1.0);
        toReturn1.setVrednostRSD(1000.0);
        toReturn1.setVrednost(1000.0);
        toReturn1.setKupljenoZa(0.0);
        toReturn1.setProfit(1000.0);
        toReturn1.setKodValute("USD");

        var toReturn1List = new ArrayList<>();
        toReturn1List.add(toReturn1);


        try (MockedStatic<HttpUtils> utilities = Mockito.mockStatic(HttpUtils.class)){
            var akcija = new AkcijePodaciDto();
            akcija.setId(1L);
            akcija.setPrice(1.0);
            var berza = new BerzaDto();
            berza.setKodValute("USD");
            var forex = new ForexPodaciDto();
            forex.setExchangeRate(1.0);
            var future = new FuturesPodaciDto();
            future.setOpen(1.0);
            utilities.when(() -> HttpUtils.getAkcijeById(any(), any()))
                    .thenReturn(ResponseEntity.ok(akcija));
            utilities.when(() -> HttpUtils.getBerzaById(any(), any()))
                    .thenReturn(ResponseEntity.ok(berza));
            utilities.when(() -> HttpUtils.getExchangeRate(any(), any(),any(),any()))
                    .thenReturn(ResponseEntity.ok(forex));
            utilities.when(() -> HttpUtils.getFuturesById(any(), any()))
                    .thenReturn(ResponseEntity.ok(future));


            assertEquals(sredstvaKapitalService.getStanjeJednogTipaHartije("","AKCIJA", false),toReturn1List);
        }
    }


    @Test
    void testGetAll() {
        Racun r = new Racun();
        r.setBrojRacuna(UUID.randomUUID());
        List<SredstvaKapital> sredstvaKapitalList = new ArrayList<>();
        SredstvaKapital sredstvaKapital = new SredstvaKapital();
        sredstvaKapital.setKapitalType(KapitalType.NOVAC);
        sredstvaKapitalList.add(sredstvaKapital);
        given(sredstvaKapitalRepository.findAllByRacun(any())).willReturn(sredstvaKapitalList);

        assertEquals(sredstvaKapitalService.getAll(r.getBrojRacuna()),sredstvaKapitalList);
    }

    @Test
    void testGetTransakcijeHartijeUplata() {
        Racun racun = new Racun();
        racun.setBrojRacuna(UUID.randomUUID());
        racun.setTipRacuna(RacunType.KES);
        when(racunRepository.findRacunByTipRacuna(RacunType.KES)).thenReturn(racun);
        List<Transakcija> transakcijaList = new ArrayList<>();
        List<TransakcijeHartijeDto> toReturn = new ArrayList<>();
        Transakcija transakcija = new Transakcija();
        transakcija.setUplata(100);
        transakcijaList.add(transakcija);
        TransakcijeHartijeDto transakcijeHartijeDto = new TransakcijeHartijeDto();
        transakcijeHartijeDto.setDatum(transakcija.getDatumVreme());
        transakcijeHartijeDto.setTipOrdera("Kupovina");
        transakcijeHartijeDto.setKolicina((long) transakcija.getUplata());
        when(transakcijaRepository.findByHaritjeOdVrednostiIDAndKapitalTypeAndRacun(1L, KapitalType.NOVAC, racun)).thenReturn(transakcijaList);
        transakcijeHartijeDto.setCena(transakcija.getUnitPrice());
        transakcijeHartijeDto.setUkupno(transakcija.getUnitPrice()*transakcijeHartijeDto.getKolicina());
        toReturn.add(transakcijeHartijeDto);
        assertEquals(sredstvaKapitalService.getTransakcijeHartijeKes(1L,KapitalType.NOVAC.toString()),toReturn);
    }


    @Test
    void testGetTransakcijeHartijeIsplata() {
        Racun racun = new Racun();
        racun.setBrojRacuna(UUID.randomUUID());
        racun.setTipRacuna(RacunType.KES);
        when(racunRepository.findRacunByTipRacuna(RacunType.KES)).thenReturn(racun);
        List<Transakcija> transakcijaList = new ArrayList<>();
        List<TransakcijeHartijeDto> toReturn = new ArrayList<>();
        Transakcija transakcija = new Transakcija();
        transakcija.setIsplata(100);
        transakcijaList.add(transakcija);
        TransakcijeHartijeDto transakcijeHartijeDto = new TransakcijeHartijeDto();
        transakcijeHartijeDto.setDatum(transakcija.getDatumVreme());
        transakcijeHartijeDto.setTipOrdera("Prodaja");
        transakcijeHartijeDto.setKolicina((long) transakcija.getIsplata());
        when(transakcijaRepository.findByHaritjeOdVrednostiIDAndKapitalTypeAndRacun(1L, KapitalType.NOVAC, racun)).thenReturn(transakcijaList);
        transakcijeHartijeDto.setCena(transakcija.getUnitPrice());
        transakcijeHartijeDto.setUkupno(transakcija.getUnitPrice()*transakcijeHartijeDto.getKolicina());
        toReturn.add(transakcijeHartijeDto);
        assertEquals(sredstvaKapitalService.getTransakcijeHartijeKes(1L,KapitalType.NOVAC.toString()),toReturn);
    }

    @Test
    void testFindSredstvaKapitalSupervisor() {
        Racun racun = new Racun();
        racun.setBrojRacuna(UUID.randomUUID());
        racun.setTipRacuna(RacunType.KES);
        when(racunRepository.findRacunByTipRacuna(RacunType.KES)).thenReturn(racun);
        when(userService.getRoleByToken(initValidJWT())).thenReturn("ROLE_ADMIN");
        List<SredstvaKapital> sredstvaKapitals = new ArrayList<>();
        List<SupervisorSredstvaKapitalDto> sredstvaKapitalDtos = new ArrayList<>();
        SredstvaKapital sredstvaKapital = new SredstvaKapital();
        sredstvaKapital.setKapitalType(KapitalType.NOVAC);
        sredstvaKapital.setValuta(new Valuta());
        sredstvaKapitals.add(sredstvaKapital);
        SupervisorSredstvaKapitalDto s = new SupervisorSredstvaKapitalDto();
        s.setKodValute(sredstvaKapital.getValuta().getKodValute());
        s.setUkupno(sredstvaKapital.getUkupno());
        s.setRezervisano(sredstvaKapital.getRezervisano());
        s.setRaspolozivo(sredstvaKapital.getRaspolozivo());
        s.setKredit(0.0);
        s.setMaintenanceMargin(0.0);
        s.setMarginCall(false);
        sredstvaKapitalDtos.add(s);
        when(sredstvaKapitalRepository.findAllByRacun(racun)).thenReturn(sredstvaKapitals);
        assertEquals(sredstvaKapitalService.findSredstvaKapitalSupervisor(initValidJWT(), false), sredstvaKapitalDtos);
    }

    @Test
    void testFindSredstvaKapitalNoNovacSupervisor() {
        when(userService.getRoleByToken(initValidJWT())).thenReturn("ROLE_ADMIN");
        List<SredstvaKapital> sredstvaKapitals = new ArrayList<>();
        List<SupervisorSredstvaKapitalDto> sredstvaKapitalDtos = new ArrayList<>();
        when(sredstvaKapitalRepository.findAllByRacun(any())).thenReturn(sredstvaKapitals);
        assertEquals(sredstvaKapitalService.findSredstvaKapitalSupervisor(initValidJWT(), false),sredstvaKapitalDtos);
    }

    @Test
    void testFindSredstvaKapitalNoNovacSupervisorRoleNull() {
        when(userService.getRoleByToken(initValidJWT())).thenReturn("ROLE_AGENT");
        List<SredstvaKapital> sredstvaKapitals = new ArrayList<>();
        assertEquals(sredstvaKapitalService.findSredstvaKapitalSupervisor(initValidJWT(), false),null);
    }

    @Test
    void testFindSredstvaKapitalAgent() {
        String token = initValidJWT();
        when(userService.getRoleByToken(anyString())).thenReturn("ROLE_AGENT");
        UserDto userDto = new UserDto();
        userDto.setLimit(100.0);
        userDto.setLimitUsed(50.0);
        when(userService.getUserByToken(anyString())).thenReturn(userDto);
        AgentSredstvaKapitalDto agentSredstvaKapitalDto = new AgentSredstvaKapitalDto();
        agentSredstvaKapitalDto.setLimit(100.0);
        agentSredstvaKapitalDto.setLimitUsed(50.0);
        agentSredstvaKapitalDto.setRaspolozivoAgentu(agentSredstvaKapitalDto.getLimit() - agentSredstvaKapitalDto.getLimitUsed());
        assertEquals(sredstvaKapitalService.findSredstvaKapitalAgent(token),agentSredstvaKapitalDto);
    }

    @Test
    void testFindSredstvaKapitalAgentNull() {
        String token = initValidJWT();
        when(userService.getRoleByToken(anyString())).thenReturn("ROLE_ADMIN");
        assertEquals(sredstvaKapitalService.findSredstvaKapitalAgent(token),null);
    }

    @Test
    void testGetTransakcijeHartijeMargins() {

        var transakcije = new ArrayList<MarginTransakcija>();
        var transakcija = new MarginTransakcija();
        transakcija.setTip(MarginTransakcijaType.UPLATA);
        transakcije.add(transakcija);

        var transakcija2 = new MarginTransakcija();
        transakcija2.setTip(MarginTransakcijaType.ISPLATA);
        transakcije.add(transakcija2);

        var transakcijeDto = new ArrayList<MarginTransakcijeHartijeDto>();
        var transakcijaDto = new MarginTransakcijeHartijeDto();
        transakcijaDto.setTipOrdera("Kupovina");
        transakcijaDto.setCena(0.0);
        transakcijaDto.setKolicina(0L);
        transakcijaDto.setUkupno(0.0);
        transakcijeDto.add(transakcijaDto);

        var transakcijaDto2 = new MarginTransakcijeHartijeDto();
        transakcijaDto2.setTipOrdera("Prodaja");
        transakcijaDto2.setCena(0.0);
        transakcijaDto2.setKolicina(0L);
        transakcijaDto2.setUkupno(0.0);
        transakcijeDto.add(transakcijaDto2);

        when(racunRepository.findRacunByTipRacuna(any())).thenReturn(new Racun());
        when(marginTransakcijaRepository.findByHaritjeOdVrednostiIDAndKapitalTypeAndRacun(any(),any(),any())).thenReturn(transakcije);

        assertEquals(sredstvaKapitalService.getTransakcijeHartijeMargins(1L,"FOREX"),transakcijeDto);
    }



    String initValidJWT() {
        return "Bearer " + JWT.create()
                .withSubject("dummyName" + ",ADMIN_ROLE")
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"CREATE_USER", "LIST_USERS", "EDIT_USER", "MY_EDIT", "DELETE_USER"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));
    }



}
