/*
 * CS381 Modeling and Simulation
 * Elevator Simulation Final Project
 * Authors: Zaheen Ahmed 
 * 			Jun Young Cheong
 */
public class FloorToFloorStats {
	//this class holds statistics such as average wait time to get from floor i to floor j
	int n; //total number of trips that have happened from i to j
	double average; //the average wait time
	double M2; //sum of squared differences between each wait time and the average
	double variance; //variance of average wait times
	double stdDeviation; //standard deviation of average wait times
	
	FloorToFloorStats(){
		n = 0;
		average = 0;
		M2 = 0;
		variance = 0;
		stdDeviation = 0;
	}
}
