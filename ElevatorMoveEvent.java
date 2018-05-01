
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
	}

}
