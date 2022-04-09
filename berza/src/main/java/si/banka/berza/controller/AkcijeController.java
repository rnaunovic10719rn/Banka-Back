package si.banka.berza.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.banka.berza.dto.AkcijeDto;
import si.banka.berza.model.Akcije;
import si.banka.berza.requests.FilterHartijaOdVrednostiRequest;
import si.banka.berza.requests.SearchHartijaOdVrednostiRequest;
import si.banka.berza.service.impl.AkcijeService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/akcije")
public class AkcijeController {

    private final AkcijeService akcijeService;

    @Autowired
    private ModelMapper modelMapper;


    public AkcijeController(AkcijeService akcijeService){
        this.akcijeService = akcijeService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAkcije(){
        List<Akcije> akcije = akcijeService.getAllAkcije();
        return ResponseEntity.ok(akcije.stream().map(this::convertToDto).collect(Collectors.toList()));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAkcijeById(@PathVariable Long id){
        return ResponseEntity.ok(akcijeService.getByID(id));
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchAkcije(@RequestBody SearchHartijaOdVrednostiRequest searchHartijaOdVrednostiRequest, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "30") Integer size){
        return ResponseEntity.ok(akcijeService.search(searchHartijaOdVrednostiRequest.getOznaka_hartije(), searchHartijaOdVrednostiRequest.getOpis_hartije(),
                page, size));
    }

    @PostMapping(value = "/filter", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> filterAkcije(@RequestBody FilterHartijaOdVrednostiRequest filterHartijaOdVrednostiRequest, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "30") Integer size){
        String berzaPrefix = filterHartijaOdVrednostiRequest.getBerzaPrefix();
        Double priceLowBound = filterHartijaOdVrednostiRequest.getPriceLowBound();
        Double priceUpperBound = filterHartijaOdVrednostiRequest.getPriceUpperBound();
        Double askLowBound = filterHartijaOdVrednostiRequest.getAskLowBound();
        Double askUpperBound = filterHartijaOdVrednostiRequest.getAskUpperBound();
        Double bidLowBound = filterHartijaOdVrednostiRequest.getBidLowBound();
        Double bidUpperBound = filterHartijaOdVrednostiRequest.getBidUpperBound();
        Long volumeLowBound = filterHartijaOdVrednostiRequest.getVolumeLowBound();
        Long volumeUpperBound = filterHartijaOdVrednostiRequest.getVolumeUpperBound();

        return ResponseEntity.ok(akcijeService.filter(berzaPrefix, priceLowBound, priceUpperBound, askLowBound, askUpperBound,
                bidLowBound, bidUpperBound, volumeLowBound, volumeUpperBound, page,size));
    }

    private AkcijeDto convertToDto(Akcije akcije) {
        AkcijeDto akcijeDto = modelMapper.map(akcije, AkcijeDto.class);
        return akcijeDto;
    }

}
