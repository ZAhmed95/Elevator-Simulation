/*
 * CS381 Modeling and Simulation
 * Elevator Simulation Final Project
 * Authors: Zaheen Ahmed 
 * 			Jun Young Cheong
 */
public class Person {
	int id;
	int arrivalFloor; //which floor this person arrived at (needed for statistics)
	int desiredFloor; //which floor this person wants to go to
	double arrivalTime; //when did this person arrive
	boolean boarded; //will be set to true when a boardEvent is created for this person
	
	public Person(int arrivalFloor, int desiredFloor){
		id = Statistics.personCount;
		Statistics.personCount++;
		this.arrivalFloor = arrivalFloor;
		this.desiredFloor = desiredFloor;
		boarded = false;
	}
}
