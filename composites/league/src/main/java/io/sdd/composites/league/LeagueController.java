package io.sdd.composites.league;

import io.sdd.composites.league.client.LeagueService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LeagueController {
    private final LeagueService leagueService;

    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }

    @RequestMapping(value = "/course", method = RequestMethod.GET)
    public List<League> getLeagues() {
        return leagueService.findAll();
    }
}
