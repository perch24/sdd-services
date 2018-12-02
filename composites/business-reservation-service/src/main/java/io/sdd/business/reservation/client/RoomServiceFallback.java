package io.sdd.business.reservation.client;

import io.sdd.business.reservation.Room;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RoomServiceFallback implements RoomService {
    @Override
    public List<Room> findAll(String roomNumber) {
        Room room = new Room();
        room.setId(-1);
        room.setBedInfo("N/A");
        room.setName("Fallback Room");
        room.setRoomNumber("N/A");
        return Collections.singletonList(room);
    }
}
