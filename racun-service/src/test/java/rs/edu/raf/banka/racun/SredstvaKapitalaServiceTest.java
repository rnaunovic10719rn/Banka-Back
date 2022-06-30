package rs.edu.raf.banka.racun;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.edu.raf.banka.racun.dto.*;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.enums.RacunType;
import rs.edu.raf.banka.racun.model.Racun;
import rs.edu.raf.banka.racun.model.SredstvaKapital;
import rs.edu.raf.banka.racun.model.Transakcija;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.repository.RacunRepository;
import rs.edu.raf.banka.racun.repository.SredstvaKapitalRepository;
import rs.edu.raf.banka.racun.repository.TransakcijaRepository;
import rs.edu.raf.banka.racun.repository.ValutaRepository;
import rs.edu.raf.banka.racun.service.impl.SredstvaKapitalService;
import rs.edu.raf.banka.racun.service.impl.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void testGetUkupnoStanjePoHartijama() {
        List<SredstvaKapital> sredstvaKapitals = new ArrayList<>();

        when(sredstvaKapitalRepository.findAllByRacun(any())).thenReturn(sredstvaKapitals);
        List<KapitalHartijeDto> toReturn = new ArrayList<>();
        KapitalHartijeDto khdAkcija = new KapitalHartijeDto(KapitalType.AKCIJA, 0.0);
        KapitalHartijeDto khdFuture = new KapitalHartijeDto(KapitalType.FUTURE_UGOVOR, 0.0);
        toReturn.add(khdAkcija);
        toReturn.add(khdFuture);

        assertEquals(sredstvaKapitalService.getUkupnoStanjePoHartijama("", false),toReturn);
    }

    @Test
    void testGetStanjeJednogTipaHartije() {
        List<SredstvaKapital> sredstvaKapitals = sredstvaKapitalRepository.findAll();
        List<KapitalPoTipuHartijeDto> toReturn = new ArrayList<>();

        assertEquals(sredstvaKapitalService.getStanjeJednogTipaHartije("","", false),toReturn);
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





    String initValidJWT() {
        return "Bearer " + JWT.create()
                .withSubject("dummyName" + ",ADMIN_ROLE")
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"CREATE_USER", "LIST_USERS", "EDIT_USER", "MY_EDIT", "DELETE_USER"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));
    }



}
