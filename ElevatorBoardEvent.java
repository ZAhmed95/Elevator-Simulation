/*
 * CS381 Modeling and Simulation
 * Elevator Simulation Final Project
 * Authors: Zaheen Ahmed 
 * 			Jun Young Cheong
 */
public class ElevatorBoardEvent extends Event {

	Person p; //the person boarding the elevator
	Elevator e; //the elevator being boarded
	int direction; //direction this person is going
	public ElevatorBoardEvent(double triggerTime, double duration, Person p, Elevator e, int d) {
		super("Person " + p.id + " enters elevator " + e.id 
				+ ", wants to go to floor " + p.desiredFloor, 
				triggerTime, 
				duration
			);
		this.p = p;
		this.e = e;
		this.direction = d;
	}

	@Override
	public void execute() {
		Floor f = EventDriver.ed.floors[e.currentFloor];
		//if direction is up:
		if (direction > 0){
			//remove person from upQueue
			f.upQueue.removeFirst();
		}
		else{
			//remove from downQueue
			f.downQueue.removeFirst();
		}
		
		e.board(p); //enter elevator e
	}

}