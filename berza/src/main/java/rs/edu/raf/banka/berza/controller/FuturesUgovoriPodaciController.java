package rs.edu.raf.banka.berza.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.berza.dto.FuturesPodaciDto;
import rs.edu.raf.banka.berza.dto.FuturesUgovoriDto;
import rs.edu.raf.banka.berza.dto.request.AkcijeTimeseriesUpdateRequest;
import rs.edu.raf.banka.berza.model.FuturesUgovori;
import rs.edu.raf.banka.berza.requests.FilterHartijaOdVrednostiRequest;
import rs.edu.raf.banka.berza.requests.SearchHartijaOdVrednostiRequest;
import rs.edu.raf.banka.berza.service.impl.FuturesUgovoriPodaciService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/futures/podaci")
public class FuturesUgovoriPodaciController {

    private final FuturesUgovoriPodaciService futuresUgovoriPodaciService;

    @Autowired
    private ModelMapper modelMapper;

    public FuturesUgovoriPodaciController(FuturesUgovoriPodaciService futuresUgovoriPodaciService){
        this.futuresUgovoriPodaciService = futuresUgovoriPodaciService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getOdabraniFuturesUgovori(){
        return ResponseEntity.ok(futuresUgovoriPodaciService.getOdabraniFuturesUgovori());
    }

    @GetMapping(value = "/{symbol}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getFuturesUgovor(@PathVariable String symbol){
        if(symbol == null || symbol.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(futuresUgovoriPodaciService.getFuturesUgovor(symbol));
    }

    @GetMapping(value = "/timeseries/{type}/{symbol}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAkcijeTimeseries(@PathVariable String type, @PathVariable String symbol){
        if(type == null || type.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if(symbol == null || symbol.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(futuresUgovoriPodaciService.getFuturesTimeseries(type, symbol));
    }

//    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<?> getFuturesUgovori(){
//        List<FuturesUgovori> ugovori = futuresUgovoriPodaciService.getAllFuturesUgovori();
//        return ResponseEntity.ok(ugovori.stream().map(this::convertToDto).collect(Collectors.toList()));
//    }
//
//    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<?> getFuturesUgovoriById(@PathVariable Long id){
//        return ResponseEntity.ok(futuresUgovoriPodaciService.getByID(id));
//    }
//
//    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<?> searchFuturesUgovori(@RequestBody SearchHartijaOdVrednostiRequest searchHartijaOdVrednostiRequest,
//                                                  @RequestParam(defaultValue = "0")
//                                                          Integer page, @RequestParam(defaultValue = "30")
//                                                              Integer size){
//        return ResponseEntity.
//                ok(futuresUgovoriPodaciService.
//                        search(searchHartijaOdVrednostiRequest.getOznaka_hartije(), searchHartijaOdVrednostiRequest.getOpis_hartije(),
//                page, size));
//    }
//
//    @PostMapping(value = "/filter", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<?> filterFuturesUgovori(@RequestBody FilterHartijaOdVrednostiRequest filterHartijaOdVrednostiRequest, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "30") Integer size){
//        String berzaPrefix = filterHartijaOdVrednostiRequest.getBerzaPrefix();
//        Double priceLowBound = filterHartijaOdVrednostiRequest.getPriceLowBound();
//        Double priceUpperBound = filterHartijaOdVrednostiRequest.getPriceUpperBound();
//        Double askLowBound = filterHartijaOdVrednostiRequest.getAskLowBound();
//        Double askUpperBound = filterHartijaOdVrednostiRequest.getAskUpperBound();
//        Double bidLowBound = filterHartijaOdVrednostiRequest.getBidLowBound();
//        Double bidUpperBound = filterHartijaOdVrednostiRequest.getBidUpperBound();
//        Long volumeLowBound = filterHartijaOdVrednostiRequest.getVolumeLowBound();
//        Long volumeUpperBound = filterHartijaOdVrednostiRequest.getVolumeUpperBound();
//
//        return ResponseEntity.ok(futuresUgovoriPodaciService.filter(berzaPrefix, priceLowBound, priceUpperBound, askLowBound, askUpperBound,
//                bidLowBound, bidUpperBound, volumeLowBound, volumeUpperBound, page,size));
//    }
//
//    private FuturesUgovoriDto convertToDto(FuturesUgovori ugovori) {
//       FuturesUgovoriDto futuresuUgovoriDto = modelMapper.map(ugovori, FuturesUgovoriDto.class);
//        return futuresuUgovoriDto;
//    }

}
