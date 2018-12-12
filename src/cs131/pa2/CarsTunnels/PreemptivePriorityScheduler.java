package cs131.pa2.CarsTunnels;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.Log;

public class PreemptivePriorityScheduler extends Tunnel{
	private Condition canEnterTunnel;
	private Condition priority;
	private Map<Vehicle,Tunnel> vTot;
	private TreeMap<Integer, HashSet<Vehicle>> waiting;
	private Lock lock;
	private Collection<Tunnel> tunnelList; 

	public PreemptivePriorityScheduler(String name,Collection<Tunnel> tunnels,Log log) {
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
				waiting.get(priority).add(vehicle);
			}else {//if there is no key, then set 1 as the value
				this.waiting.put(priority, new HashSet<Vehicle>());
				waiting.get(priority).add(vehicle);
			}
			//System.out.println(waiting);
			BasicTunnel tunnelCanGet = null;
			while(tunnelCanGet == null) {
				//find a tunnel available
				tunnelCanGet = lookForTunnel(vehicle);
			
				if(tunnelCanGet == null ) {//if no tunnel available await
					//System.out.println(1);
					canEnterTunnel.await();
					System.out.println(vehicle+"done waiting");
				}	
				//if there is a tunnel available then check the priority condition
				while(priority<this.waiting.lastKey()) {
					//System.out.println(2);
					this.priority.await();
				}
			}
			if(waiting.get(priority).size()>1) {//if value of that key >1, then just decrease the number of that priority
				this.waiting.get(priority).remove(vehicle);
			}else {//if the number of that priority is only one, then remove that key
				this.waiting.remove(priority);
			}
			if (vehicle instanceof Ambulance) {
				for (Vehicle v : vTot.keySet()) {
					if(vTot.get(v).equals(tunnelCanGet)) {
						v.ambIn();
					}
				}
			}
			this.vTot.put(vehicle, tunnelCanGet);
			//System.out.println(vehicle+" get in "+tunnelCanGet);
			tunnelCanGet.tryToEnter(vehicle);
			//System.out.println(getin);
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
			//System.out.println(vehicle);
			//System.out.println(vehicle.getPriority());
			if(bt.getAmbulanceCount()==0) {
				if(vehicle instanceof Ambulance) {
					return bt;
					/*if(bt.getDirection().equals(vehicle.getDirection())) {
						return bt;
					}*/
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
	
	@Override
	public Tunnel getTunnel(Vehicle vehicle) {
		if(vTot.containsKey(vehicle)) {
			return vTot.get(vehicle);
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
	public void exitTunnelInner(Vehicle vehicle) {
		lock.lock();
		try {
			BasicTunnel tunnel = (BasicTunnel) this.vTot.get(vehicle);
			tunnel.exitTunnel(vehicle);
			this.canEnterTunnel.signalAll();
			this.vTot.remove(vehicle);
			if (vehicle instanceof Ambulance) {
				for (Vehicle v : vTot.keySet()) {
					if(vTot.get(v).equals(tunnel)) {
						v.ambOut();
					}
				}
			}
			//System.out.println(vehicle+" exit!");
		}finally {
			lock.unlock();
		}
	}
	
}

