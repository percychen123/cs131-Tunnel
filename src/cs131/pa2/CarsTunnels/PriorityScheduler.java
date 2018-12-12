package cs131.pa2.CarsTunnels;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs131.pa2.Abstract.Direction;
import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.Log;

public class PriorityScheduler extends Tunnel{
	//private BasicTunnel tunnel;
	private Collection<Tunnel> tunnelList; 
	private Lock lock;
	private Condition canEnterTunnel;
	private Condition priority;
	private Map<Vehicle,Tunnel> vTot;
	private TreeMap<Integer,HashSet<Vehicle>> waiting;
	
	public PriorityScheduler(String name,Collection<Tunnel> tunnels,Log log) {
		super(name,log);
		this.lock = new ReentrantLock();
		this.tunnelList=tunnels;
		this.canEnterTunnel=lock.newCondition();
		this.priority=lock.newCondition();
		this.vTot=new HashMap<Vehicle,Tunnel>();//vehicle to specific tunnel
		this.waiting=new TreeMap<Integer,HashSet<Vehicle>>();//number of cars that has same priority
	}

	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		lock.lock();
		try {
		int priority = vehicle.getPriority();
		if(this.waiting.containsKey(priority)) {//check if map has this key, if has, then value +1
			waiting.get(priority).add(vehicle);;
		}else {//if there is no key, then set 1 as the value
			this.waiting.put(priority, new HashSet<Vehicle>());
			waiting.get(priority).add(vehicle);
		}
		BasicTunnel tunnelCanGet = null;
		while(tunnelCanGet == null) {
			tunnelCanGet = lookForTunnel(vehicle);
//			System.out.println(vehicle);
//			System.out.println(vehicle.getPriority());
//			System.out.println(tunnelCanGet);
			if(tunnelCanGet == null) {//if no tunnel available await
				//System.out.println("no tunnel");
				canEnterTunnel.await();
			}
			//if there is a tunnel available then check the priority condition
			while(priority<this.waiting.lastKey()) {
				//System.out.println("no priority");
				this.priority.await();
			}
		}
			
			//if conditions are satisfied, then vehicle can go in the tunnel
			if(waiting.get(priority).size()>1) {//if value of that key >1, then just decrease the number of that priority
				this.waiting.get(priority).remove(vehicle);
			}else {//if the number of that priority is only one, then remove that key
				this.waiting.remove(priority);
			}
			this.vTot.put(vehicle, tunnelCanGet);
			//System.out.println(vehicle+" get in "+tunnelCanGet);
			tunnelCanGet.tryToEnter(vehicle);
			this.priority.signalAll();
			return true;
		}catch(InterruptedException e){
			e.printStackTrace();
		}finally {
			lock.unlock();
		}
		return false;
	}
	
	public BasicTunnel lookForTunnel(Vehicle vehicle) {
		
		for(Tunnel t : this.tunnelList) {
			BasicTunnel bt = (BasicTunnel) t;
			if(bt.getAmbulanceCount()==0) {
				if(vehicle instanceof Ambulance) {
					if (bt.getCarCount() < 3 ) {
						return bt;
					}
				}
				if (vehicle instanceof Car && !waitingHasAmb()) {
					if (bt.getSledCount() == 0 && bt.getCarCount() == 0 ) {
						return bt;
					}else if(bt.getSledCount()==0 && bt.getCarCount() < 3 && bt.getDirection().equals(vehicle.getDirection())) {
						return bt;
					}
				}
				if (vehicle instanceof Sled && !waitingHasAmb()) {
					if (bt.getSledCount() == 0 && bt.getCarCount() == 0) {
						return bt;
					}
				}
			}
		}
		return null;
	}
	
	public boolean waitingHasAmb() {
		if(waiting.lastKey()==4) {
			for(Vehicle v : waiting.get(4)) {
				if  (v instanceof Ambulance) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public Tunnel getTunnel(Vehicle vehicle) {
		if(vTot.containsKey(vehicle)) {
			return vTot.get(vehicle);
		}
		return null;
	}

	@Override
	public void exitTunnelInner(Vehicle vehicle) {
		lock.lock();
		try {
			BasicTunnel tunnel = (BasicTunnel) this.vTot.get(vehicle);
			tunnel.exitTunnel(vehicle);
			//System.out.println(vehicle+" get out ");
			this.canEnterTunnel.signalAll();
			this.vTot.remove(vehicle);
		}finally {
			lock.unlock();
		}
	}
}
