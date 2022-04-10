package rs.edu.raf.banka.berza.configuration;


import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SpringConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public WebClient influxApiClient() {
        return WebClient.create("http://influx-scrapper/");
    }

    @Bean
    public Config alphavantageApiClient() {
        Config cfg = Config.builder()
            .key("INSERT_API_KEY")
            .timeOut(10)
            .build();
        AlphaVantage.api().init(cfg);

        return cfg;
    }

}
