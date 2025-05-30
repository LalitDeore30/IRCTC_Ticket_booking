package ticket.booking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Train;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrainService {

    private List<Train> trainList;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String TRAIN_FILE_PATH = Paths
            .get(System.getProperty("user.dir"), "src", "main", "java", "ticket", "booking", "localDb", "trains.json")
            .toString();

    public TrainService() throws IOException {
        File file = new File(TRAIN_FILE_PATH);
        System.out.println("Loading trains from: " + file.getAbsolutePath());
        System.out.println("File exists: " + file.exists());

        if (!file.exists()) {
            // Create the directory if it doesn't exist
            File directory = file.getParentFile();
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Initialize with some sample trains
            trainList = new ArrayList<>();

            // Sample train 1: Delhi to Mumbai
            Train train1 = new Train();
            train1.setTrainId("DEL-MUM-001");
            train1.setTrainNo("12345");
            train1.setStations(List.of("Delhi", "Jaipur", "Mumbai"));
            train1.getStationTimes().put("Delhi", "08:00:00");
            train1.getStationTimes().put("Jaipur", "12:00:00");
            train1.getStationTimes().put("Mumbai", "20:00:00");
            train1.setSeats(createEmptySeats(5, 6)); // 5 rows, 6 seats per row
            trainList.add(train1);

            // Sample train 2: Mumbai to Bangalore
            Train train2 = new Train();
            train2.setTrainId("MUM-BLR-001");
            train2.setTrainNo("54321");
            train2.setStations(List.of("Mumbai", "Pune", "Bangalore"));
            train2.getStationTimes().put("Mumbai", "09:00:00");
            train2.getStationTimes().put("Pune", "11:00:00");
            train2.getStationTimes().put("Bangalore", "19:00:00");
            train2.setSeats(createEmptySeats(5, 6));
            trainList.add(train2);

            // Save the sample trains to file
            objectMapper.writeValue(file, trainList);
            System.out.println("Created new trains.json with sample trains");
        } else {
            trainList = objectMapper.readValue(file, new TypeReference<List<Train>>() {
            });
        }
    }

    private List<List<Integer>> createEmptySeats(int rows, int seatsPerRow) {
        List<List<Integer>> seats = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < seatsPerRow; j++) {
                row.add(0); // 0 represents an empty seat
            }
            seats.add(row);
        }
        return seats;
    }

    /**
     * Searches for trains that go from the specified source to destination.
     *
     * @param source      the source station
     * @param destination the destination station
     * @return list of trains that match the route criteria
     */
    public List<Train> searchTrains(String source, String destination) {
        System.out.println("Searching for trains from " + source + " to " + destination);
        System.out.println("Total trains in system: " + trainList.size());

        List<Train> matchingTrains = trainList.stream()
                .filter(train -> isValidTrain(train, source, destination))
                .collect(Collectors.toList());

        System.out.println("Found " + matchingTrains.size() + " matching trains");
        return matchingTrains;
    }

    /**
     * Adds a new train. If a train with the same ID exists, it updates the train
     * instead.
     *
     * @param newTrain the train to be added
     */
    public void addTrain(Train newTrain) throws IOException {
        trainList.add(newTrain);
        objectMapper.writeValue(new File(TRAIN_FILE_PATH), trainList);
    }

    /**
     * Updates a train by replacing the existing one with the same trainId.
     * If not found, adds the train as a new entry.
     *
     * @param updatedTrain the updated train data
     */
    public void updateTrain(Train updatedTrain) throws IOException {
        for (int i = 0; i < trainList.size(); i++) {
            if (trainList.get(i).getTrainId().equals(updatedTrain.getTrainId())) {
                trainList.set(i, updatedTrain);
                break;
            }
        }
        objectMapper.writeValue(new File(TRAIN_FILE_PATH), trainList);
    }

    /**
     * Validates if a train goes from the source to the destination.
     *
     * @param train       the train to be validated
     * @param source      the source station
     * @param destination the destination station
     * @return true if valid route, otherwise false
     */
    private boolean isValidTrain(Train train, String source, String destination) {
        List<String> stations = train.getStations();
        System.out.println("Checking train " + train.getTrainId() + " with stations: " + String.join(" -> ", stations));

        int sourceIndex = findStationIndex(stations, source);
        int destinationIndex = findStationIndex(stations, destination);

        System.out.println("Source '" + source + "' index: " + sourceIndex);
        System.out.println("Destination '" + destination + "' index: " + destinationIndex);

        return sourceIndex != -1 && destinationIndex != -1 && sourceIndex < destinationIndex;
    }

    /**
     * Helper to find station index ignoring case.
     */
    private int findStationIndex(List<String> stations, String stationName) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.get(i).equalsIgnoreCase(stationName)) {
                return i;
            }
        }
        return -1;
    }
}
