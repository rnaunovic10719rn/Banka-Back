package rs.edu.raf.banka.racun;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import rs.edu.raf.banka.racun.enums.KapitalType;
import rs.edu.raf.banka.racun.enums.MarginTransakcijaType;
import rs.edu.raf.banka.racun.enums.RacunType;
import rs.edu.raf.banka.racun.model.Racun;
import rs.edu.raf.banka.racun.model.SredstvaKapital;
import rs.edu.raf.banka.racun.model.Transakcija;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.model.margins.MarginTransakcija;
import rs.edu.raf.banka.racun.repository.*;
import rs.edu.raf.banka.racun.requests.MarginTransakcijaRequest;
import rs.edu.raf.banka.racun.requests.TransakcijaRequest;
import rs.edu.raf.banka.racun.service.impl.MarginTransakcijaService;
import rs.edu.raf.banka.racun.service.impl.SredstvaKapitalService;
import rs.edu.raf.banka.racun.service.impl.TransakcijaService;
import rs.edu.raf.banka.racun.service.impl.UserService;
import rs.edu.raf.banka.racun.utils.HttpUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class MarginTransakcijeServiceTest {

    @InjectMocks
    MarginTransakcijaService marginTransakcijaService;

    @Mock
    SredstvaKapitalService sredstvaKapitalService;

    @Spy
    HttpUtils httpUtils;

    @Mock
    RacunRepository racunRepository;

    @Mock
    ValutaRepository valutaRepository;

    @Mock
    SredstvaKapitalRepository sredstvaKapitalRepository;

    @Mock
    TransakcijaRepository transakcijaRepository;

    @Mock
    MarginTransakcijaRepository marginTransakcijaRepository;

    @Mock
    EntityManager entityManager;

    @Mock
    UserService userService;

    @Spy
    RestTemplate restTemplate = new RestTemplate();

    @Mock
    TransakcijaService transakcijaService;

    MarginTransakcijaRequest transakcijaRequest = initTransakcijaRequest();

    String validJWToken = initValidJWT();
    String invalidJWToken = initInvalidJWT();

    String dummyName = "Mock";
    UUID mockRacun = UUID.randomUUID();
    String mockValuta = "RSD";

    @Value("${racun.user-service-url}")
    private String USER_SERVICE_URL;


    @Test
    void tesTgetAll1() {
        String token = initValidJWT();
        when(userService.getRoleByToken(anyString())).thenReturn("ROLE_AGENT");
        when(userService.getUsernameByToken(anyString())).thenReturn("mockUsername");
        List<MarginTransakcija> transakcijaList = new ArrayList<>();
        when(marginTransakcijaRepository.findByUsername(anyString())).thenReturn(transakcijaList);

        assertEquals(marginTransakcijaService.getAll(token),transakcijaList);
    }

    @Test
    void tesTgetAll12() {
        String token = initValidJWT();
        when(userService.getRoleByToken(anyString())).thenReturn("ROLE_ADMIN");
        when(userService.getUsernameByToken(anyString())).thenReturn("mockUsername");
        List<MarginTransakcija> transakcijaList = new ArrayList<>();
        when(marginTransakcijaRepository.getAll()).thenReturn(transakcijaList);

        assertEquals(marginTransakcijaService.getAll(token),transakcijaList);
    }

    @Test
    void tesTgetAll2() {
        String token = initValidJWT();
        when(userService.getRoleByToken(anyString())).thenReturn("ROLE_ADMIN");
        when(userService.getUsernameByToken(anyString())).thenReturn("mockUsername");
        List<MarginTransakcija> transakcijaList = new ArrayList<>();
        when(marginTransakcijaRepository.getAll(any(),any())).thenReturn(transakcijaList);

        assertEquals(marginTransakcijaService.getAll(token,new Date(),new Date()),transakcijaList);
    }

    @Test
    void tesTgetAll21() {
        String token = initValidJWT();
        when(userService.getRoleByToken(anyString())).thenReturn("ROLE_AGENT");
        when(userService.getUsernameByToken(anyString())).thenReturn("mockUsername");
        List<MarginTransakcija> transakcijaList = new ArrayList<>();
        when(marginTransakcijaRepository.findByUsername(anyString(),any(),any())).thenReturn(transakcijaList);

        assertEquals(marginTransakcijaService.getAll(token,new Date(),new Date()),transakcijaList);
    }


    @Test
    void testDodavanjeTransakcije() throws NoSuchFieldException {

        Valuta v = new Valuta();
        v.setId(1L);
        v.setKodValute("RSD");
        MarginTransakcija t = new MarginTransakcija();

        Racun r = new Racun();
        r.setBrojRacuna(mockRacun);
        SredstvaKapital sredstvaKapital = new SredstvaKapital();
        sredstvaKapital.setUkupno(1000);
        sredstvaKapital.setMaintenanceMargin(1.0);
        sredstvaKapital.setKreditnaSredstva(1.0);
        sredstvaKapital.setValuta(v);

        when(valutaRepository.findValutaByKodValute("RSD")).thenReturn(v);

        Query query = mock(Query.class);

        when(racunRepository.findRacunByTipRacuna(RacunType.MARGINS_RACUN)).thenReturn(r);

        given(entityManager.createQuery(anyString())).willReturn(query);

        List<SredstvaKapital> skList = new ArrayList<>();
        skList.add(sredstvaKapital);

        given(query.getResultList()).willReturn(skList);


        when(marginTransakcijaRepository.save(any())).thenReturn(t);
        when(sredstvaKapitalRepository.save(any())).thenReturn(sredstvaKapital);

        when(transakcijaService.dodajTransakciju(any(), any())).thenReturn(new Transakcija());

        assertEquals(marginTransakcijaService.dodajTransakciju("Bearer " + validJWToken, transakcijaRequest),t);
    }



    private MarginTransakcijaRequest initTransakcijaRequest() {
        MarginTransakcijaRequest tr = new MarginTransakcijaRequest();
        tr.setBrojRacuna(mockRacun);
        tr.setOpis("mockOpis");
        tr.setValutaOznaka(mockValuta);
        tr.setOrderId(1L);
        tr.setIznos(10000);
        tr.setKredit(10000);
        tr.setKolicina(1.0);
        tr.setTipKapitala(KapitalType.NOVAC);
        tr.setTipTransakcije(MarginTransakcijaType.UPLATA);
        tr.setUnitPrice(1.0);
        tr.setValutaOznaka("RSD");
        return tr;
    }

    String initValidJWT() {
        return JWT.create()
                .withSubject(dummyName + ",ADMIN_ROLE")
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"CREATE_USER", "LIST_USERS", "EDIT_USER", "MY_EDIT", "DELETE_USER"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));
    }

    String initInvalidJWT() {
        return JWT.create()
                .withSubject(dummyName + ",ROLE")
                .withIssuer("mock")
                .withClaim("permissions", Arrays.asList(new String[]{"X_LIST_USERS", "DUMMY_FAKE_PERMISSION"}))
                .sign(Algorithm.HMAC256("secret".getBytes()));
    }

}
