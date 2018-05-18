/*
 * CS381 Modeling and Simulation
 * Elevator Simulation Final Project
 * Authors: Zaheen Ahmed 
 * 			Jun Young Cheong
 */
import java.util.LinkedList;
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
		/*
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
		*/
		//generate first person
		nextPerson();
		while (!events.isEmpty()){
			nextEvent();
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
		double startTime = Math.round(next.triggerTime*100)/100.0;
		double endTime = Math.round((startTime + next.duration)*100)/100.0;
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
	
	void assignElevator(int floor, int direction){
		//basically check which elevator is going in the same direction (or idle) as the request,
		//and is closest to the floor,
		//and once you have that elevator e,
		//simply call assignElevator(floor, e)
		//and that method will handle the rest.
		
		//Send the closest elevator going in the same direction.
		//If no such elevator exists, send the closest idle elevator
		//If two of any type exist, send the lowest ID elevator first.
		
        //first check if there are any idle elevators already on the floor requested as that will be the
        //best elevator to send for minimum wait time. If it exists, send that
		for (int i = 0; i < elevators.length; i++){
            if (elevators[i].currentFloor == floor && elevators[i].direction == 0){
                assignElevator(floor, elevators[i]);
                return;
            }
		}
		
        int numFloorsToGo = 0;
        int index = 0;
		
		//select the first eligible elevator as the basis of comparison and save its index
		//first see if there are any elevators that are going in the same direction and have not yet, or are
		//at the floor
		for (int i = 0; i < elevators.length; i++) {
			
			//if up, check if there is an elevator going up that has not passed the desired floor
		    if (direction > 0 && elevators[i].direction == direction && elevators[i].currentFloor <= floor) {
				index = i;
				numFloorsToGo = floor - elevators[i].currentFloor;
				for (int j = index+1; j < elevators.length; j++){
		            if (elevators[index].direction > 0 && elevators[j].direction > 0 && 
		                    elevators[j].currentFloor <= floor && (floor-elevators[j].currentFloor)<numFloorsToGo){
		                numFloorsToGo = floor-elevators[j].currentFloor;
		                index = j;
		            }
				}
				assignElevator(floor, elevators[index]);
				return;
			}
			
			//if down, check if there is an elevator going down that has not passed the desired floor
			else if (direction < 0 && elevators[i].direction == direction && elevators[i].currentFloor >= floor) {
				index = i;
				numFloorsToGo = Math.abs(floor - elevators[i].currentFloor);
				for (int j = index+1; j < elevators.length; j++){
				    if (elevators[index].direction < 0 && elevators[j].direction < 0 && 
		                    elevators[j].currentFloor >= floor && (elevators[j].currentFloor-floor)<numFloorsToGo){
		                numFloorsToGo = elevators[j].currentFloor-floor;
		                index = j;
				    }
				}
				assignElevator(floor, elevators[index]);
				return;
			}
		}
		
		//if no such elevators exist, search through again but this time find the first idle elevator
		for (int i = 0; i < elevators.length; i++){
		    if (elevators[i].direction == 0) {
                index = i;
                numFloorsToGo = Math.abs(floor - elevators[i].currentFloor);
                for (int j = index+1; j < elevators.length; j++){
                    if (elevators[index].direction == 0 && elevators[j].direction == 0 &&
                            (Math.abs(floor-elevators[j].currentFloor) < numFloorsToGo)) {
                        numFloorsToGo = Math.abs(elevators[j].currentFloor - floor);
                        index = j;
                    }
                }
                assignElevator(floor, elevators[index]);
                return;
            }
		}
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
		
		//if this elevator is already at this floor, call exitAndBoard
		if (floor == e.currentFloor){
			exitAndBoard(e, floor);
		}
		else{
			//otherwise:
			//if elevator is already moving, simply flag it to stop at the requested floor
			e.stopAt(floor);
			//if elevator is idle, give it a kick
			if (e.direction == 0) {
				//give the elevator a direction
				e.direction = (floor - e.currentFloor > 0) ? 1 : -1;
				//1 if the desired floor is above, -1 if below
				//then move elevator
				moveElevator(e, time);
			}
		}
	}
	
	void moveElevator(Elevator e, double time){
		//create a new ElevatorMoveEvent
		//passing in the required arguments
		events.sortedAdd(new ElevatorMoveEvent(time, elevatorMoveTime, e));
	}
	
	void exitAndBoard(Elevator e, int floor){
		//keep a temporary variable to simulate time in exiting and boarding
		//the elevator, since all people can't exit and board at once
		double tempTime = time; 
		
		//first handle any person exiting the elevator
		for (Person p : e.occupants){
			if (floor == p.desiredFloor){
				events.sortedAdd(new ElevatorExitEvent(tempTime, elevatorExitTime, p, e));
				tempTime += elevatorExitTime; //update tempTime
			}
		}
		//now handle all who want to board
		//choose either the floor's upQueue or downQueue based on e's direction
		LinkedList<Person> queue = (e.direction > 0) ? 
					floors[floor].upQueue : 
					floors[floor].downQueue;
					
		//System.out.println("# of people in upQueue: " + floors[floor].upQueue.size());
		//System.out.println("# of people in downQueue: " + floors[floor].downQueue.size());
		//board all people in queue
		for (Person p : queue){
			events.sortedAdd(new ElevatorBoardEvent(tempTime, elevatorBoardTime, p, e, e.direction));
			tempTime += elevatorBoardTime; //update tempTime
		}
		//unassign this stop for this elevator
		e.stopFloors[floor] = false;
		//reset floor's up or down button
		if (e.direction > 0)
			floors[floor].up = false;
		else
			floors[floor].down = false;
		//if elevator has more stops in the direction it's going, keep it moving
		if (e.hasMoreStops()){
			//keep moving in current direction
			moveElevator(e, tempTime);
		}
		else{
			//otherwise, elevator has no more assigned stops, become idle
			e.direction = 0;
		}
	}
	
	public static void main(String[] args){
		//create EventDriver with 10 floors and 1 elevator
		EventDriver ed = new EventDriver(10, 10);
		//start simulation, go up to 1e3 arrivals
		ed.start();
	}
}
