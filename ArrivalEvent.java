//Event of a new person arriving
public class ArrivalEvent extends Event{
	Person person; //which person is arriving with this event
	int floor; //which floor he arrives at
	
	public ArrivalEvent(double triggerTime, double duration, Person p, int floor) {
		super("Person " + p.id + " arrives at floor " + floor, triggerTime, duration);
		person = p;
		this.floor = floor;
	}
	
	@Override
	public void execute(){
		//this person will arrive at the designated floor
		EventDriver.ed.floors[floor].addPerson(person);
	}
}
