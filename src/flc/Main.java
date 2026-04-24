package flc;

import flc.model.*;
import flc.service.BookingManager;
import flc.util.ReportPrinter;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static BookingManager manager;
    private static Scanner scanner;

    public static void main(String[] args) {
        // Guard: require Java 17 or later
        int javaVersion = Runtime.version().feature();
        if (javaVersion < 17) {
            System.err.println("ERROR: This program requires Java 17 or later.");
            System.err.println("       You are running Java " + javaVersion + ".");
            System.err.println("       Please install Java 17+ from https://adoptium.net/temurin/releases/?version=17");
            System.exit(1);
        }

        // Fix £ encoding so it renders correctly on all platforms
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        manager = new SampleDataFactory().createSystemWithSampleData();
        scanner = new Scanner(System.in, StandardCharsets.UTF_8);

        System.out.println("============================================");
        System.out.println("  Furzefield Leisure Centre Booking System  ");
        System.out.println("============================================");

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Select option: ", 0, 5);
            switch (choice) {
                case 1 -> bookLesson();
                case 2 -> changeOrCancelBooking();
                case 3 -> attendLesson();
                case 4 -> monthlyLessonReport();
                case 5 -> monthlyChampionReport();
                case 0 -> running = false;
            }
        }
        System.out.println("Goodbye!");
    }

    // -------------------------------------------------------------------------
    // Main menu
    // -------------------------------------------------------------------------

    private static void printMainMenu() {
        System.out.println();
        System.out.println("--- Main Menu ---");
        System.out.println("1. Book a group exercise lesson");
        System.out.println("2. Change / Cancel a booking");
        System.out.println("3. Attend a lesson");
        System.out.println("4. Monthly lesson report");
        System.out.println("5. Monthly champion lesson type report");
        System.out.println("0. Exit");
    }

    // -------------------------------------------------------------------------
    // 1. Book a lesson
    // -------------------------------------------------------------------------

    private static void bookLesson() {
        System.out.println("\n--- Book a Lesson ---");
        String memberId = promptMemberId();
        if (memberId == null) return;

        List<Lesson> timetable = chooseTimetableView();
        if (timetable == null || timetable.isEmpty()) {
            System.out.println("No lessons found.");
            return;
        }
        printLessonList(timetable);

        String lessonId = readLine("Enter Lesson ID to book (or 0 to cancel): ").trim();
        if (lessonId.equals("0")) return;

        try {
            Booking booking = manager.bookLesson(memberId, lessonId);
            System.out.printf("Booking successful! Booking ID: %s%n", booking.getBookingId());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Booking failed: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 2. Change / Cancel a booking
    // -------------------------------------------------------------------------

    private static void changeOrCancelBooking() {
        System.out.println("\n--- Change / Cancel a Booking ---");
        String memberId = promptMemberId();
        if (memberId == null) return;

        List<Booking> bookings = manager.getBookingsForMember(memberId);
        if (bookings.isEmpty()) {
            System.out.println("No active bookings found for this member.");
            return;
        }

        System.out.println("Your bookings:");
        for (Booking b : bookings) {
            Lesson l = b.getLesson();
            System.out.printf("  %s | %s %-10s %s %-10s [%s]%n",
                    b.getBookingId(),
                    l.getDate(), l.getDayType(),
                    l.getTimeSlot(), l.getExerciseType().getDisplayName(),
                    b.getStatus());
        }

        String bookingId = readLine("Enter Booking ID (or 0 to go back): ").trim();
        if (bookingId.equals("0")) return;

        System.out.println("1. Change to a different lesson");
        System.out.println("2. Cancel this booking");
        System.out.println("0. Go back");
        int action = readInt("Select: ", 0, 2);

        if (action == 0) return;

        if (action == 2) {
            try {
                manager.cancelBooking(bookingId);
                System.out.println("Booking " + bookingId + " has been cancelled.");
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Cancel failed: " + e.getMessage());
            }
            return;
        }

        // Change booking
        List<Lesson> timetable = chooseTimetableView();
        if (timetable == null || timetable.isEmpty()) {
            System.out.println("No lessons found.");
            return;
        }
        printLessonList(timetable);

        String newLessonId = readLine("Enter new Lesson ID (or 0 to cancel): ").trim();
        if (newLessonId.equals("0")) return;

        try {
            manager.changeBooking(bookingId, newLessonId);
            System.out.println("Booking changed successfully. Booking ID kept: " + bookingId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Change failed: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 3. Attend a lesson
    // -------------------------------------------------------------------------

    private static void attendLesson() {
        System.out.println("\n--- Attend a Lesson ---");
        String memberId = promptMemberId();
        if (memberId == null) return;

        List<Booking> bookings = manager.getBookingsForMember(memberId).stream()
                .filter(b -> b.getStatus() == BookingStatus.BOOKED || b.getStatus() == BookingStatus.CHANGED)
                .toList();

        if (bookings.isEmpty()) {
            System.out.println("No bookings available to attend.");
            return;
        }

        System.out.println("Your bookings:");
        for (Booking b : bookings) {
            Lesson l = b.getLesson();
            System.out.printf("  %s | %s %-10s %s %-10s [%s]%n",
                    b.getBookingId(),
                    l.getDate(), l.getDayType(),
                    l.getTimeSlot(), l.getExerciseType().getDisplayName(),
                    b.getStatus());
        }

        String bookingId = readLine("Enter Booking ID to attend (or 0 to go back): ").trim();
        if (bookingId.equals("0")) return;

        int rating = readInt("Enter rating (1=Very dissatisfied, 2=Dissatisfied, 3=Ok, 4=Satisfied, 5=Very Satisfied): ", 1, 5);
        String comment = readLine("Write a short review: ").trim();
        if (comment.isEmpty()) comment = "No comment.";

        try {
            manager.attendLesson(bookingId, rating, comment);
            System.out.println("Attendance recorded. Thank you for your review!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 4. Monthly lesson report
    // -------------------------------------------------------------------------

    private static void monthlyLessonReport() {
        System.out.println("\n--- Monthly Lesson Report ---");
        int month = readInt("Enter month number (1-12): ", 1, 12);
        ReportPrinter.printAttendanceAndRatingReport(manager, month);
    }

    // -------------------------------------------------------------------------
    // 5. Monthly champion exercise type report
    // -------------------------------------------------------------------------

    private static void monthlyChampionReport() {
        System.out.println("\n--- Monthly Champion Exercise Type Report ---");
        int month = readInt("Enter month number (1-12): ", 1, 12);
        ReportPrinter.printHighestIncomeExerciseReport(manager, month);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static List<Lesson> chooseTimetableView() {
        System.out.println("View timetable by:");
        System.out.println("  1. Day (Saturday / Sunday)");
        System.out.println("  2. Exercise type");
        int choice = readInt("Select: ", 1, 2);

        if (choice == 1) {
            System.out.println("  1. Saturday");
            System.out.println("  2. Sunday");
            int day = readInt("Select: ", 1, 2);
            DayType dayType = (day == 1) ? DayType.SATURDAY : DayType.SUNDAY;
            return manager.getTimetableByDay(dayType);
        } else {
            ExerciseType[] types = ExerciseType.values();
            for (int i = 0; i < types.length; i++) {
                System.out.printf("  %d. %s%n", i + 1, types[i].getDisplayName());
            }
            int idx = readInt("Select: ", 1, types.length);
            return manager.getTimetableByExercise(types[idx - 1]);
        }
    }

    private static void printLessonList(List<Lesson> lessons) {
        System.out.println();
        for (Lesson l : lessons) {
            System.out.printf("  %-4s | %s %-10s %s %-12s \u00a3%.2f  Spaces: %d%n",
                    l.getLessonId(),
                    l.getDate(), l.getDayType(),
                    l.getTimeSlot(), l.getExerciseType().getDisplayName(),
                    l.getPrice(),
                    Lesson.CAPACITY - l.getBookedCount());
        }
    }

    private static String promptMemberId() {
        System.out.println("Members:");
        for (flc.model.Member m : manager.getMembers()) {
            System.out.println("  " + m);
        }
        String id = readLine("Enter Member ID (or 0 to go back): ").trim();
        if (id.equals("0")) return null;
        if (manager.findMember(id).isEmpty()) {
            System.out.println("Unknown member ID.");
            return null;
        }
        return id;
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) return val;
            } catch (NumberFormatException ignored) {
            }
            System.out.printf("Please enter a number between %d and %d.%n", min, max);
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}

