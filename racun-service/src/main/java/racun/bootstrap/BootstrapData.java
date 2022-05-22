package racun.bootstrap;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class BootstrapData implements CommandLineRunner {



    @Autowired
    public BootstrapData() {

    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Loading Data...");


    }
}
