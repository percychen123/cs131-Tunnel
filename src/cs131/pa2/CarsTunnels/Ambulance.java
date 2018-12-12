package cs131.pa2.CarsTunnels;

import cs131.pa2.Abstract.Direction;
import cs131.pa2.Abstract.Vehicle;

/**
 * An ambulance is a high priority vehicle
 */
public class Ambulance extends Vehicle{

	public Ambulance (String name, Direction direction) {
		super(name, direction);
	}

	@Override
	protected int getDefaultSpeed() {
		return 9;
	}
	
	@Override
    public String toString() {
        return String.format("%s AMBULANCE %s", super.getDirection(), super.getName());
    }
}
