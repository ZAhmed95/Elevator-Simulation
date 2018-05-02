/*
 * CS381 Modeling and Simulation
 * Elevator Simulation Final Project
 * Authors: Zaheen Ahmed 
 * 			Jun Young Cheong
 */
//Event is an abstract class, its subclasses will be instantiated for the simulation
public abstract class Event implements Comparable<Event>{
	String message; //description of this event
	double triggerTime; //when this event will trigger
	double duration; //how long this event lasts
	
	public Event(String message, double triggerTime, double duration){
		this.message = message;
		this.triggerTime = triggerTime;
		this.duration = duration;
	}
	
	public String getMessage(){
		return message;
	}

	@Override
	public int compareTo(Event other) {
		return (int)(this.triggerTime - other.triggerTime);
	}
	
	//this is the main method that is used to execute an event
	public abstract void execute();
}
