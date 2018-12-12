package cs131.pa2.Test;

import cs131.pa2.Abstract.Factory;
import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.CarsTunnels.ConcreteFactory;

import static org.junit.Assert.assertTrue;

public class TestUtilities {
    //Change the import to use your concreteFactory and nothing else
    
    public static final String carName = "CAR";
    public static final String sledName = "SLED";
    //Names used in testing
    public static final String[] gbNames = {"VENKMAN", "STANTZ", "SPENGLER", "ZEDDEMORE", "BARRETT", "TULLY", "MELNITZ", "PECK", "LENNY", "GOZER", "SLIMER", "STAY PUFT", "GATEKEEPER", "KEYMASTER"};
    public static final String[] mrNames = {"CATSKILL", "ROCKY", "APPALACHIAN", "OLYMPIC", "HIMALAYA", "GREAT DIVIDING", "TRANSANTRIC", "URAL", "ATLAS", "ALTAI", "CARPATHIAN", "KJOLEN", "BARISAN", "COAST", "QIN", "WESTERN GHATS"};
    
    public static final Factory factory = new ConcreteFactory();
    
    public static void VehicleEnters(Vehicle vehicle, Tunnel tunnel) {
        boolean canEnter = tunnel.tryToEnter(vehicle);
        assertTrue(String.format("%s cannot use", vehicle), canEnter);
        vehicle.doWhileInTunnel();
        tunnel.exitTunnel(vehicle);
    }
   
}
