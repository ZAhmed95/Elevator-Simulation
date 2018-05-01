
public class Person {
	int id;
	int desiredFloor; //which floor this person wants to go to
	
	public Person(int floor){
		id = Statistics.personCount;
		Statistics.personCount++;
		desiredFloor = floor;
	}
}
