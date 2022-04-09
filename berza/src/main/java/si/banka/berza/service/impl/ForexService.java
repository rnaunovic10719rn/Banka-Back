package si.banka.berza.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import si.banka.berza.repository.ForexRepository;

@Service
public class ForexService {

    private ForexRepository forexRepository;


    @Autowired
    public ForexService(ForexRepository forexRepository){
        this.forexRepository = forexRepository;
    }


}
