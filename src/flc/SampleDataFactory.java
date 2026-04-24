package flc;

import flc.model.*;
import flc.service.BookingManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SampleDataFactory {

    public BookingManager createSystemWithSampleData() {
        BookingManager manager = new BookingManager();
        registerMembers(manager);
        registerLessons(manager);
        createBookingsAndReviews(manager);
        return manager;
    }

    private void registerMembers(BookingManager manager) {
        for (int i = 1; i <= 10; i++) {
            manager.addMember(new Member("M" + i, switch (i) {
                case 1 -> "Alice Brown";
                case 2 -> "Ben Carter";
                case 3 -> "Chloe Singh";
                case 4 -> "Daniel Reed";
                case 5 -> "Eva Moore";
                case 6 -> "Farah Khan";
                case 7 -> "George Hall";
                case 8 -> "Hana Patel";
                case 9 -> "Isaac Green";
                default -> "Jade Wilson";
            }));
        }
    }

    private void registerLessons(BookingManager manager) {
        LocalDate saturday = LocalDate.of(2026, 1, 3);
        ExerciseType[][] weekendPattern = {
                {ExerciseType.YOGA, ExerciseType.ZUMBA, ExerciseType.BOX_FIT,
                 ExerciseType.AQUACISE, ExerciseType.BODY_BLITZ, ExerciseType.PILATES},
                {ExerciseType.ZUMBA, ExerciseType.BOX_FIT, ExerciseType.YOGA,
                 ExerciseType.PILATES, ExerciseType.AQUACISE, ExerciseType.BODY_BLITZ},
                {ExerciseType.BODY_BLITZ, ExerciseType.YOGA, ExerciseType.AQUACISE,
                 ExerciseType.ZUMBA, ExerciseType.BOX_FIT, ExerciseType.PILATES},
                {ExerciseType.PILATES, ExerciseType.AQUACISE, ExerciseType.ZUMBA,
                 ExerciseType.BOX_FIT, ExerciseType.YOGA, ExerciseType.BODY_BLITZ},
                {ExerciseType.YOGA, ExerciseType.BODY_BLITZ, ExerciseType.PILATES,
                 ExerciseType.AQUACISE, ExerciseType.ZUMBA, ExerciseType.BOX_FIT},
                {ExerciseType.BOX_FIT, ExerciseType.ZUMBA, ExerciseType.AQUACISE,
                 ExerciseType.YOGA, ExerciseType.PILATES, ExerciseType.BODY_BLITZ},
                {ExerciseType.AQUACISE, ExerciseType.YOGA, ExerciseType.BODY_BLITZ,
                 ExerciseType.ZUMBA, ExerciseType.PILATES, ExerciseType.BOX_FIT},
                {ExerciseType.PILATES, ExerciseType.BOX_FIT, ExerciseType.YOGA,
                 ExerciseType.BODY_BLITZ, ExerciseType.AQUACISE, ExerciseType.ZUMBA}
        };

        int lessonNo = 1;
        for (int week = 0; week < 8; week++) {
            LocalDate satDate = saturday.plusWeeks(week);
            LocalDate sunDate = satDate.plusDays(1);
            ExerciseType[] p = weekendPattern[week];
            manager.addLesson(new Lesson("L" + lessonNo++, satDate, DayType.SATURDAY, TimeSlot.MORNING, p[0]));
            manager.addLesson(new Lesson("L" + lessonNo++, satDate, DayType.SATURDAY, TimeSlot.AFTERNOON, p[1]));
            manager.addLesson(new Lesson("L" + lessonNo++, satDate, DayType.SATURDAY, TimeSlot.EVENING, p[2]));
            manager.addLesson(new Lesson("L" + lessonNo++, sunDate, DayType.SUNDAY, TimeSlot.MORNING, p[3]));
            manager.addLesson(new Lesson("L" + lessonNo++, sunDate, DayType.SUNDAY, TimeSlot.AFTERNOON, p[4]));
            manager.addLesson(new Lesson("L" + lessonNo++, sunDate, DayType.SUNDAY, TimeSlot.EVENING, p[5]));
        }
    }

    private void createBookingsAndReviews(BookingManager manager) {
        List<String> createdBookings = new ArrayList<>();
        int memberCycle = 1;
        int reviewCount = 0;

        for (Lesson lesson : manager.getLessons()) {
            int seats = switch (lesson.getTimeSlot()) {
                case MORNING -> 3;
                case AFTERNOON -> 2;
                case EVENING -> 4;
            };

            for (int i = 0; i < seats; i++) {
                String memberId = "M" + memberCycle;
                memberCycle = memberCycle == 10 ? 1 : memberCycle + 1;
                try {
                    var booking = manager.bookLesson(memberId, lesson.getLessonId());
                    createdBookings.add(booking.getBookingId());
                    if (reviewCount < 24) {
                        int rating = 3 + (reviewCount % 3);
                        manager.attendLesson(booking.getBookingId(), Math.min(rating, 5),
                                "Enjoyed the " + lesson.getExerciseType().getDisplayName() + " session.");
                        reviewCount++;
                    }
                } catch (IllegalStateException ignored) {
                    // Skip conflicts in generated sample data.
                }
            }
        }

        // Demonstrate changeBooking using bookings that have NOT been attended yet
        // (indices 0-23 are attended; use 24+ which are still in BOOKED status)
        if (createdBookings.size() > 26) {
            try {
                manager.changeBooking(createdBookings.get(24), "L2");
            } catch (IllegalStateException ignored) {
                // Skip if a conflict prevents this change.
            }
            try {
                manager.changeBooking(createdBookings.get(25), "L8");
            } catch (IllegalStateException ignored) {
                // Skip if a conflict prevents this change.
            }
        }
    }
}
