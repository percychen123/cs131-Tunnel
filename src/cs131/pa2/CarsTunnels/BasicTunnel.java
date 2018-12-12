package cs131.pa2.CarsTunnels;

import java.util.*;

import cs131.pa2.Abstract.Direction;
import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;

public class BasicTunnel extends Tunnel{
	private int count;
	private LinkedList<Vehicle> running;
	private Direction d;
	private int carCount;
	private int sledCount;
	private int ambulanceCount;
	//private int highestP;
	public BasicTunnel(String name) {
		super(name);
		this.count=0;
		this.carCount=0;
		this.sledCount=0;
		this.ambulanceCount=0;
		//this.highestP=0;
		this.running=new LinkedList<Vehicle>();
	}

	@Override
	public synchronized boolean tryToEnterInner(Vehicle vehicle) {
			if(vehicle instanceof Ambulance) {
				this.ambulanceCount++;
				count++;
				return true;
			}else {
			if(count==0) {//there is no car in the tunnel, return true
				count++;
				running.add(vehicle);
				if(vehicle instanceof Car) {
					this.carCount++;
				}
				if(vehicle instanceof Sled) {
					this.sledCount++;
				}
				this.d=vehicle.getDirection();
				return true;
			}else {//there are cars in the tunnel
				if(this.d.equals(vehicle.getDirection())) {//make sure the direction is correct
					if(vehicle instanceof Car && !(running.peek() instanceof Sled)) {
						if(count<3) {//make sure there are less 3 cars in the tunnel
							count++;
							running.add(vehicle);
							this.carCount++;
							return true;
						}
					}else if(vehicle instanceof Sled) {//make sure the vehicle is sled
						if(count<1) {
							count++;
							running.add(vehicle);
							this.sledCount++;
							return true;
						}
					}
				}
			}
			}
			return false;
	}
	@Override
	public synchronized void exitTunnelInner(Vehicle vehicle) {
		count--;
		if(vehicle instanceof Ambulance) {
			ambulanceCount--;
		}
		if(vehicle instanceof Car) {
			carCount--;
		}else {
			sledCount--;
		}
		running.remove(vehicle);
	}
	@Override
	public Tunnel getTunnel(Vehicle vehicle) {
		return null;
	}
	public LinkedList<Vehicle> getList() {
		return running;
	}
	public Direction getDirection() {
		return this.d;
	}
	public int getCarCount() {
		return carCount;
	}
	public int getSledCount() {
		return sledCount;
	}
	public int getAmbulanceCount() {
		return ambulanceCount;
	}
}
