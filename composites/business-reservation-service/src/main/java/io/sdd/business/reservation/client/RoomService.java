package io.sdd.business.reservation.client;

import io.sdd.business.reservation.Room;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "ROOMSERVICES", fallback = RoomServiceFallback.class)
public interface RoomService {
    @RequestMapping(value = "/rooms", method = RequestMethod.GET)
    List<Room> findAll(@RequestParam(name = "roomNumber", required = false) String roomNumber);
}
