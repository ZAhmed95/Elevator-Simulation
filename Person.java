/*
 * CS381 Modeling and Simulation
 * Elevator Simulation Final Project
 * Authors: Zaheen Ahmed 
 * 			Jun Young Cheong
 */
public class Person {
	int id;
	int desiredFloor; //which floor this person wants to go to
	double arrivalTime; //when did this person arrive
	
	public Person(int floor){
		id = Statistics.personCount;
		Statistics.personCount++;
		desiredFloor = floor;
	}
}
