package rs.edu.raf.banka.racun;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;


import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import rs.edu.raf.banka.racun.model.Racun;
import rs.edu.raf.banka.racun.model.SredstvaKapital;
import rs.edu.raf.banka.racun.model.Transakcija;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.repository.RacunRepository;
import rs.edu.raf.banka.racun.repository.SredstvaKapitalRepository;
import rs.edu.raf.banka.racun.repository.TransakcijaRepository;
import rs.edu.raf.banka.racun.repository.ValutaRepository;
import rs.edu.raf.banka.racun.requests.ChangeUserLimitRequest;
import rs.edu.raf.banka.racun.requests.TransakcijaRequest;
import rs.edu.raf.banka.racun.service.impl.TransakcijaService;
import rs.edu.raf.banka.racun.service.impl.UserService;
import rs.edu.raf.banka.racun.utils.HttpUtils;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TransakcijeServiceTest {

    @InjectMocks
    TransakcijaService transakcijaService;

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
    EntityManager entityManager;

    @Mock
    UserService userService;

    @Spy
    RestTemplate restTemplate = new RestTemplate();


    TransakcijaRequest transakcijaRequest = initTransakcijaRequest();

    String validJWToken = initValidJWT();
    String invalidJWToken = initInvalidJWT();

    String dummyName = "Mock";
    UUID mockRacun = UUID.randomUUID();
    String mockValuta = "RSD";

    @Value("${racun.user-service-url}")
    private String USER_SERVICE_URL;


    @Test
    void testDodavanjeTransakcije() throws NoSuchFieldException {

        Racun r = new Racun();
        r.setBrojRacuna(mockRacun);
        SredstvaKapital sredstvaKapital = new SredstvaKapital();
        sredstvaKapital.setUkupno(1000);

        Valuta v = new Valuta();
        v.setKodValute("RSD");
        Transakcija t = new Transakcija();

     //   when(userService.getUserByToken(any())).thenReturn("mockUsername");

        Query query = mock(Query.class);

        given(racunRepository.findByBrojRacuna(any())).willReturn(r);

        when(valutaRepository.findValutaByKodValute(transakcijaRequest.getValutaOznaka())).thenReturn(v);
        given(sredstvaKapitalRepository.findByRacunAndValuta(any(), any())).willReturn(sredstvaKapital);

        given(entityManager.createQuery(anyString())).willReturn(query);

        List<SredstvaKapital> skList = new ArrayList<>();
        skList.add(sredstvaKapital);

        given(query.getResultList()).willReturn(skList);

       // when(transakcijaRepository.save(any())).thenReturn(t);
       // when(sredstvaKapitalRepository.save(any())).thenReturn(sredstvaKapital);

     //   assertEquals(transakcijaService.dodajTransakciju("Bearer " + validJWToken, transakcijaRequest.getBrojRacuna(), transakcijaRequest.getOpis(), transakcijaRequest.getValutaOznaka(), transakcijaRequest.getOrderId(), transakcijaRequest.getUplata(), transakcijaRequest.getIsplata(), transakcijaRequest.getRezervisano(), transakcijaRequest.getRezervisanoKoristi(), transakcijaRequest.getLastSegment()), null);

    }



    private TransakcijaRequest initTransakcijaRequest() {
        TransakcijaRequest tr = new TransakcijaRequest();
        tr.setBrojRacuna(mockRacun);
        tr.setOpis("mockOpis");
        tr.setValutaOznaka(mockValuta);
        tr.setOrderId(1L);
        tr.setUplata(1000);
        tr.setIsplata(10000);
        //tr.setRezervisanoKoristi(0);
        tr.setRezervisano(0);
        tr.setLastSegment(false);

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
