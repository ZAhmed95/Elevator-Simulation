/*
 * CS381 Modeling and Simulation
 * Elevator Simulation Final Project
 * Authors: Zaheen Ahmed 
 * 			Jun Young Cheong
 */
import java.util.ListIterator;

public class EventDriver {
	static EventDriver ed; //a static reference to the latest instance of EventDriver,
	//to help other classes have easy access to it
	static int maxArrivals = 10;
	Floor[] floors; //array of all floors in building
	int numFloors; //total number of floors
	Elevator[] elevators; //array of all elevators in building
	int numElevators; //total number of elevators
	double time; //current simulation time, in seconds
	//LinkedList<Person> arrivals; //future arriving people (? might remove this)
	SortedLinkedList<Event> events;
	Statistics stat;
	

	
	//Constants used in the simulation
	final double elevatorBoardTime = 0.1;
	final double elevatorExitTime = 0.1;
	final double customerArrivalLambda = 5;
	final double elevatorMoveTime = 0.2;
	
	
	public EventDriver(int numFloors, int numElevators){
		ed = this;
		this.numFloors = numFloors;
		this.numElevators = numElevators;
		floors = new Floor[numFloors];
		for (int i = 0; i < numFloors; i++){
			floors[i] = new Floor(i);
		}
		elevators = new Elevator[numElevators];
		for (int i = 0; i < numElevators; i++){
			elevators[i] = new Elevator(i, 0, 10);
		}
		time = 0;
		events = new SortedLinkedList<Event>();
		stat = new Statistics();
	}
	
	//simulate up to <maxArrivals> number of arriving customers
	public void start(){
		//keep loop going as long as either:
		//a) we haven't reached maxArrivals
		//b) there's still events waiting to be processed
		while(Statistics.personCount < maxArrivals || !events.isEmpty()){
			if (Statistics.personCount < maxArrivals){
				//generate a person's arrival
				nextPerson();
			}
			//execute all waiting events
			while(events.size() > 0)
				nextEvent();
			//make elevator decisions
			controlElevators();
		}
		
		//log average wait time
		System.out.println("Average person's wait time: " + stat.averageWaitTime());
	}
	
	void nextPerson(){
		double nextArrivalTime = time + stat.expRandom(customerArrivalLambda);
		int startFloor = chooseFloor();
		int targetFloor = startFloor;
		//make sure person's target floor is not equal to their start floor
		while (targetFloor == startFloor){
			targetFloor = stat.randInt(0, numFloors);
		}
		//create a new person
		Person p = new Person(targetFloor);
		//create a new ArrivalEvent
		Event e = new ArrivalEvent(nextArrivalTime, 0, p, startFloor);
		//add event to the list
		events.sortedAdd(e);
	}
	
	int chooseFloor(){
		//new arrivals have 50% chance of arriving at lobby (floor 0),
		//and uniform chance to arrive at any other floor
		if (stat.randInt(0, 2) == 0){
			return 0; //50% chance of floor 0
		}
		//50% chance of a uniform choice between floor 1 to floor (numFloors-1)
		return stat.randInt(1, numFloors);
	}
	
	void nextEvent(){
		if (events.isEmpty()){
			System.out.println("No events in queue.");
			return;
		}
		//get next event in queue
		Event next = events.removeFirst();
		//calculate event start time and end time
		double startTime = next.triggerTime;
		double endTime = startTime + next.duration;
		//log event message
		System.out.println(
				"(time = " + startTime + ") " //event start time
				+ next.getMessage() //event message
				+ " (end = " + endTime + ")" //event end time
			);
		//execute event
		next.execute();
		//update time if time < endTime
		//this check is necessary for bookkeeping purposes, since it's possible
		//for one event to happen while another is progressing
		//e.g. a new person arrives while an elevator is moving
		//so we need to log the correct times
		if (time < endTime)
			time = endTime;
	}
	
	//TODO: the controlElevators method has a lot of boilerplate, try to clean it up
	//this method is used to signal elevators to move up or down
	//Specifics: this method should change (or not) the 'direction' member variable
	//of each elevator, after doing whatever logic to determine said result.
	//if an elevator should ever become idle (for lack of requests), send it to the lobby
	//i.e. set e.stopFloors[0] = true (unless it's already in the lobby)
	void controlElevators(){
		//in the finished project, the controller will smartly choose
		//how to move the elevators efficiently
		
		for (int i = 0; i < elevators.length; i++) {
			Elevator e = elevators[i];
			//if the current floor the elevator is on has requests,
			//i.e. people wanted to get off or on, create elevator Exit and Board events
			//as necessary
			if (e.stopFloors[e.currentFloor]){
				//create an exit event for every person in the elevator who wants to get off
				//at this floor
				ListIterator<Person> itr = e.occupants.listIterator();
				Person p;
				double wait = 0;
				while(itr.hasNext()){
					p = itr.next();
					if (p.desiredFloor == e.currentFloor){
						events.add(new ElevatorExitEvent(time+wait, elevatorExitTime, p, e));
						wait += elevatorExitTime; //people have to go out one by one
					}
				}
				
				//TODO: Take a look at this. Might not belong in this spot.
				//if the elevator is idle (only happens in lobby), set direction to 1
				if (e.direction == 0)
					e.direction = 1;
				//all exit events have been scheduled, now see who wants to board
				itr = floors[e.currentFloor].queue.listIterator();
				wait = 0;
				while(itr.hasNext()){
					p = itr.next();
					//if the direction the elevator is heading matches the direction
					//this person wants to go:
					if ((p.desiredFloor - e.currentFloor)*e.direction > 0){
						//create a boarding event
						events.add(new ElevatorBoardEvent(time+wait, elevatorBoardTime, p, e));
						wait += elevatorBoardTime; //people must enter one by one
					}
				}
				//elevator boarding and exiting events scheduled, now change some flags:
				e.stopFloors[e.currentFloor] = false; //elevator finished it's business at this floor
				if (e.direction == 1)
					floors[e.currentFloor].up = false;
				else if (e.direction == -1)
					floors[e.currentFloor].down = false;
				//now create an elevator move event to get it moving
				//events.sortedAdd(new ElevatorMoveEvent(time+wait, elevatorBoardTime, e));
				//TODO: above statement causes a bug, figure out why
				return;
			}
			//if the elevator is going up and it has more stops above, keep going
			//same for down
			int direction = e.direction;
			boolean requested = false;
			//in our design, an elevator is only ever idle (direction = 0) in the lobby
			if (direction == 0){
				//search through floors to check if there are any requests
				for(int j = 0; j < floors.length; j++){
					if (e.stopFloors[j]){
						e.direction = 1;
						requested = true;
						break;
					}
				}
				if (requested){
					//add elevator move event to events queue
					events.sortedAdd(new ElevatorMoveEvent(time, elevatorMoveTime,e));
					return; 
					//TODO: replace this with 'break' in finished project
				}
			}
			//else, if the elevator is already moving:
			else {
				//check if there are more requested floors in the same direction
				for (int j = e.currentFloor+direction; j >= 0 && j < floors.length; j += direction){
					if (e.stopFloors[j]){
						requested = true; //continue in same direction
						break;
					}
				}
				if (requested){
					//add elevator move event to events queue
					events.sortedAdd(new ElevatorMoveEvent(time, elevatorMoveTime,e));
					return; 
					//TODO: replace this with 'break' in finished project
				}
				//if no requests found in same direction, check the opposite direction
				for (int j = e.currentFloor-direction; j >= 0 && j < floors.length; j -= direction){
					if (e.stopFloors[j]){
						//tell elevator to switch directions
						e.direction *= -1;
						requested = true;
						break;
					}
				}
				if (requested){
					//add elevator move event to events queue
					events.sortedAdd(new ElevatorMoveEvent(time, elevatorMoveTime,e));
					return;
					//TODO: replace this with 'break' in finished project
				}
			}
			
			//if no requested floors were found at all, send it to be idle in the lobby
			//(if it isn't already at the lobby)
			if (e.currentFloor != 0){
				e.direction = -1;
				e.stopAt(0);
				//add elevator move event to events queue
				events.sortedAdd(new ElevatorMoveEvent(time, elevatorMoveTime,e));
			}
			//if elevator is already at lobby, simply set it idle
			else{
				e.direction = 0;
			}
		}
	}
	
	//this method is called when a certain floor needs to be stopped at
	void signal(int floor){
		//in the final version of the project this method will smartly
		//choose which elevator to assign to this floor, but for now
		//there's just one elevator
		
		//tell the elevator to stop at this floor
		//TODO: since there's only one elevator right now this is fine,
		//but when there's multiple, only signal a SINGLE elevator:
		//the closest one going in the same direction
		Elevator e = elevators[0];
		e.stopAt(floor);
	}
	
	void assignElevator(int floor, int direction){
		//basically check which elevator is going in the same direction (or idle) as the request,
		//and is closest to the floor,
		//and once you have that elevator e,
		//simply call assignElevator(floor, e)
		//and that method will handle the rest.
		
		//Send the closest elevator going in the same direction.
		//If no such elevator exists, send the closest idle elevator
		//If two of any type exist, send the lowest ID elevator first.
		
		int numFloorsToGo = 0;
		int index = 0;
		
		//select the first eligible elevator as the basis of comparison and save its index
		for (int i = 0; i < elevators.length; i++) {
			
			//check if there is an elevator going up that has not passed the desired floor
			if (direction > 0 && elevators[i].direction == direction && elevators[i].currentFloor < floor) {
				index = i;
				numFloorsToGo = Math.abs(floor-elevators[i].currentFloor);
				break;
			}
			
			//check if there is an elevator going down that has not passed the desired floor
			if (direction < 0 && elevators[i].direction == direction && elevators[i].currentFloor > floor) {
				index = i;
				numFloorsToGo = Math.abs(floor - elevators[i].currentFloor);
				break;
			}
			
			//else find the first idle elevator
			if (elevators[i].direction == 0) {
				index = i;
				numFloorsToGo = Math.abs(floor - elevators[i].currentFloor);
				break;
			}
		}
		
		//now compare our elevator choice to all remaining and move the index if there is a
		//better elevator to send
		for (int i = index+1; i < elevators.length; i++){
			
			//if the elevator selected at index is going up 
			if (elevators[index].direction > 0 && elevators[i].direction > 0 && 
					elevators[i].currentFloor < floor && (floor-elevators[i].currentFloor)<numFloorsToGo){
				numFloorsToGo = floor-elevators[i].currentFloor;
				index = i;
			}
			
			//if the elevator selected at index is going down
			else if (elevators[index].direction < 0 && elevators[i].direction < 0 && 
					elevators[i].currentFloor > floor && (elevators[i].currentFloor-floor)<numFloorsToGo){
				numFloorsToGo = elevators[i].currentFloor-floor;
				index = i;
			}
			
			//if elevator at index is idle
			else if (elevators[index].direction == 0 && elevators[i].direction == 0 &&
					(Math.abs(floor-elevators[i].currentFloor) < numFloorsToGo)) {
				numFloorsToGo = Math.abs(elevators[i].currentFloor - floor);
				index = i;
			}
		}
		assignElevator(floor, elevators[index]);
	}

	void assignElevator(int floor, Elevator e){
		//this is an overloaded version of assign elevator
		//unlike the other version, which figures out which elevator to send
		//to a particular floor,
		//this one tells the given elevator to go to the given floor.
		//simply do e.stopAt(floor);
		//if the elevator is currently idle, you will need to create a new ElevatorMoveEvent
		//for it to start moving,
		//by calling moveElevator(e)
		
		if(e.direction != 0) e.stopAt(floor);
		else moveElevator(e);
	}
	
	void moveElevator(Elevator e){
		//create a new ElevatorMoveEvent
		//passing in the required arguments
		events.sortedAdd(new ElevatorMoveEvent(time, elevatorMoveTime, e));
	}
	
	public static void main(String[] args){
		//create EventDriver with 10 floors and 1 elevator
		EventDriver ed = new EventDriver(10, 1);
		//start simulation, go up to 1e3 arrivals
		ed.start();
	}
}
