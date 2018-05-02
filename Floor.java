/*
 * CS381 Modeling and Simulation
 * Elevator Simulation Final Project
 * Authors: Zaheen Ahmed 
 * 			Jun Young Cheong
 */
import java.util.LinkedList;

public class Floor {
	LinkedList<Person> queue; //elevator waiting line
	int floor; //which floor is this
	boolean up; //up button (true if pressed)
	boolean down; //down button (true if pressed)
	
	public Floor(int floor){
		this.floor = floor;
		queue = new LinkedList<Person>();
	}
	
	public void addPerson(Person p){
		queue.add(p);
		int direction = p.desiredFloor - floor;
		if (direction > 0)
			up = true;
		else
			down = true;
		//signal ed to tell an elevator to stop at this floor
		EventDriver.ed.signal(floor);
	}
}
