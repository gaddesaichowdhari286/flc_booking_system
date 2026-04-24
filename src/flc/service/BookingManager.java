package flc.service;

import flc.model.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class BookingManager {
    private final Map<String, Member> members = new LinkedHashMap<>();
    private final Map<String, Lesson> lessons = new LinkedHashMap<>();
    private final Map<String, Booking> bookings = new LinkedHashMap<>();
    private int bookingSequence = 1;

    public void addMember(Member member) {
        members.put(member.getId(), member);
    }

    public void addLesson(Lesson lesson) {
        lessons.put(lesson.getLessonId(), lesson);
    }

    public Collection<Member> getMembers() {
        return Collections.unmodifiableCollection(members.values());
    }

    public Collection<Lesson> getLessons() {
        return Collections.unmodifiableCollection(lessons.values());
    }

    public Optional<Member> findMember(String memberId) {
        return Optional.ofNullable(members.get(memberId));
    }

    public Optional<Lesson> findLesson(String lessonId) {
        return Optional.ofNullable(lessons.get(lessonId));
    }

    public Optional<Booking> findBooking(String bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }

    /** Returns all active (non-cancelled) bookings for a given member. */
    public List<Booking> getBookingsForMember(String memberId) {
        requireMember(memberId);
        return bookings.values().stream()
                .filter(b -> b.getMember().getId().equals(memberId))
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .collect(Collectors.toList());
    }

    public List<Lesson> getTimetableByDay(DayType dayType) {
        return lessons.values().stream()
                .filter(l -> l.getDayType() == dayType)
                .sorted(Comparator.comparing(Lesson::getDate).thenComparing(Lesson::getTimeSlot))
                .collect(Collectors.toList());
    }

    public List<Lesson> getTimetableByExercise(ExerciseType exerciseType) {
        return lessons.values().stream()
                .filter(l -> l.getExerciseType() == exerciseType)
                .sorted(Comparator.comparing(Lesson::getDate).thenComparing(Lesson::getTimeSlot))
                .collect(Collectors.toList());
    }

    public Booking bookLesson(String memberId, String lessonId) {
        Member member = requireMember(memberId);
        Lesson lesson = requireLesson(lessonId);
        validateBooking(member, lesson, null);

        Booking booking = new Booking("B" + bookingSequence++, member, lesson);
        lesson.addBooking(booking);
        bookings.put(booking.getBookingId(), booking);
        return booking;
    }

    public void changeBooking(String bookingId, String newLessonId) {
        Booking booking = requireBooking(bookingId);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot change a cancelled booking");
        }
        if (booking.getStatus() == BookingStatus.ATTENDED) {
            throw new IllegalStateException("Cannot change a booking that has already been attended");
        }
        Lesson oldLesson = booking.getLesson();
        Lesson newLesson = requireLesson(newLessonId);
        validateBooking(booking.getMember(), newLesson, booking);

        oldLesson.removeBooking(booking);
        newLesson.addBooking(booking);
        booking.setLesson(newLesson);
        booking.setStatus(BookingStatus.CHANGED);
    }

    public void cancelBooking(String bookingId) {
        Booking booking = requireBooking(bookingId);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }
        if (booking.getStatus() == BookingStatus.ATTENDED) {
            throw new IllegalStateException("Cannot cancel a booking that has already been attended");
        }
        booking.getLesson().removeBooking(booking);
        booking.setStatus(BookingStatus.CANCELLED);
    }

    /**
     * Marks a booking as attended and records the member's review and rating.
     * Review submission is part of attending a lesson.
     */
    public void attendLesson(String bookingId, int rating, String comment) {
        Booking booking = requireBooking(bookingId);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot attend a cancelled booking");
        }
        if (booking.getStatus() == BookingStatus.ATTENDED) {
            throw new IllegalStateException("Lesson has already been attended");
        }

        Lesson lesson = booking.getLesson();
        boolean alreadyReviewed = lesson.getReviews().stream()
                .anyMatch(r -> r.getMember().equals(booking.getMember()));
        if (alreadyReviewed) {
            throw new IllegalStateException("Member has already reviewed this lesson");
        }

        booking.setStatus(BookingStatus.ATTENDED);
        lesson.addReview(new Review(booking.getMember(), rating, comment));
    }

    public Map<LocalDate, List<Lesson>> getLessonsGroupedByDate() {
        return lessons.values().stream()
                .sorted(Comparator.comparing(Lesson::getDate).thenComparing(Lesson::getTimeSlot))
                .collect(Collectors.groupingBy(Lesson::getDate, LinkedHashMap::new, Collectors.toList()));
    }

    /** Lessons grouped by date, filtered to a specific month (1-12). */
    public Map<LocalDate, List<Lesson>> getLessonsGroupedByMonth(int month) {
        return lessons.values().stream()
                .filter(l -> l.getDate().getMonthValue() == month)
                .sorted(Comparator.comparing(Lesson::getDate).thenComparing(Lesson::getTimeSlot))
                .collect(Collectors.groupingBy(Lesson::getDate, LinkedHashMap::new, Collectors.toList()));
    }

    /** Income per exercise type, filtered to a specific month (1-12). */
    public Map<ExerciseType, Double> getIncomeByExerciseForMonth(int month) {
        Map<ExerciseType, Double> result = new LinkedHashMap<>();
        for (ExerciseType type : ExerciseType.values()) {
            result.put(type, 0.0);
        }
        lessons.values().stream()
                .filter(l -> l.getDate().getMonthValue() == month)
                .forEach(l -> result.merge(l.getExerciseType(), l.getAttendedIncome(), Double::sum));
        return result;
    }

    public Optional<ExerciseType> getHighestIncomeExercise() {
        return lessons.values().stream()
                .collect(Collectors.groupingBy(Lesson::getExerciseType,
                        Collectors.summingDouble(Lesson::getAttendedIncome)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    public Optional<ExerciseType> getHighestIncomeExerciseForMonth(int month) {
        return getIncomeByExerciseForMonth(month).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    public double getIncomeForExercise(ExerciseType exerciseType) {
        return lessons.values().stream()
                .filter(l -> l.getExerciseType() == exerciseType)
                .mapToDouble(Lesson::getAttendedIncome)
                .sum();
    }

    private void validateBooking(Member member, Lesson lesson, Booking bookingBeingChanged) {
        if (!lesson.hasSpace() && (bookingBeingChanged == null || bookingBeingChanged.getLesson() != lesson)) {
            throw new IllegalStateException("No spaces available for the selected lesson");
        }

        // Explicit duplicate booking check
        boolean isDuplicate = bookings.values().stream()
                .filter(b -> bookingBeingChanged == null || !b.getBookingId().equals(bookingBeingChanged.getBookingId()))
                .filter(b -> b.getMember().equals(member))
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .anyMatch(b -> b.getLesson().getLessonId().equals(lesson.getLessonId()));
        if (isDuplicate) {
            throw new IllegalStateException("Member has already booked this lesson");
        }

        // Time-slot conflict check
        boolean hasConflict = bookings.values().stream()
                .filter(b -> bookingBeingChanged == null || !b.getBookingId().equals(bookingBeingChanged.getBookingId()))
                .filter(b -> b.getMember().equals(member))
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .map(Booking::getLesson)
                .anyMatch(existing -> existing.getDate().equals(lesson.getDate())
                        && existing.getTimeSlot() == lesson.getTimeSlot());
        if (hasConflict) {
            throw new IllegalStateException("Member already has a booking in the same time slot");
        }
    }

    private Member requireMember(String memberId) {
        return findMember(memberId).orElseThrow(() -> new IllegalArgumentException("Unknown member: " + memberId));
    }

    private Lesson requireLesson(String lessonId) {
        return findLesson(lessonId).orElseThrow(() -> new IllegalArgumentException("Unknown lesson: " + lessonId));
    }

    private Booking requireBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Unknown booking: " + bookingId);
        }
        return booking;
    }
}
