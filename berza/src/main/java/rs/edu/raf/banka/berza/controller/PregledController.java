package rs.edu.raf.banka.berza.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.edu.raf.banka.berza.service.impl.FuturesUgovoriPodaciService;

@RestController
@RequestMapping("/api/futures/podaci")
public class PregledController {

    @Autowired
    private ModelMapper modelMapper;

    public FuturesUgovoriPodaciController(FuturesUgovoriPodaciService futuresUgovoriPodaciService){
        this.futuresUgovoriPodaciService = futuresUgovoriPodaciService;
    }

}
