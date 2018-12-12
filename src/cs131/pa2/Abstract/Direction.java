package cs131.pa2.Abstract;

public enum Direction {

	NORTH("NORTH"),
	SOUTH("SOUTH");
    private final String name;

    private Direction(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static Direction random() {
        int i = (int) (Math.random() * Direction.values().length);
        return Direction.values()[i];
    }
};