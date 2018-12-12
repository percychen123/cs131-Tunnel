package cs131.pa2.CarsTunnels;

import cs131.pa2.Abstract.Direction;
import cs131.pa2.Abstract.Vehicle;

/**
 * A Sled is a slow Vehicle.
 */
public class Sled extends Vehicle {

    public Sled(String name, Direction direction) {
        super(name, direction);
    }

    protected int getDefaultSpeed() {
        return 4;
    }
    
    @Override
    public String toString() {
        return String.format("%s SLED %s", super.getDirection(), super.getName());
    }
}
