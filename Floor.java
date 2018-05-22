/*
 * CS381 Modeling and Simulation
 * Elevator Simulation Final Project
 * Authors: Zaheen Ahmed 
 * 			Jun Young Cheong
 */
import java.util.LinkedList;

public class Floor {
	LinkedList<Person> upQueue; //waiting line for people going up
	LinkedList<Person> downQueue; //waiting line for people going down
	int floor; //which floor is this
	boolean up; //up button (true if pressed)
	boolean down; //down button (true if pressed)
	
	public Floor(int floor){
		this.floor = floor;
		upQueue = new LinkedList<Person>();
		downQueue = new LinkedList<Person>();
	}
	
	public void addPerson(Person p){
	    int direction = (p.desiredFloor - floor) > 0 ? 1 : -1;
		if (direction > 0){
			upQueue.add(p);
			up = true;
		}
		else{
			downQueue.add(p);
			down = true;
		}
		//signal ed to tell an elevator to stop at this floor
		EventDriver.ed.assignElevator(floor, direction);
	}
}