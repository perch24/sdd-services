package io.sdd.composites.league.client;

import io.sdd.composites.league.League;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class LeagueServiceFallback implements LeagueService {
    @Override
    public List<League> findAll() {
        League league = new League();
        league.setId(-1);
        league.setName("Fallback League");
        return Collections.singletonList(league);
    }
}
