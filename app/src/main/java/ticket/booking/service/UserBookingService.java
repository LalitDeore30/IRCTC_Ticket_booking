package ticket.booking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class UserBookingService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String USER_FILE_PATH = Paths
            .get(System.getProperty("user.dir"), "src", "main", "java", "ticket", "booking", "localDb", "users.json")
            .toString();

    private List<User> userList;
    private User user;

    // Constructors
    public UserBookingService(User user) throws IOException {
        loadUserListFromFile();

        // Check if user exists and credentials are valid
        Optional<User> existingUser = userList.stream()
                .filter(u -> u.getName().equals(user.getName()))
                .findFirst();

        if (!existingUser.isPresent()) {
            throw new IOException("User '" + user.getName() + "' not found. Please sign up first.");
        }

        User foundUser = existingUser.get();
        if (!UserServiceUtil.checkPassword(user.getPassword(), foundUser.getHashedPassword())) {
            throw new IOException("Invalid password for user '" + user.getName() + "'.");
        }

        // Set the user to the found user to maintain correct user data
        this.user = foundUser;
    }

    public UserBookingService() throws IOException {
        loadUserListFromFile();
    }

    // Load user list from file
    private void loadUserListFromFile() throws IOException {
        File file = new File(USER_FILE_PATH);
        if (!file.exists()) {
            userList = new ArrayList<>();
            objectMapper.writeValue(file, userList);
        } else {
            userList = objectMapper.readValue(file, new TypeReference<List<User>>() {
            });
        }

        // Print debug information
        System.out.println("Loaded " + userList.size() + " users from file.");
        for (User u : userList) {
            System.out.println("Found user: " + u.getName());
        }
    }

    // Save user list to file
    private void saveUserListToFile() throws IOException {
        objectMapper.writeValue(new File(USER_FILE_PATH), userList);
        System.out.println("Saved " + userList.size() + " users to file.");
    }

    // User login
    public Boolean loginUser() {
        Optional<User> existingUser = userList.stream()
                .filter(u -> u.getName().equals(user.getName()))
                .findFirst();

        if (!existingUser.isPresent()) {
            System.out.println("User '" + user.getName() + "' not found. Please sign up first.");
            return Boolean.FALSE;
        }

        User foundUser = existingUser.get();
        if (!UserServiceUtil.checkPassword(user.getPassword(), foundUser.getHashedPassword())) {
            System.out.println("Invalid password for user '" + user.getName() + "'.");
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    // Sign up new user
    public Boolean signUp(User newUser) {
        try {
            // Validate username and password
            if (newUser.getName() == null || newUser.getName().trim().isEmpty()) {
                System.out.println("Username cannot be empty.");
                return Boolean.FALSE;
            }

            if (newUser.getPassword() == null || newUser.getPassword().trim().isEmpty()) {
                System.out.println("Password cannot be empty.");
                return Boolean.FALSE;
            }

            // Check if username already exists (case-sensitive)
            boolean userExists = userList.stream()
                    .anyMatch(u -> u.getName().equals(newUser.getName()));

            if (userExists) {
                System.out.println(
                        "Username '" + newUser.getName() + "' already exists. Please choose a different username.");
                return Boolean.FALSE;
            }

            // Add the new user
            userList.add(newUser);
            saveUserListToFile();
            System.out.println("Sign up successful! You can now log in with your credentials.");
            return Boolean.TRUE;
        } catch (IOException e) {
            System.out.println("Error during sign up: " + e.getMessage());
            return Boolean.FALSE;
        }
    }

    // Fetch bookings of the current user
    public void fetchBookings() {
        userList.stream()
                .filter(u -> u.getName().equals(user.getName()) &&
                        UserServiceUtil.checkPassword(user.getPassword(), u.getHashedPassword()))
                .findFirst()
                .ifPresent(User::printTickets);
    }

    // Cancel a ticket by ticketId
    public Boolean cancelBooking(String ticketId) {
        if (ticketId == null || ticketId.isEmpty()) {
            System.out.println("Ticket ID cannot be null or empty.");
            return Boolean.FALSE;
        }

        // Find the matching user in the list
        for (User u : userList) {
            if (u.getName().equals(user.getName()) &&
                    UserServiceUtil.checkPassword(user.getPassword(), u.getHashedPassword())) {

                // Find the ticket to be cancelled
                Optional<Ticket> ticketToCancel = u.getTicketsBooked().stream()
                        .filter(ticket -> ticket.getTicketId().equals(ticketId))
                        .findFirst();

                if (ticketToCancel.isPresent()) {
                    Ticket ticket = ticketToCancel.get();
                    Train train = ticket.getTrain();

                    try {
                        // Update the train's seat matrix
                        TrainService trainService = new TrainService();
                        List<Train> trains = trainService.searchTrains(ticket.getSource(), ticket.getDestination());

                        for (Train t : trains) {
                            if (t.getTrainId().equals(train.getTrainId())) {
                                // Get the current seats
                                List<List<Integer>> seats = t.getSeats();

                                // Use the stored row and column to mark the exact seat as available
                                seats.get(ticket.getSeatRow()).set(ticket.getSeatColumn(), 0);

                                // Update the train with the new seat matrix
                                t.setSeats(seats);
                                trainService.updateTrain(t);

                                // Remove the ticket from user's bookings
                                u.getTicketsBooked().remove(ticket);
                                saveUserListToFile();

                                System.out.println("Ticket with ID " + ticketId + " has been canceled.");
                                System.out.println("Seat at Row " + ticket.getSeatRow() + ", Column "
                                        + ticket.getSeatColumn() + " is now available.");
                                return Boolean.TRUE;
                            }
                        }

                        System.out.println("Could not find the train. Cancellation failed.");
                        return Boolean.FALSE;
                    } catch (IOException e) {
                        System.out.println("Error updating train data after cancellation: " + e.getMessage());
                        return Boolean.FALSE;
                    }
                } else {
                    System.out.println("No ticket found with ID " + ticketId);
                    return Boolean.FALSE;
                }
            }
        }

        System.out.println("User not found or invalid credentials.");
        return Boolean.FALSE;
    }

    // Search trains
    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    // Fetch seat matrix from a train
    public List<List<Integer>> fetchSeats(Train train) {
        return train.getSeats();
    }

    // Book a train seat
    public Boolean bookTrainSeat(Train train, int row, int seat) {
        try {
            List<List<Integer>> seats = train.getSeats();

            if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);

                    // Create a new ticket
                    String ticketId = UUID.randomUUID().toString();
                    String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());

                    Ticket ticket = new Ticket(
                            ticketId,
                            user.getUserId(),
                            train.getStations().get(0), // source
                            train.getStations().get(train.getStations().size() - 1), // destination
                            currentDate,
                            train,
                            row, // Save the row number
                            seat // Save the seat number
                    );

                    // Find and update the current user's tickets
                    for (User u : userList) {
                        if (u.getName().equals(user.getName()) &&
                                UserServiceUtil.checkPassword(user.getPassword(), u.getHashedPassword())) {
                            u.getTicketsBooked().add(ticket);
                            break;
                        }
                    }

                    // Persist the updated train
                    new TrainService().addTrain(train);
                    // Persist updated user list
                    saveUserListToFile();

                    System.out.println("Ticket booked successfully! Ticket ID: " + ticket.getTicketId());
                    return Boolean.TRUE;
                } else {
                    System.out.println("Seat already booked.");
                    return Boolean.FALSE;
                }
            } else {
                System.out.println("Invalid seat coordinates.");
                return Boolean.FALSE;
            }
        } catch (IOException e) {
            System.out.println("Error occurred while booking: " + e.getMessage());
            return Boolean.FALSE;
        }
    }
}
