package flc.model;

public enum ExerciseType {
    YOGA("Yoga", 12.00),
    ZUMBA("Zumba", 11.00),
    AQUACISE("Aquacise", 13.50),
    BOX_FIT("Box Fit", 14.00),
    BODY_BLITZ("Body Blitz", 15.00),
    PILATES("Pilates", 12.50);

    private final String displayName;
    private final double price;

    ExerciseType(String displayName, double price) {
        this.displayName = displayName;
        this.price = price;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getPrice() {
        return price;
    }
}
