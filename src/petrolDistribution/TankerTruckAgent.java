package petrolDistribution;

import java.util.LinkedList;
import java.util.Queue;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class TankerTruckAgent {

    private ContinuousSpace<Object> space;
    private Grid<Object> grid;
    private int id;
    private int status; // 0: Empty, 1: Full Tank and assigned, 2: Full Tank and not assigned
    private double capacity;
    private double petrolLevel;
    private Queue<PetrolStationAgent> requestQueue;
 

    public TankerTruckAgent(ContinuousSpace<Object> space, Grid<Object> grid, int id, double capacity) {
        this.space = space;
        this.grid = grid;
        this.id = id;
        this.status = 0; // Initially empty
        this.capacity = capacity;
        this.petrolLevel = 0; // Initially empty
        this.requestQueue = new LinkedList<PetrolStationAgent>();
    }
    
    @ScheduledMethod(start = 1, interval = 1)
    public void step() {
    	
        GridPoint pt = grid.getLocation(this);
        if (status == 0) { // If empty, move towards petrol tank refiner and refill
            if (space.getLocation(this).getX() <= 5 && space.getLocation(this).getY() <= 5) {
            	System.out.println("Refilling tank...");
                refillTank();
            } else {
                // Move towards petrol tank refiner
            	System.out.println("Moving towards petrol tank refiner...");
                double dx = 5 - space.getLocation(this).getX();
                double dy = 5 - space.getLocation(this).getY();
                double distance = Math.sqrt(dx*dx + dy*dy);
                double speed = 0.1; // Adjust the speed as necessary
                double[] displacement = { dx, dy }; // create a displacement vector with two dimensions
                space.moveByVector(this, speed, displacement); // use the updated moveByVector method
                if (distance <= 1) {
                    System.out.println("Refilling tank...");
                    refillTank();
                }

            }
        } else if (status == 2) { // If not assigned and there are pending requests, move towards the first request in queue
            if (!requestQueue.isEmpty()) {
                PetrolStationAgent petrolStation = requestQueue.peek();
                double requestedQuantity = petrolStation.getDailyDemand();
                if (requestedQuantity <= capacity - petrolLevel) { // If enough capacity, move towards the petrol station and deliver petrol
                    // Move towards the petrol station
                	System.out.println("Moving towards petrol station...");
                    double dx = petrolStation.getLocation().getX() - space.getLocation(this).getX();
                    double dy = petrolStation.getLocation().getY() - space.getLocation(this).getY();
                    double distance = Math.sqrt(dx*dx + dy*dy);
                    double speed = 0.1; // Adjust the speed as necessary
                    double[] displacement = { dx, dy }; // create a displacement vector with two dimensions
                    space.moveByVector(this, speed, displacement); // use the updated moveByVector method
                    // Check if the truck has reached the petrol station
                    if (distance <= 1) {
                    	System.out.println("Delivering petrol to petrol station...");
                        deliverPetrol(petrolStation, requestedQuantity);
                        // Wait for 30 seconds
                        try {
                            Thread.sleep(30000); 
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        status = 0; // Change the status of the petrol station to empty
                        requestQueue.poll(); // Remove the first request from queue
                    }
                } else {
                	System.out.println("Rejecting request and moving towards next request...");
                    // If not enough capacity, refuse the request and send it to the high-capacity tanker truck
                    requestQueue.poll(); // Remove the first request from queue
                    moveTowardsNextRequest(); // Move towards the next request in queue
                }
            } else { // If full and assigned, move towards the assigned petrol station and deliver petrol
                PetrolStationAgent assignedStation = (PetrolStationAgent) grid.getObjectAt(pt.getX(), pt.getY());
                double requestedQuantity = assignedStation.getDailyDemand();
                if (requestedQuantity <= petrolLevel) { // If enough petrol, move towards the assigned petrol station and deliver petrol
                	System.out.println("Moving towards assigned petrol station...");
                    double dx = assignedStation.getLocation().getX() - space.getLocation(this).getX();
                    double dy = assignedStation.getLocation().getY() - space.getLocation(this).getY();
                    double distance = Math.sqrt(dx*dx + dy*dy);
                    double speed = 0.1; // Adjust the speed as necessary
                    double[] displacement = { dx, dy }; // create a displacement vector with two dimensions
                    space.moveByVector(this, speed, displacement); // use the updated moveByVector method
                    // Check if the truck has reached the assigned petrol station
                    if (distance <= 1) {
                    	System.out.println("Delivering petrol to assigned petrol station...");
                        deliverPetrol(assignedStation, requestedQuantity);
                        try {// Wait for 30 seconds
                            Thread.sleep(30000); 
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            status = 0; // Change the status of the petrol station to empty
                        } else { // If not enough petrol, move towards the petrol tank refiner to refill
                        	System.out.println("Not enough petrol. Moving towards petrol tank refiner to refill...");
                        	moveTowardsPetrolTankRefiner();
                        	refillTank();
                        }
                    } else { // If not enough petrol, move towards the petrol tank refiner to refill
                    	System.out.println("Not enough petrol. Moving towards petrol tank refiner to refill...");
                    	moveTowardsPetrolTankRefiner();
                    	refillTank();
                    }
                }
            }
        }/*else {
        	// handle the case where pt is null
        }     
    }*/

    
    
    
    public void receivePetrolRequest(PetrolStationAgent petrolStation, double requestedQuantity) {
        if (status == 0) { // If empty, refuse the request
            petrolStation.receivePetrolDelivery(0, false);
        } else if (status == 1) { // If assigned, refuse the request
            petrolStation.receivePetrolDelivery(0, false);
        } else if (status == 2) { // If not assigned, add the request to the queue
            requestQueue.add(petrolStation);
        }
    }
    
    
    /*public void respondToRequest(PetrolStationAgent station, String response) {
    	if (response.equals("YES")) {
    	// Tanker truck accepts the request and delivers the petrol
    	int amount = station.getRequestedAmount();
    	int time = (int)(amount / TANKER_TRUCK_FLOW_RATE);
    	station.receivePetrolDelivery(amount, time);
    	this.setStatus(1); // Set tanker truck status to "assigned"
    	} else {
    	// Tanker truck declines the request
    	this.setWaiting(true); // Set tanker truck waiting status to true
    	}
    	}*/

    public void addRequest(PetrolStationAgent petrolStation) {
        requestQueue.add(petrolStation);
    }

    private void refillTank() {
        // Simulate refilling the tank at a petrol tank refiner
        petrolLevel = capacity;
        status = 2; // After refilling, the truck is not assigned and available for new requests
        moveTowardsNextRequest();
    }
    

    
    private void deliverPetrol(PetrolStationAgent petrolStation, double quantity) {
        status = 1; // Change status to assigned
        petrolLevel -= quantity;
        petrolStation.receivePetrolDelivery(quantity, true);
    }

    private void moveTowardsPetrolTankRefiner() {
        // Move towards petrol tank refiner
        double dx = 10 - space.getLocation(this).getX();
        double dy = 10 - space.getLocation(this).getY();
        double distance = Math.sqrt(dx*dx + dy*dy);
        double speed = 0.1; // Adjust the speed as necessary
        double[] displacement = { dx, dy }; // create a displacement vector with two dimensions
        space.moveByVector(this, speed, displacement); // use the updated moveByVector method
    }

    private void moveTowardsNextRequest() {
        // Move towards the first request in queue
        if (!requestQueue.isEmpty()) {
            PetrolStationAgent petrolStation = requestQueue.peek();
            double dx = petrolStation.getLocation().getX() - space.getLocation(this).getX();
            double dy = petrolStation.getLocation().getY() - space.getLocation(this).getY();
            double distance = Math.sqrt(dx*dx + dy*dy);
            double speed = 0.1; // Adjust the speed as necessary
            double[] displacement = { dx, dy }; // create a displacement vector with two dimensions
            space.moveByVector(this, speed, displacement); // use the updated moveByVector method
        }
    }
    
    public int getId() {
        return id;
    }

    public int getStatus() {
        return status;
    }

    public boolean isWaiting() {
        return !requestQueue.isEmpty();
    }
}