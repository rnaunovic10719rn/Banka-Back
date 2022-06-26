package rs.edu.raf.banka.racun.bootstrap;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import rs.edu.raf.banka.racun.dto.CurrencyCSV;
import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.repository.ValutaRepository;
import rs.edu.raf.banka.racun.service.impl.RacunService;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;


@Component
@Slf4j
public class BootstrapData implements CommandLineRunner {


    private final ValutaRepository valutaRepository;
    private final RacunService racunService;


    @Autowired
    public BootstrapData(ValutaRepository valutaRepository, RacunService racunService) {
        this.valutaRepository = valutaRepository;
        this.racunService = racunService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Loading Data...");

        if (valutaRepository.count()==0) {


            FileOutputStream fos = null;
            try {
                URL website = new URL("https://www.alphavantage.co/physical_currency_list/");
                try (ReadableByteChannel rbc = Channels.newChannel(website.openStream())) {
                    fos = new FileOutputStream("currency.csv");
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(fos != null) {
                    fos.close();
                }
            }

            String fileName = "currency.csv";

            List<CurrencyCSV> currencies = new CsvToBeanBuilder<CurrencyCSV>(new FileReader(fileName))
                    .withType(CurrencyCSV.class)
                    .withSkipLines(1)
                    .build()
                    .parse();

            List<Valuta> valute = new ArrayList<>();
            for (CurrencyCSV c : currencies) {
                System.out.println(c);
                Valuta v = new Valuta();
                v.setKodValute(c.getIsoCode());
                v.setNazivValute(c.getDescription());
                try {
                    Currency jc = Currency.getInstance(c.getIsoCode());
                    if (jc != null) {
                        v.setOznakaValute(jc.getSymbol());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                valute.add(v);
            }
            valutaRepository.saveAll(valute);

            racunService.createKesRacun();
            racunService.createMarginRacun();
        }


    }
}
