/*
 * CS381 Modeling and Simulation
 * Elevator Simulation Final Project
 * Authors: Zaheen Ahmed 
 * 			Jun Young Cheong
 */
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

//TODO: implement data collection
//this class records the statistics of the elevator simulation
public class Statistics {
	static int personCount; //how many people have arrived
	double totalWaitTime; //cumulative wait time of people
	//wait time is the time it takes from when a person arrives at a floor,
	//until when they get off at their destination
	int numFloors;
	
	Random rand; //random instance to use for rng
	
	//this 2D array will hold statistics of trips from floor i to floor j
	//e.g. stats[i][j] will tell you avg wait time to get from floor i to j
	FloorToFloorStats[][] stats;
	
	PrintWriter statOutput; //output file to write stats to
	
	public Statistics(){
		rand = new Random();
		numFloors = EventDriver.ed.numFloors;
		//initialize stats
		stats = new FloorToFloorStats[numFloors][numFloors];
		for (int i = 0; i < numFloors; i++){
			for (int j = 0; j < numFloors; j++){
				stats[i][j] = new FloorToFloorStats();
			}
		}
	}
	
	public void updateStats(int i, int j, double x){
		//i is start floor, j is end floor, x is wait time
		FloorToFloorStats stat = stats[i][j];
		int n = ++stat.n; //new # of trips
		double avg = stat.average; //old average
		double M2 = stat.M2; //old difference of squares
		//calculate new average
		stat.average = avg + (x - avg) / n;
		//calculate new M2 ( = old M2 + (x - old average) * (x - new average) )
		stat.M2 = M2 + (x - avg) * (x - stat.average);
		//variance and s.d. can be calculated at the end
	}
	
	public void finalizeStats(){
		//finalize stats
		for (int i = 0; i < numFloors; i++){
			for (int j = 0; j < numFloors; j++){
				finalizeStats(i, j);
			}
		}
	}
	
	void finalizeStats(int i, int j){
		//simulation has completed, finalize the stats by computing variance and s.d.
		FloorToFloorStats stat = stats[i][j];
		stat.variance = (stat.n > 0) ? stat.M2 / stat.n : 0; //calculate variance
		stat.stdDeviation = Math.sqrt(stat.variance); //calculate s.d.
	}
	
	public void outputStats(){
		//open output file
		try {
			statOutput = new PrintWriter("statistics.txt");
		} catch (FileNotFoundException e) {
			System.out.println("Could not open statistics output file");
			System.exit(1);
		}
		statOutput.write("Statistics for Elevator Simulation:\n");
		statOutput.write("Total number of customers served: " + personCount + "\n");
		
		//necessary variables for upcoming loops
		FloorToFloorStats stat;
		double value;
		//outputting total number of trips from floor i to floor j
		statOutput.write("Total number of trips from floor i to floor j:\n\n");
		outputTable("n", "%-10d");
		
		//outputting average wait times from floor i to floor j
		statOutput.write("Average wait time of a person going from floor i to floor j:\n\n");
		outputTable("average", "%-10.2f");
		
		//outputting standard deviation of wait times from floor i to floor j
		statOutput.write("Standard deviation of wait times going from floor i to floor j:\n\n");
		outputTable("stdDeviation", "%-10.2f");
		//flush and close output file
		statOutput.flush();
		statOutput.close();
	}
	
	void outputTable(String field, String format){
		FloorToFloorStats stat;
		statOutput.write("Floor\t");
		for (int j = 0; j < numFloors; j++)
			statOutput.write(String.format("%-10d", j));
		statOutput.write("\n\n");
		
		for (int i = 0; i < numFloors; i++){
			statOutput.write(i + "\t\t");
			for (int j = 0; j < numFloors; j++){
				stat = stats[i][j];
				try {
					statOutput.write(String.format(format, FloorToFloorStats.class.getDeclaredField(field).get(stat)));
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			statOutput.write("\n\n");
		}
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
