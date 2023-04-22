package dev.yawkar;

import java.util.*;
import java.util.stream.IntStream;

public class Building {
    private int minFloor;
    private int maxFloor;
    private Elevator elevator;
    private Queue<Integer> floorsQueue;
    private List<Boolean> isFloorActive;
    private List<Queue<Map.Entry<Integer, Integer>>> queuesOnFloors;

    public Building(int maxFloor, int maxElevatorCapacity, int elevatorStartingFloor) {
        this.minFloor = 0;
        this.maxFloor = maxFloor;
        this.elevator = new Elevator(elevatorStartingFloor, maxElevatorCapacity);
        this.floorsQueue = new ArrayDeque<>();
        this.isFloorActive = new ArrayList<>(IntStream.range(0, maxFloor + 1).mapToObj(i -> false).toList());
        this.queuesOnFloors = new ArrayList<>(IntStream.range(0, maxFloor + 1).mapToObj(i -> new ArrayDeque<Map.Entry<Integer, Integer>>()).toList());
    }

    public synchronized void registerOnFloor(int personId, int floor, int anotherFloor) {
        assert floor >= 0 && floor <= maxFloor;
        System.out.printf("Пассажир %d занял очередь на %d этаже, цель %d%n", personId, floor, anotherFloor);
        if (!isFloorActive.get(floor)) {
            isFloorActive.set(floor, true);
            floorsQueue.offer(floor);
        }
        this.queuesOnFloors.get(floor).offer(Map.entry(personId, anotherFloor));
    }

    public synchronized void stepElevatorFurther() {
        switch (elevator.status) {
            case IDLE -> {
                if (floorsQueue.isEmpty()) {
                    System.out.println("Очередь пуста. Лифт спит.");
                } else {
                    elevator.targetFloor = floorsQueue.poll();
                    if (elevator.targetFloor == elevator.currentFloor) {
                        processReception();
                    } else {
                        elevator.status = ElevatorStatus.MOVING;
                    }
                }
            }
            case MOVING -> {
                assert elevator.currentFloor != elevator.targetFloor;
                if (elevator.currentFloor < elevator.targetFloor) {
                    elevator.currentFloor++;
                } else {
                    elevator.currentFloor--;
                }
                if (elevator.currentFloor == elevator.targetFloor) {
                    processReception();
                } else {
                    // Take only passengers who targeted towards elevator's direction
                    boolean upDir = elevator.currentFloor < elevator.targetFloor;
                    processByWay(upDir);
                }
            }
        }
    }

    private void processByWay(boolean upDir) {
        isFloorActive.set(elevator.currentFloor, false);
        var outPassengers = new ArrayList<Integer>();
        for (var passenger : elevator.passengers) {
            if (passenger.getValue() == elevator.currentFloor) {
                outPassengers.add(passenger.getKey());
            }
        }
        for (var passengerId : outPassengers) {
            int index = -1;
            for (int i = 0; i < elevator.passengers.size(); ++i) {
                if (Objects.equals(elevator.passengers.get(i).getKey(), passengerId)) {
                    index = i;
                    break;
                }
            }
            elevator.passengers.remove(index);
            System.out.printf("Пассажир %d вышел из лифта на этаже %d%n", passengerId, elevator.currentFloor);
        }

        var queueCopy = new ArrayDeque<>(queuesOnFloors.get(elevator.currentFloor).stream().filter(entry -> {
            if (upDir) {
                return entry.getValue() > elevator.currentFloor;
            } else {
                return entry.getValue() < elevator.currentFloor;
            }
        }).toList());

        while (elevator.passengers.size() < elevator.maxCapacity && !queueCopy.isEmpty()) {
            var passenger = queueCopy.poll();
            elevator.passengers.add(passenger);
            queuesOnFloors.get(elevator.currentFloor).removeIf(entry -> Objects.equals(entry.getKey(), passenger.getKey()));
            assert passenger != null;
            System.out.printf("Пассажир %d зашёл в лифт на этаже %d%n", passenger.getKey(), elevator.currentFloor);
        }
        if (!queuesOnFloors.get(elevator.currentFloor).isEmpty()) {
            isFloorActive.set(elevator.currentFloor, true);
            floorsQueue.offer(elevator.currentFloor);
        }

        elevator.status = ElevatorStatus.MOVING;
        System.out.printf("Лифт продолжает движение %s к этажу %d%n", upDir ? "вверх" : "вниз", elevator.targetFloor);
    }

    private void processReception() {
        System.out.printf("Лифт открыл двери на этаже %d%n", elevator.currentFloor);
        isFloorActive.set(elevator.currentFloor, false);
        var outPassengers = new ArrayList<Integer>();
        for (var passenger : elevator.passengers) {
            if (passenger.getValue() == elevator.currentFloor) {
                outPassengers.add(passenger.getKey());
            }
        }
        for (var passengerId : outPassengers) {
            int index = -1;
            for (int i = 0; i < elevator.passengers.size(); ++i) {
                if (Objects.equals(elevator.passengers.get(i).getKey(), passengerId)) {
                    index = i;
                    break;
                }
            }
            elevator.passengers.remove(index);
            System.out.printf("Пассажир %d вышел из лифта на этаже %d%n", passengerId, elevator.currentFloor);
        }

        while (elevator.passengers.size() < elevator.maxCapacity && !queuesOnFloors.get(elevator.currentFloor).isEmpty()) {
            var passenger = queuesOnFloors.get(elevator.currentFloor).poll();
            elevator.passengers.add(passenger);
            assert passenger != null;
            System.out.printf("Пассажир %d зашёл в лифт на этаже %d%n", passenger.getKey(), elevator.currentFloor);
        }
        if (!queuesOnFloors.get(elevator.currentFloor).isEmpty()) {
            isFloorActive.set(elevator.currentFloor, true);
            floorsQueue.offer(elevator.currentFloor);
        }

        if (!elevator.passengers.isEmpty()) {
            elevator.targetFloor = elevator.passengers.get(0).getValue();
            elevator.status = ElevatorStatus.MOVING;
        } else {
            System.out.printf("Лифт уснул на этаже %d%n", elevator.currentFloor);
            elevator.status = ElevatorStatus.IDLE;
        }
    }
}
