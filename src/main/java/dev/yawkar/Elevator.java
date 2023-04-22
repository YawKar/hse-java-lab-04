package dev.yawkar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Elevator {
    public List<Map.Entry<Integer, Integer>> passengers;
    public int maxCapacity;
    public int currentFloor;
    public int targetFloor;
    public ElevatorStatus status;

    public Elevator(int startingFloor, int maxCapacity) {
        this.status = ElevatorStatus.IDLE;
        this.currentFloor = startingFloor;
        this.maxCapacity = maxCapacity;
        this.passengers = new ArrayList<>();
    }
}
