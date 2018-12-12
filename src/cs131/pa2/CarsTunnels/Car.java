package cs131.pa2.CarsTunnels;

import cs131.pa2.Abstract.Direction;
import cs131.pa2.Abstract.Vehicle;

/**
 * A Car is a fast Vehicle.
 */
public class Car extends Vehicle {

    public Car(String name, Direction direction) {
        super(name, direction);
    }

    protected int getDefaultSpeed() {
        return 6;
    }
    
    @Override
    public String toString() {
        return String.format("%s CAR %s", super.getDirection(), super.getName());
    }
}
