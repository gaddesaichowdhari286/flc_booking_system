package flc.model;

import java.util.Objects;

public class Booking {
    private final String bookingId;
    private final Member member;
    private Lesson lesson;
    private BookingStatus status;

    public Booking(String bookingId, Member member, Lesson lesson) {
        this.bookingId = Objects.requireNonNull(bookingId, "bookingId");
        this.member = Objects.requireNonNull(member, "member");
        this.lesson = Objects.requireNonNull(lesson, "lesson");
        this.status = BookingStatus.BOOKED;
    }

    public String getBookingId() { return bookingId; }
    public Member getMember() { return member; }
    public Lesson getLesson() { return lesson; }
    public BookingStatus getStatus() { return status; }

    public void setLesson(Lesson lesson) {
        this.lesson = Objects.requireNonNull(lesson, "lesson");
    }

    public void setStatus(BookingStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }
}
