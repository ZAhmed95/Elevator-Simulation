/*
 * CS381 Modeling and Simulation
 * Elevator Simulation Final Project
 * Authors: Zaheen Ahmed 
 * 			Jun Young Cheong
 */
import java.util.Random;

//TODO: implement data collection
//this class records the statistics of the elevator simulation
public class Statistics {
	static int personCount; //how many people have arrived
	double totalWaitTime; //cumulative wait time of people
	//wait time is the time it takes from when a person arrives at a floor,
	//until when they get off at their destination
	Random rand;
	
	public Statistics(){
		rand = new Random();
	}
	//utility function to generate exponential random variable
	public double expRandom(double mean){
		return -Math.log(1.0 - rand.nextDouble())*mean;
	}
	
	//generate discrete random number from a (inclusive) to b (exclusive)
	public int randInt(int a, int b){
		return rand.nextInt(b-a)+a;
	}
	
	//calculate average wait time
	public double averageWaitTime(){
		return totalWaitTime / personCount;
	}
}
