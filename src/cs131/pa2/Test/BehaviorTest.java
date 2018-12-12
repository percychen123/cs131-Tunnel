package cs131.pa2.Test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cs131.pa2.Abstract.Direction;
import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.Event;
import cs131.pa2.Abstract.Log.EventType;

public class BehaviorTest {

    @Before
    public void setUp() {
        Tunnel.DEFAULT_LOG.clearLog();
    }

    @BeforeClass
    public static void broadcast() {
        System.out.printf("Running Behavior Tests using %s \n", TestUtilities.factory.getClass().getCanonicalName());
    }

    /**
     * Vehicle RollCall checks the basic functions of an vehicle. Note if the test
     * does not pass neither will any other test *
     */
    @Test
    public void Vehicle_RollCall() {

        for (Direction direction : Direction.values()) {
            Vehicle car = TestUtilities.factory.createNewCar(TestUtilities.gbNames[0], direction);
            Vehicle sled = TestUtilities.factory.createNewSled(TestUtilities.gbNames[1], direction);

            assertTrue("car is the wrong direction", car.getDirection().equals(direction));
            assertTrue("sled is the wrong industry", sled.getDirection().equals(direction));

            assertTrue("car has the wrong name", car.getName().equals(TestUtilities.gbNames[0]));
            assertTrue("sled has the wrong name", sled.getName().equals(TestUtilities.gbNames[1]));

            assertTrue("car has the wrong priority", car.getPriority() == 0);
            assertTrue("sled has the wrong priority", sled.getPriority() == 0);

            assertTrue("car toString does not function as expected", String.format("%s %s %s", direction, TestUtilities.carName, TestUtilities.gbNames[0]).equals(car.toString()));
            assertTrue("sled toString does not function as expected", String.format("%s %s %s", direction, TestUtilities.sledName, TestUtilities.gbNames[1]).equals(sled.toString()));

        }
    }
    
    @Test
    public void Tunnel_Basic() {
        Tunnel tunnel = TestUtilities.factory.createNewBasicTunnel(TestUtilities.mrNames[0]);
        assertTrue("Tunnel has the wrong name", TestUtilities.mrNames[0].equals(tunnel.getName()));
        assertTrue("Tunnel toString does not function as expected", String.format("%s", TestUtilities.mrNames[0]).equals(tunnel.toString()));
    }

    @Test
    public void car_Enter() {
        Vehicle car = TestUtilities.factory.createNewCar(TestUtilities.gbNames[0], Direction.random());
        Tunnel tunnel = TestUtilities.factory.createNewBasicTunnel(TestUtilities.mrNames[0]);
        TestUtilities.VehicleEnters(car, tunnel);
        Event logEvent = Tunnel.DEFAULT_LOG.get();
        assertTrue("Tunnel log did not record vehicle entering tunnel", new Event(car, tunnel, EventType.ENTER_ATTEMPT).weakEquals(logEvent));
        logEvent = Tunnel.DEFAULT_LOG.get();
        assertTrue("Tunnel log did not record vehicle entering tunnel", new Event(car, tunnel, EventType.ENTER_SUCCESS).weakEquals(logEvent));
        logEvent = Tunnel.DEFAULT_LOG.get();
        assertTrue("Tunnel log did not record vehicle leaving tunnel", new Event(car, tunnel, EventType.LEAVE_START).weakEquals(logEvent));
        logEvent = Tunnel.DEFAULT_LOG.get();
        assertTrue("Tunnel log did not record vehicle leaving tunnel", new Event(car, tunnel, EventType.LEAVE_END).weakEquals(logEvent));
    }
    

    @Test
    public void sled_Enter() {
        Vehicle sled = TestUtilities.factory.createNewSled(TestUtilities.gbNames[0], Direction.random());
        Tunnel tunnel = TestUtilities.factory.createNewBasicTunnel(TestUtilities.mrNames[0]);
        TestUtilities.VehicleEnters(sled, tunnel);
        Event logEvent = Tunnel.DEFAULT_LOG.get();
        assertTrue("Tunnel log did not record sled entering tunnel", new Event(sled, tunnel, EventType.ENTER_ATTEMPT).weakEquals(logEvent));
        logEvent = Tunnel.DEFAULT_LOG.get();
        assertTrue("Tunnel log did not record sled entering tunnel", new Event(sled, tunnel, EventType.ENTER_SUCCESS).weakEquals(logEvent));
        logEvent = Tunnel.DEFAULT_LOG.get();
        assertTrue("Tunnel log did not record sled entering tunnel", new Event(sled, tunnel, EventType.LEAVE_START).weakEquals(logEvent));
        logEvent = Tunnel.DEFAULT_LOG.get();
        assertTrue("Tunnel log did not record sled entering tunnel", new Event(sled, tunnel, EventType.LEAVE_END).weakEquals(logEvent));
    }

    @Test
    public void Direction_Constraint() {
        Vehicle car = TestUtilities.factory.createNewCar(TestUtilities.gbNames[0], Direction.NORTH);
        Vehicle violator = TestUtilities.factory.createNewCar(TestUtilities.gbNames[1], Direction.SOUTH);
        Tunnel tunnel = TestUtilities.factory.createNewBasicTunnel(TestUtilities.mrNames[0]);
        boolean canUse = tunnel.tryToEnter(car);
        assertTrue(String.format("%s cannot use", car), canUse);
        canUse = tunnel.tryToEnter(violator);
        assertTrue(String.format("%s is using with %s. Violates industry constraint", violator, car), !canUse);
    }

    @Test
    public void Multiple_cars() {
    		Vehicle nick = TestUtilities.factory.createNewCar(TestUtilities.gbNames[0], Direction.NORTH);
        Vehicle peter = TestUtilities.factory.createNewCar(TestUtilities.gbNames[0], Direction.NORTH);
        Vehicle ray = TestUtilities.factory.createNewCar(TestUtilities.gbNames[1], Direction.NORTH);
        Vehicle walter = TestUtilities.factory.createNewCar(TestUtilities.gbNames[7], Direction.NORTH);
        Tunnel tunnel = TestUtilities.factory.createNewBasicTunnel(TestUtilities.mrNames[0]);
        boolean canUse = tunnel.tryToEnter(peter);
        assertTrue(String.format("%s cannot use", peter), canUse);
        canUse = tunnel.tryToEnter(ray);
        assertTrue(String.format("%s is not using with %s.", peter, ray), canUse);
        canUse = tunnel.tryToEnter(nick);
        assertTrue(String.format("%s is not using with %s and %s.", nick, peter, ray), canUse);
        canUse = tunnel.tryToEnter(walter);
        assertTrue(String.format("%s is using with %s, %s and %s violates number constraint.", walter, peter, ray, nick), !canUse);
        peter.doWhileInTunnel();
        tunnel.exitTunnel(peter);
        ray.doWhileInTunnel();
        tunnel.exitTunnel(ray);
        canUse = tunnel.tryToEnter(walter);
        assertTrue(String.format("%s cannot use, %s and %s did not leave tunnel.", walter, peter, ray), canUse);
    }
}
