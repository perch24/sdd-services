package io.perch.business.reservation;

import io.perch.business.reservation.client.RoomService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RoomReservationController {
    private final RoomService roomService;

    public RoomReservationController(RoomService roomService) {
        this.roomService = roomService;
    }

    @RequestMapping(value = "/rooms", method = RequestMethod.GET)
    public List<Room> getAllRooms(@RequestParam(name = "roomNumber", required = false) String roomNumber) {
        return roomService.findAll(roomNumber);
    }
}
