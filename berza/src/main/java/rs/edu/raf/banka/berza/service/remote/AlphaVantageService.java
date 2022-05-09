package rs.edu.raf.banka.berza.service.remote;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;
import com.crazzyghost.alphavantage.fundamentaldata.response.CompanyOverview;
import com.crazzyghost.alphavantage.fundamentaldata.response.CompanyOverviewResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AlphaVantageService {

    private final Config alphavantageApiClient;

    @Autowired
    public AlphaVantageService(Config alphavantageApiClient){
        this.alphavantageApiClient = alphavantageApiClient;
    }

    public CompanyOverview getCompanyOverview(String ticker) {
        CompanyOverviewResponse cor = AlphaVantage
                .api()
                .fundamentalData()
                .companyOverview()
                .forSymbol(ticker)
                .fetchSync();
        if(cor != null && cor.getErrorMessage() == null) {
            return cor.getOverview();
        }

        return null;
    }
}
