package rs.edu.raf.banka.berza.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.edu.raf.banka.berza.dto.FuturesUgovoriDto;
import rs.edu.raf.banka.berza.model.FuturesUgovori;
import rs.edu.raf.banka.berza.requests.FilterHartijaOdVrednostiRequest;
import rs.edu.raf.banka.berza.requests.SearchHartijaOdVrednostiRequest;
import rs.edu.raf.banka.berza.service.impl.FuturesUgovoriService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/futuresUgovori")
public class FuturesUgovoriController {

    private final FuturesUgovoriService futuresUgovoriService;

    @Autowired
    private ModelMapper modelMapper;


    public FuturesUgovoriController(FuturesUgovoriService futuresUgovoriService){

        this.futuresUgovoriService = futuresUgovoriService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getFuturesUgovori(){
        List<FuturesUgovori> ugovori = futuresUgovoriService.getAllFuturesUgovori();
        return ResponseEntity.ok(ugovori.stream().map(this::convertToDto).collect(Collectors.toList()));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getFuturesUgovoriById(@PathVariable Long id){
        return ResponseEntity.ok(futuresUgovoriService.getByID(id));
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchFuturesUgovori(@RequestBody SearchHartijaOdVrednostiRequest searchHartijaOdVrednostiRequest,
                                                  @RequestParam(defaultValue = "0")
                                                          Integer page, @RequestParam(defaultValue = "30")
                                                              Integer size){
        return ResponseEntity.
                ok(futuresUgovoriService.
                        search(searchHartijaOdVrednostiRequest.getOznaka_hartije(), searchHartijaOdVrednostiRequest.getOpis_hartije(),
                page, size));
    }

    @PostMapping(value = "/filter", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> filterFuturesUgovori(@RequestBody FilterHartijaOdVrednostiRequest filterHartijaOdVrednostiRequest, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "30") Integer size){
        String berzaPrefix = filterHartijaOdVrednostiRequest.getBerzaPrefix();
        Double priceLowBound = filterHartijaOdVrednostiRequest.getPriceLowBound();
        Double priceUpperBound = filterHartijaOdVrednostiRequest.getPriceUpperBound();
        Double askLowBound = filterHartijaOdVrednostiRequest.getAskLowBound();
        Double askUpperBound = filterHartijaOdVrednostiRequest.getAskUpperBound();
        Double bidLowBound = filterHartijaOdVrednostiRequest.getBidLowBound();
        Double bidUpperBound = filterHartijaOdVrednostiRequest.getBidUpperBound();
        Long volumeLowBound = filterHartijaOdVrednostiRequest.getVolumeLowBound();
        Long volumeUpperBound = filterHartijaOdVrednostiRequest.getVolumeUpperBound();

        return ResponseEntity.ok(futuresUgovoriService.filter(berzaPrefix, priceLowBound, priceUpperBound, askLowBound, askUpperBound,
                bidLowBound, bidUpperBound, volumeLowBound, volumeUpperBound, page,size));
    }

    private FuturesUgovoriDto convertToDto(FuturesUgovori ugovori) {
       FuturesUgovoriDto futuresuUgovoriDto = modelMapper.map(ugovori, FuturesUgovoriDto.class);
        return futuresuUgovoriDto;
    }

}
