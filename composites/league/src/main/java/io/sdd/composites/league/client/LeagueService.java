package io.sdd.composites.league.client;

import io.sdd.composites.league.League;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "COURSE-SERVICE", fallback = LeagueServiceFallback.class)
public interface LeagueService {
    @RequestMapping(value = "/course", method = RequestMethod.GET)
    List<League> findAll();
}
