package rs.edu.raf.banka.berza.service.remote;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.fundamentaldata.response.CompanyOverview;
import com.crazzyghost.alphavantage.fundamentaldata.response.CompanyOverviewResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlphaVantageService {

    @Autowired
    public AlphaVantageService(){
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
