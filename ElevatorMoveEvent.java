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
		//System.out.println(e.direction);
		//System.out.println(e.hasMoreStops());
		//move to next floor
		e.move();
		//after arriving at the floor, check if this is one of this elevator's
		//assigned floors. if so, start boarding and exiting people
		if (e.stopFloors[e.currentFloor]){
			EventDriver.ed.exitAndBoard(e, e.currentFloor);
			//do nothing else, exitAndBoard method will tell elevator
			//whether and how to move
		}
		else{
			//if currentFloor wasn't an assigned stop, keep moving,
			//this means there's an assigned stop further in this direction
			EventDriver.ed.moveElevator(e, triggerTime + duration);
		}
	}

}
