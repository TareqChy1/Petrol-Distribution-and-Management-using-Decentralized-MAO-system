package petrolDistribution;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;

public class PetrolContextBuilder implements ContextBuilder<Object> {

    @Override
    public Context<Object> build(Context<Object> context) {
        context.setId("petrolDistribution");

        ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
        ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context,
                new RandomCartesianAdder<Object>(),
                new repast.simphony.space.continuous.WrapAroundBorders(),
                50, 50);

        GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
        Grid<Object> grid = gridFactory.createGrid("grid", context,
                new GridBuilderParameters<Object>(new WrapAroundBorders(),
                        new SimpleGridAdder<Object>(),
                        true, 50, 50));

        // Set the number of Tanker Trucks and Petrol Stations
        int numTankerTrucks = 1;
        int numPetrolStations = 10;
        int numRefiners = 1;
        
        Parameters params = RunEnvironment.getInstance().getParameters();

        // Create Tanker Truck agents
        //int TankerTrucksCount = params.getInteger("TankerTrucks_count");
        for (int i = 0; i < numTankerTrucks; i++) {
            TankerTruckAgent tankerTruck = new TankerTruckAgent(space, grid, i, 250000);
            context.add(tankerTruck);
        }

        // Create Petrol Station agents
        //int PetrolStationsCount = params.getInteger("PetrolStations_count");
        for (int i = 0; i < numPetrolStations; i++) {
            PetrolStationAgent petrolStation = new PetrolStationAgent(space, grid, i);
            context.add(petrolStation);
        }
        
     // Create Refiner Objects
        //int RefinersCount = params.getInteger("Refiners_count");
        for (int i = 0; i < numRefiners; i++) {
            Refiner refiner = new Refiner(space, grid);
            context.add(refiner);
        }

        // Create a network to represent communication between Tanker Trucks and Petrol Stations
        NetworkBuilder<Object> networkBuilder = new NetworkBuilder<Object>("Petrol Distribution Network", context, true);
        Network<Object> network = networkBuilder.buildNetwork();

        // Connect Petrol Stations to Tanker Trucks in the network
        for (Object petrolStation : context.getObjects(PetrolStationAgent.class)) {
            for (Object tankerTruck : context.getObjects(TankerTruckAgent.class)) {
                network.addEdge(petrolStation, tankerTruck);
            }
        }

        // Initialize random seed for demand generation
        RandomHelper.setSeed((int) System.currentTimeMillis());

        return context;
    }
}
