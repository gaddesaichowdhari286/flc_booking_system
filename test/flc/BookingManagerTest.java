package flc;

import flc.model.*;
import flc.service.BookingManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BookingManagerTest {
    private BookingManager manager;

    @BeforeEach
    void setUp() {
        manager = new BookingManager();
        manager.addMember(new Member("M1", "Alice"));
        manager.addMember(new Member("M2", "Bob"));
        manager.addLesson(new Lesson("L1", LocalDate.of(2026, 1, 3), DayType.SATURDAY, TimeSlot.MORNING,   ExerciseType.YOGA));
        manager.addLesson(new Lesson("L2", LocalDate.of(2026, 1, 3), DayType.SATURDAY, TimeSlot.AFTERNOON, ExerciseType.ZUMBA));
        manager.addLesson(new Lesson("L3", LocalDate.of(2026, 1, 3), DayType.SATURDAY, TimeSlot.MORNING,   ExerciseType.BOX_FIT));
    }

    // -------------------------------------------------------------------------
    // 1. bookLesson — successful booking
    // -------------------------------------------------------------------------
    @Test
    void shouldBookLessonWhenSpaceAvailable() {
        Booking booking = manager.bookLesson("M1", "L1");
        assertEquals("M1", booking.getMember().getId());
        assertEquals(BookingStatus.BOOKED, booking.getStatus());
        assertEquals(1, manager.findLesson("L1").orElseThrow().getBookedCount());
    }

    // -------------------------------------------------------------------------
    // 2. bookLesson — time-slot conflict prevention
    // -------------------------------------------------------------------------
    @Test
    void shouldPreventTimeConflict() {
        manager.bookLesson("M1", "L1");
        // L1 and L3 are both MORNING on the same day — conflict expected
        assertThrows(IllegalStateException.class, () -> manager.bookLesson("M1", "L3"));
    }

    // -------------------------------------------------------------------------
    // 3. bookLesson — duplicate booking prevention
    // -------------------------------------------------------------------------
    @Test
    void shouldPreventDuplicateBooking() {
        manager.bookLesson("M1", "L1");
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> manager.bookLesson("M1", "L1"));
        assertTrue(ex.getMessage().contains("already booked"));
    }

    // -------------------------------------------------------------------------
    // 4. changeBooking — updates lesson and sets status to CHANGED
    // -------------------------------------------------------------------------
    @Test
    void shouldChangeBookingAndUpdateStatus() {
        Booking booking = manager.bookLesson("M1", "L1");
        manager.changeBooking(booking.getBookingId(), "L2");
        assertEquals("L2", booking.getLesson().getLessonId());
        assertEquals(BookingStatus.CHANGED, booking.getStatus());
        // Old lesson should have freed its space
        assertEquals(0, manager.findLesson("L1").orElseThrow().getBookedCount());
    }

    // -------------------------------------------------------------------------
    // 5. cancelBooking — releases space and sets status to CANCELLED
    // -------------------------------------------------------------------------
    @Test
    void shouldCancelBookingAndReleaseSpace() {
        Booking booking = manager.bookLesson("M1", "L1");
        manager.cancelBooking(booking.getBookingId());
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(0, manager.findLesson("L1").orElseThrow().getBookedCount());
    }

    // -------------------------------------------------------------------------
    // 6. attendLesson — records review, sets status to ATTENDED
    // -------------------------------------------------------------------------
    @Test
    void shouldAttendLessonAndRecordReview() {
        Booking booking = manager.bookLesson("M1", "L1");
        manager.attendLesson(booking.getBookingId(), 5, "Great class");
        assertEquals(BookingStatus.ATTENDED, booking.getStatus());
        assertEquals(5.0, manager.findLesson("L1").orElseThrow().getAverageRating());
        assertEquals(1, manager.findLesson("L1").orElseThrow().getAttendedCount());
    }

    // -------------------------------------------------------------------------
    // 7. attendLesson — cannot attend a cancelled booking
    // -------------------------------------------------------------------------
    @Test
    void shouldRejectAttendingCancelledBooking() {
        Booking booking = manager.bookLesson("M1", "L1");
        manager.cancelBooking(booking.getBookingId());
        assertThrows(IllegalStateException.class,
                () -> manager.attendLesson(booking.getBookingId(), 4, "Too late"));
    }

    // -------------------------------------------------------------------------
    // 8. getIncomeByExerciseForMonth — attended income only, filtered by month
    // -------------------------------------------------------------------------
    @Test
    void shouldCalculateMonthlyIncomeByExercise() {
        Booking b1 = manager.bookLesson("M1", "L2"); // Zumba £11
        Booking b2 = manager.bookLesson("M2", "L2"); // Zumba £11
        manager.attendLesson(b1.getBookingId(), 4, "Good");
        // b2 booked but not attended — should not count towards income

        Map<ExerciseType, Double> incomes = manager.getIncomeByExerciseForMonth(1);
        assertEquals(11.0, incomes.get(ExerciseType.ZUMBA), 0.001);
    }

    // -------------------------------------------------------------------------
    // 9. getHighestIncomeExercise — identifies correct champion
    // -------------------------------------------------------------------------
    @Test
    void shouldIdentifyHighestIncomeExercise() {
        Booking b1 = manager.bookLesson("M1", "L2");
        Booking b2 = manager.bookLesson("M2", "L2");
        manager.attendLesson(b1.getBookingId(), 4, "Good");
        manager.attendLesson(b2.getBookingId(), 3, "Ok");
        assertEquals(ExerciseType.ZUMBA, manager.getHighestIncomeExercise().orElseThrow());
    }
}

