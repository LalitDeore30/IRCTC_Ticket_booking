package ticket.booking;

import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.service.UserBookingService;
import ticket.booking.util.UserServiceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class App {

    public static void main(String[] args) {
        System.out.println("Running Train Booking System");
        Scanner scanner = new Scanner(System.in);
        int option = 0;
        UserBookingService userBookingService = null;
        Train trainSelectedForBooking = null; // To keep track of selected train
        User currentUser = null; // Track logged-in user
        boolean isLoggedIn = false;

        try {
            userBookingService = new UserBookingService();
        } catch (IOException ex) {
            System.out.println("There is something wrong initializing booking service.");
            ex.printStackTrace();
            scanner.close();
            return;
        }

        while (option != 7) {
            try {
                System.out.println("\nChoose option");
                System.out.println("1. Sign up");
                System.out.println("2. Login");
                System.out.println("3. Fetch Bookings");
                System.out.println("4. Search Trains");
                System.out.println("5. Book a Seat");
                System.out.println("6. Cancel my Booking");
                System.out.println("7. Exit the App");

                String input = scanner.nextLine().trim();

                try {
                    option = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number.");
                    continue;
                }

                switch (option) {
                    case 1:
                        System.out.println("Enter the username to signup:");
                        String nameToSignUp = scanner.nextLine().trim();
                        System.out.println("Enter the password to signup:");
                        String passwordToSignUp = scanner.nextLine().trim();

                        if (nameToSignUp.isEmpty() || passwordToSignUp.isEmpty()) {
                            System.out.println("Username or password cannot be empty.");
                            break;
                        }

                        // Create a new user with hashed password
                        String hashedPassword = UserServiceUtil.hashPassword(passwordToSignUp);
                        User userToSignup = new User(
                                nameToSignUp,
                                passwordToSignUp,
                                hashedPassword,
                                new ArrayList<>(),
                                UUID.randomUUID().toString());

                        boolean signUpResult = userBookingService.signUp(userToSignup);
                        if (!signUpResult) {
                            System.out.println("Please try signing up again with different credentials.");
                        }
                        break;

                    case 2:
                        if (isLoggedIn) {
                            System.out.println("You are already logged in as: " + currentUser.getName());
                            System.out.println(
                                    "Please logout first (select option 7) if you want to login as a different user.");
                            break;
                        }

                        System.out.println("Enter the username to login:");
                        String nameToLogin = scanner.nextLine().trim();
                        System.out.println("Enter the password to login:");
                        String passwordToLogin = scanner.nextLine().trim();

                        if (nameToLogin.isEmpty() || passwordToLogin.isEmpty()) {
                            System.out.println("Username or password cannot be empty.");
                            break;
                        }

                        try {
                            String hashedLoginPassword = UserServiceUtil.hashPassword(passwordToLogin);
                            User userToLogin = new User(
                                    nameToLogin,
                                    passwordToLogin,
                                    hashedLoginPassword,
                                    new ArrayList<>(),
                                    UUID.randomUUID().toString());

                            UserBookingService tempService = new UserBookingService(userToLogin);
                            userBookingService = tempService;
                            currentUser = userToLogin;
                            isLoggedIn = true;
                            trainSelectedForBooking = null; // reset selection on login
                            System.out.println("Login successful! Welcome, " + currentUser.getName());
                        } catch (IOException ex) {
                            System.out.println("Login failed: " + ex.getMessage());
                            System.out
                                    .println("Please check your credentials or sign up if you don't have an account.");
                            isLoggedIn = false;
                            currentUser = null;
                        }
                        break;

                    case 3:
                        if (!isLoggedIn) {
                            System.out.println("Please login first (Option 2).");
                            break;
                        }
                        System.out.println("Fetching your bookings...");
                        userBookingService.fetchBookings();
                        break;

                    case 4:
                        if (!isLoggedIn) {
                            System.out.println("Please login first (Option 2).");
                            break;
                        }
                        System.out.println("Type your source station:");
                        String source = scanner.nextLine().trim();
                        System.out.println("Type your destination station:");
                        String dest = scanner.nextLine().trim();

                        if (source.isEmpty() || dest.isEmpty()) {
                            System.out.println("Source or destination cannot be empty.");
                            break;
                        }

                        List<Train> trains = userBookingService.getTrains(source, dest);

                        if (trains.isEmpty()) {
                            System.out.println("No trains found for the given source and destination.");
                            break;
                        }

                        int index = 1;
                        for (Train t : trains) {
                            System.out.println(index + ". Train id: " + t.getTrainId());
                            for (Map.Entry<String, String> entry : t.getStationTimes().entrySet()) {
                                System.out.println("   Station: " + entry.getKey() + ", Time: " + entry.getValue());
                            }
                            index++;
                        }

                        System.out.println("Select a train by typing 1, 2, 3, ...");
                        String trainSelection = scanner.nextLine().trim();
                        int selectedTrainIndex;

                        try {
                            selectedTrainIndex = Integer.parseInt(trainSelection) - 1;
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter a number.");
                            break;
                        }

                        if (selectedTrainIndex < 0 || selectedTrainIndex >= trains.size()) {
                            System.out.println("Invalid train selection.");
                            break;
                        }
                        trainSelectedForBooking = trains.get(selectedTrainIndex);
                        System.out.println("Train selected: " + trainSelectedForBooking.getTrainId());
                        break;

                    case 5:
                        if (!isLoggedIn) {
                            System.out.println("Please login first (Option 2).");
                            break;
                        }
                        if (trainSelectedForBooking == null) {
                            System.out.println("Please search and select a train first (Option 4).");
                            break;
                        }

                        System.out.println("Available seats (0 = free, 1 = booked):");
                        List<List<Integer>> seats = userBookingService.fetchSeats(trainSelectedForBooking);
                        for (List<Integer> row : seats) {
                            for (Integer val : row) {
                                System.out.print(val + " ");
                            }
                            System.out.println();
                        }

                        System.out.println("Select the seat by typing the row and column:");

                        int row = -1, col = -1;
                        try {
                            System.out.print("Enter the row: ");
                            String rowInput = scanner.nextLine().trim();
                            System.out.print("Enter the column: ");
                            String colInput = scanner.nextLine().trim();

                            row = Integer.parseInt(rowInput);
                            col = Integer.parseInt(colInput);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input for row or column. Please enter numbers.");
                            break;
                        }

                        System.out.println("Booking your seat...");
                        Boolean booked = userBookingService.bookTrainSeat(trainSelectedForBooking, row, col);
                        if (Boolean.TRUE.equals(booked)) {
                            System.out.println("Booked! Enjoy your journey.");
                        } else {
                            System.out.println("Can't book this seat.");
                        }
                        break;

                    case 6:
                        if (!isLoggedIn) {
                            System.out.println("Please login first (Option 2).");
                            break;
                        }
                        System.out.println("Enter the Ticket ID to cancel:");
                        String ticketIdToCancel = scanner.nextLine().trim();

                        if (ticketIdToCancel.isEmpty()) {
                            System.out.println("Ticket ID cannot be empty.");
                            break;
                        }

                        Boolean cancelResult = userBookingService.cancelBooking(ticketIdToCancel);
                        if (Boolean.TRUE.equals(cancelResult)) {
                            System.out.println("Booking cancelled successfully.");
                        } else {
                            System.out.println("Failed to cancel booking. Ticket ID may be invalid.");
                        }
                        break;

                    case 7:
                        System.out.println("Exiting the app. Goodbye!");
                        break;

                    default:
                        System.out.println("Invalid option. Please select between 1 and 7.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }

        scanner.close();
    }
}
