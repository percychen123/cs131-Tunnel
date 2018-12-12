package cs131.pa2.Test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;

import org.junit.Test;

import cs131.pa2.Abstract.Direction;
import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.Event;
import cs131.pa2.Abstract.Log.EventType;
import cs131.pa2.Abstract.Log.Log;
import cs131.pa2.CarsTunnels.Ambulance;
import cs131.pa2.CarsTunnels.Car;

public class PrioritySchedulerTest {

    private final String prioritySchedulerName = "SCHEDULER";
    private final String preemptivePrioritySchedulerName = "PREEMPTIVE_SCHEDULER";

    @Before
    public void setUp() {
        Tunnel.DEFAULT_LOG.clearLog();
    }

    @BeforeClass
    public static void broadcast() {
        System.out.printf("Running Priority Scheduler Tests using %s \n", TestUtilities.factory.getClass().getCanonicalName());
    }

    private Tunnel setupSimplePriorityScheduler(String name) {
        Collection<Tunnel> tunnels = new ArrayList<Tunnel>();
        tunnels.add(TestUtilities.factory.createNewBasicTunnel(name));
        return TestUtilities.factory.createNewPriorityScheduler(prioritySchedulerName, tunnels, new Log());
    }
    
    private Tunnel setupPreemptivePriorityScheduler(String name) {
    	Collection<Tunnel> tunnels = new ArrayList<Tunnel>();
    	tunnels.add(TestUtilities.factory.createNewBasicTunnel(name));
    	return TestUtilities.factory.createNewPreemptivePriorityScheduler(preemptivePrioritySchedulerName, tunnels, new Log());
    }
    
    private Tunnel setupPreemptivePrioritySchedulerTwoTunnels(String name1, String name2) {
    	Collection<Tunnel> tunnels = new ArrayList<Tunnel>();
    	tunnels.add(TestUtilities.factory.createNewBasicTunnel(name1));
    	tunnels.add(TestUtilities.factory.createNewBasicTunnel(name2));
    	return TestUtilities.factory.createNewPreemptivePriorityScheduler(preemptivePrioritySchedulerName, tunnels, new Log());
    }

    @Test
    public void Car_Enter() {
        Vehicle car = TestUtilities.factory.createNewCar(TestUtilities.gbNames[0], Direction.random());
        Tunnel tunnel = setupSimplePriorityScheduler(TestUtilities.mrNames[0]);
        TestUtilities.VehicleEnters(car, tunnel);
    }

    @Test
    public void Sled_Enter() {
    	Vehicle sled = TestUtilities.factory.createNewSled(TestUtilities.gbNames[0], Direction.random());
        Tunnel tunnel = setupSimplePriorityScheduler(TestUtilities.mrNames[0]);
        TestUtilities.VehicleEnters(sled, tunnel);
    }
    
    @Test
    public void Ambulance_Enter() {
    	Vehicle ambulance = TestUtilities.factory.createNewAmbulance(TestUtilities.gbNames[0], Direction.random());
        Tunnel tunnel = setupSimplePriorityScheduler(TestUtilities.mrNames[0]);
        TestUtilities.VehicleEnters(ambulance, tunnel);
    }
    
    @Test
    public void Priority() {
    	List<Thread> vehicleThreads = new ArrayList<Thread>();
        Tunnel priorityScheduler = setupSimplePriorityScheduler(TestUtilities.mrNames[0]);
		for (int i=0; i<7; i++) {
			Vehicle car = TestUtilities.factory.createNewCar(Integer.toString(i), Direction.NORTH);
            car.addTunnel(priorityScheduler);
            if (i<3) {
				car.setPriority(4);
			}
			else {
				car.setPriority(i-3);
			}
            Thread sharedThread = new Thread(car);
            sharedThread.start();
            vehicleThreads.add(sharedThread);
		}
		for (Thread t: vehicleThreads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Tunnel.DEFAULT_LOG.addToLog(EventType.END_TEST);
		Log log = Tunnel.DEFAULT_LOG;
		Event currentEvent;
		int i=0;
		Vehicle lastEnteredVehicle = null;
		do {
			currentEvent = log.get();
			if(currentEvent.getEvent() == EventType.ENTER_SUCCESS) {
				//System.err.println(currentEvent);
				if(i++ > 2) {
					if (lastEnteredVehicle == null) {
						lastEnteredVehicle = currentEvent.getVehicle();
					}
					else if (currentEvent.getVehicle().getPriority() > lastEnteredVehicle.getPriority()){
						assertTrue("Vehicle "+currentEvent.getVehicle() + " has higher priority than "+lastEnteredVehicle + " and should run before!", false);
					}
				}
			}
		} while (!currentEvent.getEvent().equals(EventType.END_TEST));    		
    }
    
    @Test
    public void PreemptivePriority() {
    	List<Thread> vehicleThreads = new ArrayList<Thread>();
    	Tunnel preemptivePriorityScheduler = setupPreemptivePriorityScheduler(TestUtilities.mrNames[0]);
        // start 3 fast cars
        for (int i=0; i<3; i++) {
			Vehicle car = TestUtilities.factory.createNewCar(Integer.toString(i), Direction.NORTH);
			car.setSpeed(8);
            car.addTunnel(preemptivePriorityScheduler);
            Thread sharedThread = new Thread(car);
            sharedThread.start();
            vehicleThreads.add(sharedThread);
        }
        try {
			Thread.sleep(50);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
        // start one slow ambulance
        for (int i=0; i<1; i++) {
	        Vehicle ambulance = TestUtilities.factory.createNewAmbulance("AMB"+i, Direction.values()[i % Direction.values().length]);
	        ambulance.setSpeed(0);
	        ambulance.addTunnel(preemptivePriorityScheduler);
	        Thread ambulanceThread = new Thread(ambulance);
	        ambulanceThread.start();
	        vehicleThreads.add(ambulanceThread);
        }
        for(Thread t : vehicleThreads) {
        	try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    	// make sure that nobody exits the tunnel until ambulances exit
        Tunnel.DEFAULT_LOG.addToLog(EventType.END_TEST);
		Log log = Tunnel.DEFAULT_LOG;
		Event currentEvent;
		Vehicle ambulance=null;
		Vehicle [] cars = new Vehicle[3];
		for(int i=0; i<3; i++)
			cars[i] = null;
		boolean ambulanceLeft=false;
		do {
			currentEvent = log.get();
			if (currentEvent.getEvent() == EventType.ENTER_SUCCESS && currentEvent.getVehicle() instanceof Ambulance) {
				ambulance = currentEvent.getVehicle();
			}
			if (currentEvent.getEvent() == EventType.ENTER_SUCCESS && currentEvent.getVehicle() instanceof Car) {
				switch(currentEvent.getVehicle().getName()){
				case "0":
					cars[0] = currentEvent.getVehicle();
					break;
				case "1":
					cars[1] = currentEvent.getVehicle();
					break;
				case "2":
					cars[2] = currentEvent.getVehicle();
					break;
				default:
					assertTrue("Wrong vehicle entered tunnel!", false);
					break;
				}
			}
			if(currentEvent.getEvent() == EventType.LEAVE_START) {
				if(currentEvent.getVehicle() instanceof Car && !ambulanceLeft) {
					assertTrue("Vehicle "+currentEvent.getVehicle() + " left tunnel while ambulance was still running!", false);
				}
				if(currentEvent.getVehicle() instanceof Ambulance)
					ambulanceLeft = true;
			}
			System.out.println(currentEvent.toString());
		} while (!currentEvent.getEvent().equals(EventType.END_TEST));
		if(ambulance == null | cars[0] == null || cars[1] == null || cars[2] == null) {
			assertTrue("Vehicles did not enter tunnel successfully!", false);
		}
    }
    
    @Test
    public void PreemptivePriorityManyAmb() {
    	List<Thread> vehicleThreads = new ArrayList<Thread>();
    	Tunnel preemptivePriorityScheduler = setupPreemptivePriorityScheduler(TestUtilities.mrNames[0]);
        // start 3 slow cars
        for (int i=0; i<3; i++) {
			Vehicle car = TestUtilities.factory.createNewCar(Integer.toString(i), Direction.NORTH);
			car.setSpeed(0);
            car.addTunnel(preemptivePriorityScheduler);
            Thread sharedThread = new Thread(car);
            sharedThread.start();
            vehicleThreads.add(sharedThread);
        }
        try {
			Thread.sleep(50);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
        // start 4 fast ambulances
        for (int i=0; i<4; i++) {
	        Vehicle ambulance = TestUtilities.factory.createNewAmbulance("AMB"+i, Direction.values()[i % Direction.values().length]);
	        ambulance.setSpeed(9);
	        ambulance.addTunnel(preemptivePriorityScheduler);
	        Thread ambulanceThread = new Thread(ambulance);
	        ambulanceThread.start();
	        vehicleThreads.add(ambulanceThread);
	        try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        for(Thread t : vehicleThreads) {
        	try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    	// make sure that nobody exits the tunnel until ambulances exit
        Tunnel.DEFAULT_LOG.addToLog(EventType.END_TEST);
		Log log = Tunnel.DEFAULT_LOG;
		Event currentEvent;
		Vehicle ambulances[] = new Vehicle[4];
		Vehicle cars[] = new Vehicle[3];
		for (int i=0; i<4; i++)
			ambulances[i] = null;
		for (int i=0; i<3; i++)
			cars[i] = null;		
		int ambulancesLeft = 0;
		do {
			currentEvent = log.get();
			if (currentEvent.getEvent() == EventType.ENTER_SUCCESS && currentEvent.getVehicle() instanceof Ambulance) {
				switch(currentEvent.getVehicle().getName()) {
				case "AMB0":
					ambulances[0] = currentEvent.getVehicle();
					break;
				case "AMB1":
					ambulances[1] = currentEvent.getVehicle();
					break;
				case "AMB2":
					ambulances[2] = currentEvent.getVehicle();
					break;
				case "AMB3":
					ambulances[3] = currentEvent.getVehicle();
					break;
				default:
					assertTrue("Wrong vehicle entered tunnel!", false);
					break;
				}
			}
			if (currentEvent.getEvent() == EventType.ENTER_SUCCESS && currentEvent.getVehicle() instanceof Car) {
				switch(currentEvent.getVehicle().getName()){
				case "0":
					cars[0] = currentEvent.getVehicle();
					break;
				case "1":
					cars[1] = currentEvent.getVehicle();
					break;
				case "2":
					cars[2] = currentEvent.getVehicle();
					break;
				default:
					assertTrue("Wrong vehicle entered tunnel!", false);
					break;
				}
			}
			if(currentEvent.getEvent() == EventType.LEAVE_START) {
				if(currentEvent.getVehicle() instanceof Car && ambulancesLeft < 4) {
					assertTrue("Vehicle "+currentEvent.getVehicle() + " left tunnel while " + ((4 -ambulancesLeft) ==1 ? " 1 ambulance was" : (4 -ambulancesLeft)+" ambulances were") + " still running!", false);
				}
				if(currentEvent.getVehicle() instanceof Ambulance)
					ambulancesLeft++;
			}
			System.out.println(currentEvent.toString());
		} while (!currentEvent.getEvent().equals(EventType.END_TEST));
		for(int i=0; i<4; i++)
			if(ambulances[i] == null)
				assertTrue("Ambulances did not enter tunnel successfully!", false);
		for(int i=0; i<3; i++)
			if(cars[i] == null)
				assertTrue("Cars did not enter tunnel successfully!", false);
    }
    
    @Test
    public void PreemptivePriorityManyTunnels() {
    	List<Thread> vehicleThreads = new ArrayList<Thread>();
    	Tunnel preemptivePriorityScheduler = setupPreemptivePrioritySchedulerTwoTunnels(TestUtilities.mrNames[0], TestUtilities.mrNames[1]);
        // start a fast car in first tunnel
		Vehicle car1 = TestUtilities.factory.createNewCar("0", Direction.NORTH);
		car1.setSpeed(9);
        car1.addTunnel(preemptivePriorityScheduler);
        Thread car1Thread = new Thread(car1);
        car1Thread.start();
        vehicleThreads.add(car1Thread);
        // start a fast car in second tunnel
 		Vehicle car2 = TestUtilities.factory.createNewCar("1", Direction.SOUTH);
 		car2.setSpeed(9);
 		car2.addTunnel(preemptivePriorityScheduler);
        Thread car2Thread = new Thread(car2);
        car2Thread.start();
        vehicleThreads.add(car2Thread);
        try {
			Thread.sleep(50);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
        // start a slow ambulance
        Vehicle ambulance = TestUtilities.factory.createNewAmbulance("AMB0", Direction.NORTH);
        ambulance.setSpeed(0);
        ambulance.addTunnel(preemptivePriorityScheduler);
        Thread ambulanceThread = new Thread(ambulance);
        ambulanceThread.start();
        vehicleThreads.add(ambulanceThread);
        for(Thread t : vehicleThreads) {
        	try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        Tunnel.DEFAULT_LOG.addToLog(EventType.END_TEST);
		Log log = Tunnel.DEFAULT_LOG;
		Event currentEvent;
		ambulance=null; car1=null; car2=null;
		Tunnel ambulanceTunnel=null;
		Tunnel car1Tunnel = null;
		Vehicle lonelyCar = null;
		boolean ambulanceLeft=false, carLonelyTunnelLeft=false;
		do {
			currentEvent = log.get();
			if (currentEvent.getEvent() == EventType.ENTER_SUCCESS && currentEvent.getVehicle() instanceof Ambulance) {
				ambulance = currentEvent.getVehicle();
				ambulanceTunnel = currentEvent.getTunnel();
				lonelyCar = (car1Tunnel == ambulanceTunnel ? car2: car1);
			}
			if (currentEvent.getEvent() == EventType.ENTER_SUCCESS && currentEvent.getVehicle() instanceof Car) {
				switch(currentEvent.getVehicle().getName()){
				case "0":
					car1 = currentEvent.getVehicle();
					car1Tunnel = currentEvent.getTunnel();
					break;
				case "1":
					car2 = currentEvent.getVehicle();
					break;
				default:
					assertTrue("Wrong vehicle entered tunnel!", false);
					break;
				}
			}
			if(currentEvent.getEvent() == EventType.LEAVE_START) {
				if(currentEvent.getVehicle() instanceof Car && currentEvent.getTunnel().getName() == ambulanceTunnel.getName() && !ambulanceLeft) {
					assertTrue("Vehicle "+currentEvent.getVehicle() + " left tunnel while ambulance was still running!", false);
				}
				if(currentEvent.getVehicle() instanceof Car && currentEvent.getTunnel().getName() != ambulanceTunnel.getName()) {
					assertTrue("Car "+ currentEvent.getVehicle().getName() + " should be in the other Tunnel" , currentEvent.getVehicle() == lonelyCar);
					carLonelyTunnelLeft = true;
				}
				if(currentEvent.getVehicle() instanceof Ambulance) {
					ambulanceLeft = true;
					// at this point, car in the other tunnel must have left!
					if(!carLonelyTunnelLeft)
						assertTrue("Car "+ lonelyCar.getName() + " should not wait for ambulance to exit, since they are in different tunnels", false);
				}
			}
			System.out.println(currentEvent.toString());
		} while (!currentEvent.getEvent().equals(EventType.END_TEST));
		if(ambulance == null | car1 == null || car2 == null) {
			assertTrue("Vehicles did not enter tunnel successfully!", false);
		}
    }
}
