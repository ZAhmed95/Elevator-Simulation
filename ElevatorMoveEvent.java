/*
 * CS381 Modeling and Simulation
 * Elevator Simulation Final Project
 * Authors: Zaheen Ahmed 
 * 			Jun Young Cheong
 */
public class ElevatorMoveEvent extends Event {

	Elevator e; //which elevator is performing this event
	public ElevatorMoveEvent(double triggerTime, double duration, Elevator e) {
		super("Elevator " + e.id + " moves from floor " 
				+ e.currentFloor + " to floor " + (e.currentFloor + e.direction), 
				triggerTime,
				duration
			 );
		this.e = e;
	}

	@Override
	public void execute() {
		e.move();
	    EventDriver.ed.moveElevator(e);
	}

}
