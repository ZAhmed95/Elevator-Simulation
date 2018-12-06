/*
 * CS381 Modeling and Simulation
 * Elevator Simulation Final Project
 * Authors: Zaheen Ahmed 
 * 			Jun Young Cheong
 */
import java.util.LinkedList;

public class EventDriver {
	static EventDriver ed; //a static reference to the latest instance of EventDriver,
	//to help other classes have easy access to it
	static int maxArrivals = 100; // change this to determine how long simulation will run for
	Floor[] floors; //array of all floors in building
	int numFloors; //total number of floors
	Elevator[] elevators; //array of all elevators in building
	int numElevators; //total number of elevators
	double time; //current simulation time, in seconds
	SortedLinkedList<Event> events; //linked list of all future events
	Statistics stat;
	boolean debug = true; //if true, program will print debugging output to console

	
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
		//generate first person
		nextPerson();
		//simulation will keep running as long as events keep being generated
		//events linked list is sorted based on its triggerTime
		while (!events.isEmpty()){
			nextEvent();
		}
		//finalize statistics
		stat.finalizeStats();
		//output statistics
		stat.outputStats();
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
		Person p = new Person(startFloor, targetFloor);
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
		//50% chance of an equilikely choice between floor 1 to floor (numFloors-1)
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
		if (debug){
			//log event message
			System.out.println(
					"(time = " + startTime + ") " //event start time
					+ next.getMessage() //event message
					+ " (end = " + endTime + ")" //event end time
				);
		}
		
		//update time if time < endTime
		//this check is necessary for bookkeeping purposes, since it's possible
		//for one event to happen while another is progressing
		//e.g. a new person arrives while an elevator is moving
		//so we need to log the correct times
		if (time < endTime)
			time = endTime;
		//execute event
		next.execute();
	}
	
	void assignElevator(int floor, int direction){
	    //Master controller for the elevators. Decides with elevator is the best
	    //to send based on the desired floor and the direction the person will be going.
	    
	    //Note: Direction can be up (1), idle (0), or down (-1).
		
		//Send the closest elevator going in the same direction.
		//If no such elevator exists, send the closest idle elevator
		//If two of any type exist, send the lowest ID elevator first.
		
		int index = -1; //index of most suitable elevator
		int minDistance = -1; //distance that the most suitable elevator must travel
		int distance; //distance that a given elevator must travel
		Elevator e; //will hold each elevator in the loop
		Elevator bestElevator = null; //will hold the most suitable elevator to assign
		
		//FIRST PRIORITY: closest elevator going in same direction
		//loop through all elevators and look for the closest one going in the same direction
		for (int i = 0; i < elevators.length; i++) {
			e = elevators[i];
			//compute how much distance this elevator must travel to get to floor
			//we multiply by direction to ensure that distance is positive for a suitable
			//elevator, i.e. if direction is 1, the elevator is BELOW the desired floor
			distance = (floor - e.currentFloor) * direction;
			if (e.direction == direction && distance > 0){
				//if either:
				//a) no suitable elevator has been found yet (index = -1), OR
				//b) this elevator is a more suitable candidate,
				//assign this elevator instead of the previous one
				if (index == -1 || distance < minDistance){
					index = i;
					minDistance = distance;
					bestElevator = elevators[index];
				}
			}
		}
		
		//SECOND PRIORITY: closest idle elevator
		//if above loop didn't find any suitable elevators going in the same direction,
		//(i.e. index is still -1),
		//try and look for the closest IDLE elevator
		if (index == -1){
			for (int i = 0; i < elevators.length; i++){
				e = elevators[i];
				//if this elevator is idle:
				if (e.direction == 0){
					distance = Math.abs(floor - e.currentFloor);
					//if a suitable elevator has not yet been found, OR
					//this elevator is closer than the previous closest,
					//assign this one
					if (index == -1 || distance < minDistance){
						index = i;
						minDistance = distance;
						bestElevator = elevators[index];
					}
				}
			}
		}
		
		//LAST PRIORITY: default elevator (e0)
		//if we STILL haven't found a suitable elevator, simply assign elevator 0
		if (index == -1){
			index = 0;
			bestElevator = elevators[index];
		}
		
		//assign the best elevator for the job
		assignElevator(floor, bestElevator);
	}

	void assignElevator(int floor, Elevator e){
		//this is an overloaded version of assign elevator
		//unlike the other version, which figures out which elevator to send
		//to a particular floor,
		//this one tells the given elevator to go to the given floor.
		
		//if this elevator is idle AND is already at this floor, call exitAndBoard
		if (e.direction == 0 && floor == e.currentFloor){
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
		
		//unassign this stop for this elevator
		e.stopFloors[floor] = false;
		
		//first handle any person exiting the elevator
		for (Person p : e.occupants){
			if (floor == p.desiredFloor){
				events.sortedAdd(new ElevatorExitEvent(tempTime, elevatorExitTime, p, e));
				tempTime += elevatorExitTime; //update tempTime
			}
		}
		//now handle all who want to board
		LinkedList<Person> queue;
		//if e is idle and this method was called, that means
		//someone arrived at this floor just now
		//see which queue the person is, and choose that direction to go
		if (e.direction == 0){
			//if someone's waiting to go up, set direction to 1
			if (floors[floor].upQueue.size() > 0)
				e.direction = 1;
			else
				//otherwise, we have someone wanting to go down
				e.direction = -1;
		}
		//choose either the floor's upQueue or downQueue based on e's direction
		queue = (e.direction > 0) ? floors[floor].upQueue : floors[floor].downQueue;
		
		//if there's no one wanting to go in the same direction as e, check
		//if e has any more stops above this floor. If not, pick up the people
		//wanting to go the other way.
		if (queue.isEmpty() && !e.hasMoreStops()){
			//pick the people who wants to go in the OPPOSITE direction
			queue = (e.direction > 0) ? floors[floor].downQueue : floors[floor].upQueue;
			//set e's direction to be opposite of its original direction
			e.direction *= -1;
			//if there's no one in this queue either, AND
			//there's no more stops waiting for this elevator in the
			//opposite direction, set the elevator idle.
			if (queue.isEmpty() && !e.hasMoreStops()){
				e.direction = 0;
				return;
			}
		}
		//above logic has determined which queue to pick people up from,
		//and which direction elevator will go after done picking people up.
		//now, all that's left to do is actually board the people
		
		//board all people in queue
		for (Person p : queue){
			//skip anyone who's already scheduled to board.
			//this is to avoid a race condition where 2 elevators on the same floor
			//try to board the same person
			if (p.boarded)
				continue;
			events.sortedAdd(new ElevatorBoardEvent(tempTime, elevatorBoardTime, p, e, e.direction));
			p.boarded = true;
			tempTime += elevatorBoardTime; //update tempTime
		}
		
		//reset floor's up or down button
		if (e.direction > 0)
			floors[floor].up = false;
		else
			floors[floor].down = false;
		
		//tell it to start moving
		moveElevator(e, tempTime);
	}
	
	public static void main(String[] args){
		//create EventDriver with 10 floors and 10 elevator
		EventDriver ed = new EventDriver(10, 10);
		//start simulation, go up to 1e3 arrivals
		ed.start();
	}
}
