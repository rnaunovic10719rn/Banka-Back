package rs.edu.raf.banka.berza.bootstrap;

import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import rs.edu.raf.banka.berza.dto.BerzaCSV;
import rs.edu.raf.banka.berza.dto.CurrencyCSV;
import rs.edu.raf.banka.berza.model.Berza;
import rs.edu.raf.banka.berza.model.Valuta;
import rs.edu.raf.banka.berza.repository.BerzaRepository;
import rs.edu.raf.banka.berza.repository.ValutaRepository;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

@Component
public class BootstrapData implements CommandLineRunner {

    @Value("${berza.berze.csv}")
    private String berzaCSVPath;

    private final ValutaRepository valutaRepository;
    private final BerzaRepository berzaRepository;

    @Autowired
    public BootstrapData(ValutaRepository valutaRepository, BerzaRepository berzaRepo) {
        this.valutaRepository = valutaRepository;
        this.berzaRepository = berzaRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Loading Data...");

        FileOutputStream fos = null;
        try {
            URL website = new URL("https://www.alphavantage.co/physical_currency_list/");
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            fos = new FileOutputStream("currency.csv");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fos.close();
        }

        String fileName = "currency.csv";

        List<CurrencyCSV> currencies = new CsvToBeanBuilder(new FileReader(fileName))
                .withType(CurrencyCSV.class)
                .withSkipLines(1)
                .build()
                .parse();

        List<Valuta> valute = new ArrayList<>();
        for(CurrencyCSV c: currencies) {
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

        //Dodavanje informacija o berzi
        List<Berza> berze = new ArrayList<>();

        List<BerzaCSV> berzeCSV = new CsvToBeanBuilder(new FileReader(berzaCSVPath))
                .withType(BerzaCSV.class)
                .withSkipLines(1)
                .build()
                .parse();

        for(BerzaCSV bc : berzeCSV) {
            Berza berza = new Berza();
            berza.setDrzava(bc.getCountry());
            berza.setNaziv(bc.getExchangeName());
            berza.setOznakaBerze(bc.getExchangeAcronym());
            berza.setVremenskaZona(bc.getTimeZone());
            berza.setMicCode(bc.getExchangeMicCode());
            berza.setOpenTime(bc.getOpenTime());
            berza.setCloseTime(bc.getCloseTime());
            Optional<Valuta> valutaBerze = valutaRepository.getValutaByNazivValute(bc.getCurrency());
            if(valutaBerze.isEmpty()) {
                System.err.println("Preskacem berzu " + bc.getExchangeAcronym() + " zato sto valuta " + bc.getCurrency() + " ne postoji!");
                continue;
            }
            berza.setValuta(valutaBerze.get());

            berze.add(berza);
        }
        berzaRepository.saveAll(berze);
    }
}
