package rs.edu.raf.banka.berza.bootstrap;

import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import rs.edu.raf.banka.berza.dto.BerzaCSV;
import rs.edu.raf.banka.berza.dto.InflacijaCSV;
import rs.edu.raf.banka.berza.dto.CurrencyCSV;
import rs.edu.raf.banka.berza.model.Berza;
import rs.edu.raf.banka.berza.model.IstorijaInflacije;
import rs.edu.raf.banka.berza.model.Valuta;
import rs.edu.raf.banka.berza.repository.BerzaRepository;
import rs.edu.raf.banka.berza.repository.ValutaRepository;
import rs.edu.raf.banka.berza.repository.InflacijaRepository;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

@Component
@Slf4j
public class BootstrapData implements CommandLineRunner {

    @Value("${berza.berze.csv}")
    private String berzaCSVPath;
    @Value("${berza.inflacije.csv}")
    private String inflacijeCSVPath;

    private final ValutaRepository valutaRepository;
    private final BerzaRepository berzaRepository;
    private final InflacijaRepository inflacijaRepository;

    @Autowired
    public BootstrapData(ValutaRepository valutaRepository, BerzaRepository berzaRepo, InflacijaRepository inflacijaRepository) {
        this.valutaRepository = valutaRepository;
        this.berzaRepository = berzaRepo;
        this.inflacijaRepository = inflacijaRepository;
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

        List<CurrencyCSV> currencies = new CsvToBeanBuilder<CurrencyCSV>(new FileReader(fileName))
                .withType(CurrencyCSV.class)
                .withSkipLines(1)
                .build()
                .parse();

        List<Valuta> valute = new ArrayList<>();
        for(CurrencyCSV c: currencies) {
            Optional<Valuta> valutaBerze = valutaRepository.getValutaByNazivValute(c.getDescription());
            if(valutaBerze.isPresent()) {
                continue;
            }

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

        List<BerzaCSV> berzeCSV = new CsvToBeanBuilder<BerzaCSV>(new FileReader(berzaCSVPath))
                .withType(BerzaCSV.class)
                .withSkipLines(1)
                .build()
                .parse();

        for(BerzaCSV bc : berzeCSV) {
            Berza b = berzaRepository.findBerzaByOznakaBerze(bc.getExchangeAcronym());
            if(b != null) {
                continue;
            }

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
                log.error("Preskacem berzu " + bc.getExchangeAcronym() + " zato sto valuta " + bc.getCurrency() + " ne postoji!");
                continue;
            }
            berza.setValuta(valutaBerze.get());

            berze.add(berza);
        }
        berzaRepository.saveAll(berze);

        //Dodavanje informacija o inflacijama
        List<IstorijaInflacije> inflacije = new ArrayList<>();

        List<InflacijaCSV> inflacijeCSV = new CsvToBeanBuilder<InflacijaCSV>(new FileReader(inflacijeCSVPath))
                .withType(InflacijaCSV.class)
                .withSkipLines(1)
                .build()
                .parse();

        for(InflacijaCSV ic : inflacijeCSV) {
            Optional<Valuta> valutaInflacije = valutaRepository.getValutaByNazivValute(ic.getCurrency());
            if(valutaInflacije.isEmpty()) {
                log.error("Preskocena inflacija jer nema ove valute: "+ic.getCurrency());
                continue;
            }

            Optional<IstorijaInflacije> ii = inflacijaRepository.findByValutaAndYear(valutaInflacije.get(), ic.getYear());
            if(ii.isPresent()) {
                continue;
            }

            IstorijaInflacije istorijaInflacije = new IstorijaInflacije();
            istorijaInflacije.setValuta(valutaInflacije.get());
            istorijaInflacije.setYear(ic.getYear());
            istorijaInflacije.setInflationRate(Double.parseDouble(ic.getInflationRate()));

            inflacije.add(istorijaInflacije);
        }
        inflacijaRepository.saveAll(inflacije);
    }
}
