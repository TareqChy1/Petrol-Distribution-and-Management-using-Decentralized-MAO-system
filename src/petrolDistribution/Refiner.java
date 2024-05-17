package petrolDistribution;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
public class Refiner {


private ContinuousSpace<Object> space;
private Grid<Object> grid;
		
public Refiner(ContinuousSpace<Object> space, Grid<Object> grid) {
			this.space = space;
			this.grid = grid;
		}
}
