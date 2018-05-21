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
		//check if elevator can't go any further
		if (	(e.currentFloor == 0 && e.direction == -1) || //can't go further down
				(e.currentFloor == EventDriver.ed.numFloors - 1 && e.direction == 1)){ //can't go up
			//set direction to be opposite
			e.direction *= -1;
			//if it doesn't have any stops in the opposite direction, set it idle
			if (!e.hasMoreStops()){
				e.direction = 0;
				return;
			}
			//otherwise, continue with elevator movement
		}
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
