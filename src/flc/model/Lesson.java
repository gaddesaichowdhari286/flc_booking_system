package flc.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Lesson {
    public static final int CAPACITY = 4;

    private final String lessonId;
    private final LocalDate date;
    private final DayType dayType;
    private final TimeSlot timeSlot;
    private final ExerciseType exerciseType;
    private final List<Booking> bookings = new ArrayList<>();
    private final List<Review> reviews = new ArrayList<>();

    public Lesson(String lessonId, LocalDate date, DayType dayType, TimeSlot timeSlot, ExerciseType exerciseType) {
        this.lessonId = Objects.requireNonNull(lessonId, "lessonId");
        this.date = Objects.requireNonNull(date, "date");
        this.dayType = Objects.requireNonNull(dayType, "dayType");
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");
        this.exerciseType = Objects.requireNonNull(exerciseType, "exerciseType");
    }

    public String getLessonId() { return lessonId; }
    public LocalDate getDate() { return date; }
    public DayType getDayType() { return dayType; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public ExerciseType getExerciseType() { return exerciseType; }
    public double getPrice() { return exerciseType.getPrice(); }

    public boolean hasSpace() {
        long active = bookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .count();
        return active < CAPACITY;
    }

    public void addBooking(Booking booking) {
        if (!hasSpace()) {
            throw new IllegalStateException("Lesson is full");
        }
        bookings.add(Objects.requireNonNull(booking, "booking"));
    }

    public void removeBooking(Booking booking) {
        bookings.remove(booking);
    }

    public List<Booking> getBookings() {
        return Collections.unmodifiableList(bookings);
    }

    /** Total bookings including cancelled ones (for reference). */
    public int getBookedCount() {
        return (int) bookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .count();
    }

    /** Count of members who have actually attended this lesson. */
    public int getAttendedCount() {
        return (int) bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.ATTENDED)
                .count();
    }

    public void addReview(Review review) {
        reviews.add(Objects.requireNonNull(review, "review"));
    }

    public List<Review> getReviews() {
        return Collections.unmodifiableList(reviews);
    }

    public double getAverageRating() {
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }

    /** Income based on all active (non-cancelled) bookings. */
    public double getIncome() {
        return getBookedCount() * getPrice();
    }

    /** Income based only on attended bookings (used in monthly reports). */
    public double getAttendedIncome() {
        return getAttendedCount() * getPrice();
    }

    @Override
    public String toString() {
        return String.format("%s %s %s (%s, \u00a3%.2f, spaces left: %d)",
                date, dayType, timeSlot, exerciseType.getDisplayName(), getPrice(), CAPACITY - getBookedCount());
    }
}
