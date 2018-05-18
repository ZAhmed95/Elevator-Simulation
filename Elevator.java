/*
 * CS381 Modeling and Simulation
 * Elevator Simulation Final Project
 * Authors: Zaheen Ahmed 
 * 			Jun Young Cheong
 */
import java.util.LinkedList;

public class Elevator {
	int id; //this elevator's id
	int currentFloor; //which floor this elevator is currently on
	int direction; //the direction this elevator is moving, 1 for up, -1 for down, 0 for idle
	int capacity; //maximum # of people it can hold
	LinkedList<Person> occupants; //current occupants
	boolean[] stopFloors; //array holding which floors this elevator will stop at
	
	public Elevator(int id, int startFloor, int cap){
		this.id = id;
		currentFloor = startFloor;
		capacity = cap;
		occupants = new LinkedList<Person>();
		stopFloors = new boolean[EventDriver.ed.numFloors];
		this.direction = 0;
	}
	
	//tell this elevator to stop at a certain floor
	public void stopAt(int floor){
		stopFloors[floor] = true;
	}
	
	//elevator will move one floor in the current direction
	public void move(){
		currentFloor += direction;
	}
	
	//method to board a person onto the elevator
	public void board(Person p){
		occupants.add(p);
		stopFloors[p.desiredFloor] = true;
	}
	
	//method to check if this elevator still has more stops to get to
	public boolean hasMoreStops(){
		if (direction == 0)
			return false;
		
		boolean moreStops = false;
		for (int i = currentFloor + direction; 
				i >= 0 && i < EventDriver.ed.numFloors; 
				i += direction
			){
			if (stopFloors[i]){
				//found a remaining stop
				moreStops = true;
				break;
			}
		}
		return moreStops;
	}
}
