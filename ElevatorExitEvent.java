/*
 * CS381 Modeling and Simulation
 * Elevator Simulation Final Project
 * Authors: Zaheen Ahmed 
 * 			Jun Young Cheong
 */
public class ElevatorExitEvent extends Event {

	Person p; //the person getting off
	Elevator e; //the elevator that is being exited
	
	public ElevatorExitEvent(double triggerTime, double duration, Person p, Elevator e) {
		super("Person " + p.id + " exits elevator " + e.id +
				" at floor " + e.currentFloor, triggerTime, duration);
		this.p = p;
		this.e = e;
	}

	@Override
	public void execute() {
		e.occupants.remove(p);
		//calculate how much time this person waited until now
		double waitTime = triggerTime+duration - p.arrivalTime;
		//add it to the stats
		EventDriver.ed.stat.totalWaitTime += waitTime;
	}

}
