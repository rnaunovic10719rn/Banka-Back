package rs.edu.raf.banka.racun.service.impl;

import org.springframework.stereotype.Service;
import rs.edu.raf.banka.racun.dto.AgentSredstvaKapitalDto;
import rs.edu.raf.banka.racun.dto.SredstvaKapitalDto;
import rs.edu.raf.banka.racun.model.Racun;
import rs.edu.raf.banka.racun.model.SredstvaKapital;

import rs.edu.raf.banka.racun.model.Valuta;
import rs.edu.raf.banka.racun.repository.RacunRepository;
import rs.edu.raf.banka.racun.repository.SredstvaKapitalRepository;
import rs.edu.raf.banka.racun.repository.ValutaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SredstvaKapitalService {

    private final SredstvaKapitalRepository sredstvaKapitalRepository;
    private final RacunRepository racunRepository;
    private final ValutaRepository valutaRepository;
    private final UserService userService;


    public SredstvaKapitalService(SredstvaKapitalRepository sredstvaKapitalRepository, RacunRepository racunRepository, ValutaRepository valutaRepository, UserService userService) {
        this.sredstvaKapitalRepository = sredstvaKapitalRepository;
        this.racunRepository = racunRepository;
        this.valutaRepository = valutaRepository;
        this.userService = userService;
    }

    public SredstvaKapital getAll(UUID racun, String valuta) {
        return sredstvaKapitalRepository.findByRacunAndValuta(racunRepository.findByBrojRacuna(racun), valutaRepository.findValutaByKodValute(valuta));
    }

    public SredstvaKapital pocetnoStanje(UUID uuidRacuna, String kodValute, double ukupno) {
        Racun racun = racunRepository.findByBrojRacuna(uuidRacuna);
        if(racun == null) {
            return null;
        }
        Valuta valuta = valutaRepository.findValutaByKodValute(kodValute);
        if(valuta == null) {
            return null;
        }

        SredstvaKapital sredstvaKapital;
        sredstvaKapital = new SredstvaKapital();
        sredstvaKapital.setRacun(racun);
        sredstvaKapital.setValuta(valuta);
        sredstvaKapital.setUkupno(ukupno);
        sredstvaKapital.setRezervisano(0);
        sredstvaKapital.setRaspolozivo(ukupno);
        return sredstvaKapitalRepository.save(sredstvaKapital);
    }

    public List<SredstvaKapitalDto> findSredstvaKapitalSupervisor(String token) {
        String role = userService.getRoleByToken(token);
        if (role.equals("ROLE_AGENT"))
            return null;
        List<SredstvaKapital> sredstvaKapitals = sredstvaKapitalRepository.findAll();
        List<SredstvaKapitalDto> sredstvaKapitalDtos = new ArrayList<>();
        for (SredstvaKapital sredstvaKapital : sredstvaKapitals) {
            SredstvaKapitalDto s = new SredstvaKapitalDto();
            s.setKodValute(sredstvaKapital.getValuta().getKodValute());
            s.setUkupno(sredstvaKapital.getUkupno());
            s.setRezervisano(sredstvaKapital.getRezervisano());
            s.setRaspolozivo(sredstvaKapital.getRaspolozivo());
            sredstvaKapitalDtos.add(s);
        }
        return sredstvaKapitalDtos;
    }

    public AgentSredstvaKapitalDto findSredstvaKapitalAgent(String token) {
        String role = userService.getRoleByToken(token);
        if (role.equals("ROLE_AGENT")) {
            AgentSredstvaKapitalDto agentSredstvaKapitalDto = new AgentSredstvaKapitalDto();
            agentSredstvaKapitalDto.setLimit(userService.getUserByToken(token).getLimit());
            agentSredstvaKapitalDto.setLimitUsed(userService.getUserByToken(token).getLimitUsed());
            agentSredstvaKapitalDto.setRaspolozivoAgentu(agentSredstvaKapitalDto.getLimit() - agentSredstvaKapitalDto.getLimitUsed());
            return agentSredstvaKapitalDto;
        }
           return null;
    }
}
