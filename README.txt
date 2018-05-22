CSCI 381 - Elevator Simulation
Spring 2018
By Zaheen Ahmed, Jun Young Cheong

This is an discrete-event elevator simulation where people arrive and are serviced 
by any number of elevators for any number of floors. The arrivals are handled randomly
with an exponential distribution and can happen at any of the floors. 

At the heart of the program is the linked list of all future events. All types 
of events (elevator movement, people exiting, boarding, and arriving) are all subclasses of the 
Event class and are ordered by their triggerTimes (the time in which they are set to occur).

Note that when run, the program will write onto a .txt file in the program folder. Please open
this file with an text editor such as Notepad++.

Classes:

    ArrivalEvent 
        - Calls methods to add the person to the appropriate floor and generate the next person upon 
          execution.
    Elevator
        - Each elevator object keeps track of its current location, the direction that is it moving in, 
          the stops it needs to make along the way, and its occupants.
        - Each elevator contains a linked list of its occupants.
    ElevatorBoardEvent
        - On execution, the event removes a person from one of the floor's two lists and boards them 
          onto the appropriate elevator.
    ElevatorExitEvent
        - Removes the person from the elevator when executed.
    ElevatorMoveEvent
        - Tells the elevator to move up or down a floor and if there are people who need to board or
          exit on that floor, call the methods necessary.
    Event
        - Superclass for all events.
    EventDriver
        - Driver class for the project. 
        - It is here that all events are executed, the elevators are assigned by the master controller,
          and the events are generated.
        - The floors and elevators objects are stored in arrays in this class.
    Floor
        - Each floor contain two linked lists which store the people that wish to go up and down. 
    FloorToFloorStats
        - This class holds statistics such as average wait time to get from floor i to floor j
    Person
        - People are generated randomly with an exponential distribution.
        - Can arrive and desire to go to any random floor. The desired floor is only known to the elevator
          upon the person's boarding.
    SortedLinkedList
        - Created to assist in the main events list.
    Statistics
        - Contains all the statistical data needed for output.
    