package rs.edu.raf.banka.berza.service.remote;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rs.edu.raf.banka.berza.requests.MarginTransakcijaRequest;
import rs.edu.raf.banka.berza.requests.TransakcijaRequest;
import rs.edu.raf.banka.berza.response.MarginTransakcijaResponse;
import rs.edu.raf.banka.berza.response.TransakcijaResponse;
import rs.edu.raf.banka.berza.utils.HttpUtils;

@Service
public class TransakcijaService {

    @Value("${berza.racun-service-url}")
    private String RACUN_SERVICE_URL;

    public TransakcijaResponse commitTransaction(String token, TransakcijaRequest transakcijaRequest) {
        ResponseEntity<TransakcijaResponse> response = HttpUtils.postTransaction(RACUN_SERVICE_URL, token, transakcijaRequest);
        if(response.getStatusCode() != HttpStatus.OK) {
            return null;
        }
        return response.getBody();
    }

    public MarginTransakcijaResponse commitMarginsTransaction(String token, MarginTransakcijaRequest request) {
        ResponseEntity<MarginTransakcijaResponse> response = HttpUtils.postMarginsTransaction(RACUN_SERVICE_URL, token, request);
        if(response.getStatusCode() != HttpStatus.OK) {
            return null;
        }
        return response.getBody();
    }

}
