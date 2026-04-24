package flc.model;

import java.util.Objects;

public class Review {
    private final Member member;
    private final int rating;
    private final String comment;

    public Review(Member member, int rating, String comment) {
        this.member = Objects.requireNonNull(member, "member");
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.rating = rating;
        this.comment = Objects.requireNonNull(comment, "comment");
    }

    public Member getMember() {
        return member;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }
}
