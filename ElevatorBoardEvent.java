/*
 * CS381 Modeling and Simulation
 * Elevator Simulation Final Project
 * Authors: Zaheen Ahmed 
 * 			Jun Young Cheong
 */
public class ElevatorBoardEvent extends Event {

	Person p; //the person boarding the elevator
	Elevator e; //the elevator being boarded
	public ElevatorBoardEvent(double triggerTime, double duration, Person p, Elevator e) {
		super("Person " + p.id + " enters elevator " + e.id 
				+ ", wants to go to floor " + p.desiredFloor, 
				triggerTime, 
				duration
			);
		this.p = p;
		this.e = e;
	}

	@Override
	public void execute() {
		Floor f = EventDriver.ed.floors[e.currentFloor];
		//this person is first one in line at floor f
		f.queue.removeFirst(); //leave floor f
		e.board(p); //enter elevator e
		EventDriver.ed.assignElevator(e.currentFloor, e);
	}

}
