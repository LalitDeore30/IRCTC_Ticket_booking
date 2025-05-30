package ticket.booking.test;

import ticket.booking.service.TrainService;
import ticket.booking.entities.Train;
import java.io.IOException;
import java.util.List;

public class TestTrainService {
    public static void main(String[] args) {
        try {
            System.out.println("Testing Train Service...");
            TrainService service = new TrainService();

            // Test Mumbai to Delhi
            System.out.println("\nTesting Mumbai to Delhi route:");
            List<Train> mumDel = service.searchTrains("Mumbai", "Delhi");

            // Test Bangalore to Delhi
            System.out.println("\nTesting Bangalore to Delhi route:");
            List<Train> bangDel = service.searchTrains("Bangalore", "Delhi");

            // Test Chennai to Mumbai
            System.out.println("\nTesting Chennai to Mumbai route:");
            List<Train> chenMum = service.searchTrains("Chennai", "Mumbai");

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}