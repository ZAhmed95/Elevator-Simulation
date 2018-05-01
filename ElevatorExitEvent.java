
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
	}

}
