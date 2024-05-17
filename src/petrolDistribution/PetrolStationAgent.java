package petrolDistribution;

import java.util.Random;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class PetrolStationAgent {

private ContinuousSpace<Object> space;
private Grid<Object> grid;
private int id;
private double dailyDemand;
private double inventoryLevel;
private int currentTankerTruckId;
private boolean waitingForResponse;
private int numPetrolStations = 10; // The number of petrol stations that the agent manages

public PetrolStationAgent(ContinuousSpace<Object> space, Grid<Object> grid, int id) {
this.space= space;
this.grid=grid;
this.id = id;
this.dailyDemand = 0;
this.inventoryLevel = 1000; // Initial inventory level
this.currentTankerTruckId = 1;
this.waitingForResponse = false;
}

@ScheduledMethod(start = 1, interval = 1)
public void step() {
	GridPoint pt = grid.getLocation(this);
generateDailyDemand();
if (inventoryLevel < forecast() && !waitingForResponse) {
requestPetrolDelivery();
Random random = new Random();
boolean randomBoolean = random.nextBoolean();
receivePetrolDelivery(RandomHelper.nextDoubleFromTo(300, 350), randomBoolean);
}
}



private void generateDailyDemand() {
    int dayOfWeek = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount() % 7;
    if (dayOfWeek >= 0 && dayOfWeek < 5) {
        dailyDemand = RandomHelper.nextDoubleFromTo(100, 300);
    } else {
        dailyDemand = RandomHelper.nextDoubleFromTo(400, 600);
    }
    // Ensure inventory level does not go below 0
    inventoryLevel = Math.max(inventoryLevel - dailyDemand, 0);
}


private double forecast() {
double mean = inventoryLevel;
double stdDev = 50;
double forecast = RandomHelper.createNormal(mean, stdDev).nextDouble();
// ensure forecast is at least equal to dailyDemand
if (forecast < dailyDemand) {
forecast = dailyDemand;
}
return forecast;
}

private void requestPetrolDelivery() {
    // Send a request to the current tanker truck agent for petrol delivery
    int tankerTruckId = currentTankerTruckId;
    while (true) {
        TankerTruckAgent tankerTruck = (TankerTruckAgent)grid.getObjectAt(tankerTruckId);
        if (tankerTruck == null) {
            // The tankerTruck object is null, so break out of the loop
            break;
        }
        // Check if tanker truck is available and has capacity to fulfill request
        if (tankerTruck.getStatus() == 0 && !tankerTruck.isWaiting()) {
            waitingForResponse = true;
            tankerTruck.receivePetrolRequest(this, forecast());
            break;
        }
        // If the tanker truck is assigned to another station, check the next one
        else if (tankerTruck.getStatus() == 1 && !tankerTruck.isWaiting()) {
            currentTankerTruckId++;
            tankerTruckId = currentTankerTruckId;
        }
        // If the tanker truck is available but its tank is full, add the request to the queue
        else if (tankerTruck.getStatus() == 2 && !tankerTruck.isWaiting()) {
            waitingForResponse = true;
            tankerTruck.receivePetrolRequest(this, forecast());
            currentTankerTruckId++;
            break;
        }
        // If none of the above conditions are met, check the next tanker truck
        else {
            currentTankerTruckId++;
            tankerTruckId = currentTankerTruckId;
        }
    }
}


public void receivePetrolDelivery(double quantity, boolean accepted) {
    if (accepted) {
        inventoryLevel += quantity;
        System.out.println("Received petrol delivery: quantity = " + quantity + ", accepted = " + accepted);
        waitingForResponse = false;
    } else {
        // If the request was not accepted, send a request to the next tanker truck ID
        currentTankerTruckId++;
        requestPetrolDelivery();
    }
    // Update inventory level
    System.out.println("Current inventory level: " + inventoryLevel);
}



public int getID() {
    return id;
}

public double getDailyDemand() {
    return dailyDemand;
}

public double getInventoryLevel() {
    return inventoryLevel;
}

public GridPoint getLocation() {
    return grid.getLocation(this);
    }



}
