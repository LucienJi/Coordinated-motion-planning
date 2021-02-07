import java.util.ArrayList;

/**
 * An algorithm that computes a solution of the motion planning problem. <br>
 * 
 * @author Luca Castelli Aleardi (INF421, Ecole Polytechnique, dec 2020)
 *
 */
public class MyBestAlgorithm extends MotionAlgorithm {
	/** An input instance of the motion planning problem */
	public Instance input;
	
	/** The solution computed by the algorithm */
	public Solution solution;
	public Solution solution2;
	/** Current locations of robots */
	Coordinates current;
	
	public MyBestAlgorithm(Instance input) {
		this.input=input;
		this.solution=new Solution(input.name); // create an empty solution (no steps at the beginning)
		this.current=new Coordinates(this.input.starts.getPositions()); // initialize the current locations with the starting input locations
	}
	
	/**
	 * Return the current solution: it assumes that the solution has been computed
	 */
	public Solution getSolution() {
		return this.solution;
	}
	
	
	/**
	 * Compute a complete solution to the input problem: compute all steps, until all robots reach their target destinations
	 */
	public void run() {
		// TO BE COMPLETED
		
		Multiagent_Planning algo = new Multiagent_Planning(input);
		
		System.out.println("Prepare for algo");
		
		long startTime = System.currentTimeMillis();
		
		//ArrayList<byte[]> stepsArrayList = algo.reinforcement_learning();
		ArrayList<byte[]> stepsArrayList = algo.run();

		long endTime = System.currentTimeMillis();
		System.out.println("Total Time: " + (endTime - startTime));
		
		for(int i = 0;i<stepsArrayList.size();i++) {
			this.solution.addStep(stepsArrayList.get(i));
		}
		
		
		
		
		this.solution.getInstance(this.input);
		System.out.println("Solution computed, Validity: " + this.solution.isValid());
		
		
	}
	
	/**
	 * Add a new motion step to the current solution
	 */
	public void computeOneStep() {
		int n=this.input.n; // number of robots
		
		throw new Error("TO BE COMPLETED");
	}

}
