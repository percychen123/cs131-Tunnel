package cs131.pa2.Abstract;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs131.pa2.Abstract.Log.EventType;
import cs131.pa2.Abstract.Log.Log;
import cs131.pa2.CarsTunnels.Ambulance;


/**
 * A Tunnel is an object which can be entered by vehicles. Vehicles
 * themselves are responsible for indicating when they want to enter
 * and when they are ready to leave. Tunnels are responsible for
 * indicating if it is safe for a Vehicle to enter.
 *
 * When a Vehicle wants to enter a Tunnel, it calls tryToEnter on the
 * Tunnel instance. If the Vehicle has entered the Tunnel
 * successfully, tryToEnter returns true. Otherwise, tryToEnter
 * returns false. The Vehicle simulates the time spent in the tunnel,
 * and then must call exitTunnel on the same Tunnel instance it
 * entered.
 */
public abstract class Tunnel {
	
	
	private final String name;
	public static Log DEFAULT_LOG = new Log();
    private final Log log;
    
    public Tunnel(String name, Log log) {
        this.name = name;
        this.log = log;
    }

    public Tunnel(String name) {
        this(name, Tunnel.DEFAULT_LOG);
    }
    
    public final boolean tryToEnter(Vehicle vehicle) {
        //Do not overwrite this function, you should be overwriting tryToEnterInner
        int sig = log.nextLogEventNumber();
        log.addToLog(vehicle, this, EventType.ENTER_ATTEMPT, sig);
        if (this.tryToEnterInner(vehicle)) {
            log.addToLog(vehicle, this, EventType.ENTER_SUCCESS, sig);
            return true;
        } else {
            log.addToLog(vehicle, this, EventType.ENTER_FAILED, sig);
            return false;
        }
    }
    
    /**
     * Vehicle tries to enter a tunnel.
     *
     * @param  vehicle The vehicle that is attempting to enter
     * @return true if the vehicle was able to enter, false otherwise
     */
    public abstract boolean tryToEnterInner(Vehicle vehicle);
    
    public final void exitTunnel(Vehicle vehicle) {
        //Do not overwrite this function, you should be overwriting disconnectInner
        int sig = log.nextLogEventNumber();
        this.log.addToLog(vehicle, this, EventType.LEAVE_START, sig);
        this.exitTunnelInner(vehicle);
        this.log.addToLog(vehicle, this, EventType.LEAVE_END, sig);
    }
    
    /**
     * Vehicle exits the tunnel.
     * 
     * @param vehicle The vehicle that is exiting the tunnel
     */
    public abstract void exitTunnelInner(Vehicle vehicle);


    /**
     * Returns the name of this tunnel
     *
     * @return The name of this tunnel
     */
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return String.format("%s", this.name);
    }
    
    
    public abstract Tunnel getTunnel(Vehicle vehicle);

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Tunnel other = (Tunnel) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }
}
