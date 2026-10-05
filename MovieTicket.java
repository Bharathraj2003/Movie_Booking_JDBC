

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class MovieTicket {
	static final String URL =
            "jdbc:mysql://localhost:3306/movie_booking";

    static final String USER = "root";
    static final String PASSWORD = "root";

	public static void main(String[] args) {
	    Scanner sc = new Scanner(System.in);

        try {
            Connection con =
                    DriverManager.getConnection(URL, USER, PASSWORD);

            while (true) {

                System.out.println("\n===== MOVIE TICKET BOOKING =====");
                System.out.println("1. View Movies");
                System.out.println("2. View Shows");
                System.out.println("3. Book Ticket");
                System.out.println("4. View Bookings");
                System.out.println("5. Exit");

                System.out.print("Enter choice: ");
                int choice = sc.nextInt();

                switch (choice) {

                    // 1. Movie Listing
                    case 1:

                        String movieQuery =
                                "SELECT * FROM movies";

                        PreparedStatement moviePs =
                                con.prepareStatement(movieQuery);

                        ResultSet movieRs =
                                moviePs.executeQuery();

                        System.out.println("\n----- MOVIES -----");

                        while (movieRs.next()) {

                            System.out.println(
                                    "ID: " +
                                    movieRs.getInt("movie_id") +

                                    " | Name: " +
                                    movieRs.getString("movie_name") +

                                    " | Language: " +
                                    movieRs.getString("language") +

                                    " | Duration: " +
                                    movieRs.getInt("duration") +
                                    " minutes"
                            );
                        }

                        break;


                    // 2. Show Listing
                    case 2:

                        String showQuery =
                                "SELECT s.show_id, " +
                                "m.movie_name, " +
                                "t.theater_name, " +
                                "t.location, " +
                                "s.show_time, " +
                                "s.available_seats " +
                                "FROM shows s " +
                                "JOIN movies m ON s.movie_id = m.movie_id " +
                                "JOIN theaters t ON s.theater_id = t.theater_id";

                        PreparedStatement showPs =
                                con.prepareStatement(showQuery);

                        ResultSet showRs =
                                showPs.executeQuery();

                        System.out.println("\n----- SHOWS -----");

                        while (showRs.next()) {

                            System.out.println(
                                    "Show ID: " +
                                    showRs.getInt("show_id") +

                                    " | Movie: " +
                                    showRs.getString("movie_name") +

                                    " | Theater: " +
                                    showRs.getString("theater_name") +

                                    " | Location: " +
                                    showRs.getString("location") +

                                    " | Time: " +
                                    showRs.getTimestamp("show_time") +

                                    " | Available Seats: " +
                                    showRs.getInt("available_seats")
                            );
                        }

                        break;


                    // 3. Book Ticket
                    case 3:

                        System.out.print("Enter Show ID: ");
                        int showId = sc.nextInt();

                        sc.nextLine();

                        System.out.print("Enter Customer Name: ");
                        String customerName = sc.nextLine();

                        System.out.print("Enter Number of Seats: ");
                        int seats = sc.nextInt();

                        // Start transaction
                        con.setAutoCommit(false);

                        try {

                            // Check available seats
                            String checkQuery =
                                    "SELECT available_seats " +
                                    "FROM shows " +
                                    "WHERE show_id = ? FOR UPDATE";

                            PreparedStatement checkPs =
                                    con.prepareStatement(checkQuery);

                            checkPs.setInt(1, showId);

                            ResultSet rs =
                                    checkPs.executeQuery();

                            if (!rs.next()) {

                                System.out.println("Show not found!");

                                con.rollback();
                                con.setAutoCommit(true);

                                break;
                            }

                            int availableSeats =
                                    rs.getInt("available_seats");


                            // Seat validation
                            if (seats <= 0) {

                                System.out.println(
                                        "Invalid number of seats!"
                                );

                                con.rollback();
                                con.setAutoCommit(true);

                                break;
                            }


                            if (seats > availableSeats) {

                                System.out.println(
                                        "Booking failed!"
                                );

                                System.out.println(
                                        "Only " +
                                        availableSeats +
                                        " seats are available."
                                );

                                con.rollback();
                                con.setAutoCommit(true);

                                break;
                            }


                            // Update available seats
                            String updateQuery =
                                    "UPDATE shows " +
                                    "SET available_seats = " +
                                    "available_seats - ? " +
                                    "WHERE show_id = ?";

                            PreparedStatement updatePs =
                                    con.prepareStatement(updateQuery);

                            updatePs.setInt(1, seats);
                            updatePs.setInt(2, showId);

                            updatePs.executeUpdate();


                            // Insert booking
                            String bookingQuery =
                                    "INSERT INTO bookings " +
                                    "(show_id, customer_name, seat_count) " +
                                    "VALUES (?, ?, ?)";

                            PreparedStatement bookingPs =
                                    con.prepareStatement(bookingQuery);

                            bookingPs.setInt(1, showId);
                            bookingPs.setString(2, customerName);
                            bookingPs.setInt(3, seats);

                            bookingPs.executeUpdate();


                            // Complete transaction
                            con.commit();

                            System.out.println(
                                    "\n===== BOOKING SUCCESSFUL ====="
                            );

                            System.out.println(
                                    "Customer: " +
                                    customerName
                            );

                            System.out.println(
                                    "Seats Booked: " +
                                    seats
                            );

                        } catch (Exception e) {

                            con.rollback();

                            System.out.println(
                                    "Booking failed!"
                            );

                            e.printStackTrace();

                        } finally {

                            con.setAutoCommit(true);
                        }

                        break;


                    // 4. View Bookings
                    case 4:

                        String bookingQuery =
                                "SELECT b.booking_id, " +
                                "b.customer_name, " +
                                "m.movie_name, " +
                                "t.theater_name, " +
                                "b.seat_count, " +
                                "b.booking_time " +
                                "FROM bookings b " +
                                "JOIN shows s ON b.show_id = s.show_id " +
                                "JOIN movies m ON s.movie_id = m.movie_id " +
                                "JOIN theaters t ON s.theater_id = t.theater_id";

                        PreparedStatement bookingPs =
                                con.prepareStatement(bookingQuery);

                        ResultSet bookingRs =
                                bookingPs.executeQuery();

                        System.out.println(
                                "\n----- BOOKINGS -----"
                        );

                        while (bookingRs.next()) {

                            System.out.println(
                                    "Booking ID: " +
                                    bookingRs.getInt("booking_id") +

                                    " | Customer: " +
                                    bookingRs.getString("customer_name") +

                                    " | Movie: " +
                                    bookingRs.getString("movie_name") +

                                    " | Theater: " +
                                    bookingRs.getString("theater_name") +

                                    " | Seats: " +
                                    bookingRs.getInt("seat_count") +

                                    " | Time: " +
                                    bookingRs.getTimestamp("booking_time")
                            );
                        }

                        break;


                    // 5. Exit
                    case 5:

                        con.close();
                        sc.close();

                        System.out.println(
                                "Thank you!"
                        );

                        return;


                    default:

                        System.out.println(
                                "Invalid choice!"
                        );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        
}

	}

}
