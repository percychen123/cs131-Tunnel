package cs131.pa2.Abstract;

import java.util.Collection;

import cs131.pa2.Abstract.Log.Log;

public interface Factory {

    public abstract Tunnel createNewBasicTunnel(String label);

    public abstract Tunnel createNewPriorityScheduler(String label, Collection<Tunnel> tunnels, Log log);

    public abstract Vehicle createNewCar(String label, Direction direction);

    public abstract Vehicle createNewSled(String label, Direction direction);
    
    public abstract Vehicle createNewAmbulance(String label, Direction direction);
    
    public abstract Tunnel createNewPreemptivePriorityScheduler(String label, Collection<Tunnel> tunnels, Log log);

}
