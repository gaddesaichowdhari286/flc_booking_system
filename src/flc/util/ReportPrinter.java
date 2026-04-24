package flc.util;

import flc.model.ExerciseType;
import flc.model.Lesson;
import flc.service.BookingManager;

import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportPrinter {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM uuuu", Locale.UK);

    /**
     * Monthly lesson report: lists each lesson in the specified month with attended member
     * count and average rating. Only attended bookings are counted.
     */
    public static void printAttendanceAndRatingReport(BookingManager manager, int month) {
        String monthName = Month.of(month).getDisplayName(java.time.format.TextStyle.FULL, Locale.UK);
        System.out.println("======== Monthly Attendance and Rating Report: " + monthName + " ========");
        Map<java.time.LocalDate, List<Lesson>> grouped = manager.getLessonsGroupedByMonth(month);
        if (grouped.isEmpty()) {
            System.out.println("No lessons found for " + monthName + ".");
            return;
        }
        for (Map.Entry<java.time.LocalDate, List<Lesson>> entry : grouped.entrySet()) {
            System.out.println("\n" + entry.getKey().format(DATE_FORMAT));
            for (Lesson lesson : entry.getValue()) {
                System.out.printf("  %-10s %-12s Attended: %d  Avg rating: %.2f  Income: \u00a3%.2f%n",
                        lesson.getTimeSlot(),
                        lesson.getExerciseType().getDisplayName(),
                        lesson.getAttendedCount(),
                        lesson.getAverageRating(),
                        lesson.getAttendedIncome());
            }
        }
    }

    /**
     * Monthly champion exercise type report: lists income for every exercise type in the
     * specified month and highlights the highest earner.
     */
    public static void printHighestIncomeExerciseReport(BookingManager manager, int month) {
        String monthName = Month.of(month).getDisplayName(java.time.format.TextStyle.FULL, Locale.UK);
        System.out.println("\n======== Monthly Champion Exercise Type Report: " + monthName + " ========");
        Map<ExerciseType, Double> incomes = manager.getIncomeByExerciseForMonth(month);
        if (incomes.values().stream().allMatch(v -> v == 0.0)) {
            System.out.println("No income data for " + monthName + ".");
            return;
        }
        for (Map.Entry<ExerciseType, Double> entry : incomes.entrySet()) {
            System.out.printf("  %-12s  Income: \u00a3%.2f%n",
                    entry.getKey().getDisplayName(), entry.getValue());
        }
        manager.getHighestIncomeExerciseForMonth(month).ifPresent(best ->
                System.out.printf("%nChampion exercise: %s (Total: \u00a3%.2f)%n",
                        best.getDisplayName(), incomes.get(best)));
    }
}

