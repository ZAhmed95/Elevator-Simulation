import java.util.Random;

//this class records the statistics of the elevator simulation,
public class Statistics {
	static int personCount; //how many people have arrived
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
}
