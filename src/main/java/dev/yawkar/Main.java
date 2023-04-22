package dev.yawkar;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Минимальный этаж = 0.");
        System.out.print("Введите максимальный этаж: ");
        int maxFloor = scanner.nextInt();
        System.out.print("Введите максимальную вместимость лифта: ");
        int maxElevatorCapacity = scanner.nextInt();
        System.out.print("Введите стартовый этаж лифта: ");
        int elevatorStartingFloor = scanner.nextInt();
        System.out.print("Введите периодичность появления пассажиров (миллисекунды): ");
        long millisPasGen = scanner.nextLong();
        System.out.print("Введите скорость движения лифта (миллисекунд на пролет этажа): ");
        long millisPerFloor = scanner.nextLong();

        Building building = new Building(maxFloor, maxElevatorCapacity, elevatorStartingFloor);

        Executor executor = Executors.newCachedThreadPool();
        Timer timer = new Timer();
        Random random = new Random(Instant.now().getEpochSecond());
        Set<Integer> wereUsedIds = new HashSet<>();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                executor.execute(building::stepElevatorFurther);
            }
        }, 0, millisPerFloor);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                var fromFloor = random.nextInt(0, maxFloor + 1);
                var anotherFloor = random.nextInt(0, maxFloor + 1);
                while (fromFloor == anotherFloor) {
                    anotherFloor = random.nextInt(0, maxFloor + 1);
                }
                var personId = random.nextInt(Integer.MAX_VALUE);
                while (wereUsedIds.contains(personId)) {
                    personId = random.nextInt();
                }
                wereUsedIds.add(personId);
                int finalPersonId = personId;
                int finalAnotherFloor = anotherFloor;
                executor.execute(() -> building.registerOnFloor(finalPersonId, fromFloor, finalAnotherFloor));
            }
        }, 0, millisPasGen);
    }
}