import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.Vector;

/**
 * 
 * @author jijingtian yuyunhao
 *
 * @description: main function, OptimizeDistance and OptimizeMakespan will all use this class
 */


public class Multiagent_Planning {
	public Instance instance;
	public ArrayList<byte[]> testArrayList; // results
	public int nagent; 
	public Vector<Node> startNodes;
	public Vector<Node> targetNodes;
	public Multiagent_Planning(Instance input) {
		this.instance = input;
		this.instance.getBoundingBox();
		Coordinates startsCoordinates = input.starts;
		Coordinates tarCoordinates = input.targets;
		this.nagent = input.n;
		
		int[][] pos_agents = startsCoordinates.getPositions();
		int[][] target_agents = tarCoordinates.getPositions();
		
		this.startNodes = new Vector<Node>();
		this.targetNodes = new Vector<Node>();
		
		for(int i = 0;i<this.nagent;i++) {
			int init_x = pos_agents[0][i];
			int init_y = pos_agents[1][i];
			Node initNode = new Node(init_x, init_y,nagent);
			
			int tar_x = target_agents[0][i];
			int tar_y = target_agents[1][i];
			Node targetNode = new Node(tar_x,tar_y,nagent);
			Map.totalNodeHashMap.put(initNode.position, initNode);    // all the nodes are contained in the Map.totalNodeHashMap
			Map.totalNodeHashMap.put(targetNode.position, targetNode);
			startNodes.add(Map.totalNodeHashMap.get(initNode.position));
			targetNodes.add(Map.totalNodeHashMap.get(targetNode.position));
				
		}
		for(int i = 0;i<this.nagent;i++) {
			
			Agent agent  = new Agent(new Position(pos_agents[0][i], pos_agents[1][i]),new Position(target_agents[0][i], target_agents[1][i]), i,input);
			Map.agents.add(agent);
		}
		
		
		for(int i = 0;i<input.obstacles.n;i++) {
			Position position = new Position(input.obstacles.getX(i), input.obstacles.getY(i));
			Map.obstacleSet.add(position);
		}
		System.out.println("Obstacles: " + Map.obstacleSet.size());
		
		// we will add the boundaries that we define
		Map.obstacleSet.addAll(this.makeObstacle(this.instance.xmin-3, this.instance.xmax+3, this.instance.ymin-3, this.instance.ymax+3));
		System.out.println("Map Size: " +  (this.instance.xmax -this.instance.xmin + 6)  + " * "  + (this.instance.ymax - this.instance.ymin+6)  );
		

	}
	
	/**
	 * @deprecated this method is experimental
	 * @return
	 */
	public ArrayList<byte[]> reinforcement_learning(){
		int n_rounds = 100;
		int N = 1000;
		int max_steps = 100;
		double gamma = 0.9;
		boolean  minmakespan = false;
		
		Vector<Node> initNodes = new Vector<Node>();
		Vector<Node> endNodes = new Vector<Node>();
		for(int i = 0;i<this.nagent;i++) {
			//Agent tmpAgent = this.agent_list.get(i);
			Agent tmpAgent = Map.agents.get(i);
			tmpAgent.Initialization();
			tmpAgent.ComputeShortestPath();
			}
		RL rl = new RL(n_rounds,max_steps,N,gamma,minmakespan,this.startNodes,this.targetNodes);
		return rl.run();
		
	}
	/**
	 * @deprecated this method is experimental
	 * @return
	 */
	
	public ArrayList<byte[]> BFS(){
		Vector<Node> startNodes = new Vector<Node>();
		Vector<Node> endNodes = new Vector<Node>();
		
		for(Agent i:Map.agents) {
			startNodes.add(i.curNode);
			endNodes.add(i.tarNode);
		}
		//System.out.println(startNodes.size());
		//System.out.println(this.instance.xmin +" "+ this.instance.xmax + " " +this.instance.ymin +" "+this.instance.ymax);
		Global_Planning gp = new Global_Planning(this.instance,startNodes, endNodes);
		//System.out.println(gp.startState.size());
		gp.initialization();
		Stack<State> steps = new Stack<State>();
		gp.plan(steps);
		ArrayList<byte[]> resArrayList = gp.getSteps(steps);
		//System.out.println(resArrayList.get(1)[1]);
		return resArrayList;
	}
	
	/**
	 * @description: main function to return the solution
	 * @return
	 */
	public ArrayList<byte[]> run(){
		
		// initialization	
		Vector<Node> initNodes = new Vector<Node>();
		Vector<Node> endNodes = new Vector<Node>();
		for(int i = 0;i<this.nagent;i++) {
			//Agent tmpAgent = this.agent_list.get(i);
			Agent tmpAgent = Map.agents.get(i);
			initNodes.add(tmpAgent.curNode);
			endNodes.add(tmpAgent.tarNode);
			tmpAgent.Initialization();
			tmpAgent.ComputeShortestPath();
		}
		// calculate the all pairs'shortest path
		Map.Construct_Map();
		// Generic Information
		System.out.println("Global Map Constructed");
		double feasible_node = Map.totalNodeHashMap.size();
		double density = this.nagent/feasible_node;
		System.out.println("Density: " + density );
		int optimal_makespan = 0;
		int optimal_total_distance = 0;
		for(int i = 0;i<nagent;i++) {
			int dis = Map.getDistance(Map.agents.get(i).curNode, Map.agents.get(i).tarNode);
			if(dis > optimal_makespan) {
				optimal_makespan = dis;
			}
			optimal_total_distance+= dis;
			
		}
		System.out.println("Theoretical Makespan: " + optimal_makespan + " Theoretical Distance: " + optimal_total_distance);
		
		Map.trajectoryQueue.addLast(new State(initNodes));
		Vector<Node> curstepsNodes = new Vector<Node>();		
		
		// Loop end when we reach the endNodes
		while(!curstepsNodes.equals(endNodes)) {
			
			curstepsNodes = Map.recuit_simule(); // we get the next nodes
			for(int i = 0;i<this.nagent;i++) {   // clean the direction mark
					Map.agents.get(i).curNode.direction = Node.None;
					Map.agents.get(i).nexNode.direction = Node.None;
				}
				
				
			Map.trajectoryQueue.addLast(new State(curstepsNodes));
			
			/**
			 * this is a experimental function, we track each agent's last 5 movement
			 */
			for(int i = 0;i<this.nagent;i++) {
				Map.agents.get(i).self_history.add(Map.agents.get(i).curNode);
				if(Map.agents.get(i).self_history.size()>=5) {
					Map.agents.get(i).self_history.poll();
				}
				Map.agents.get(i).progress += Map.agents.get(i).curNode.g[i] - Map.agents.get(i).nexNode.g[i];
				Map.agents.get(i).curNode = Map.agents.get(i).nexNode;
			}
			
			// avoid the potential overflow
			if(Map.trajectoryQueue.size() >500) {
				break;
			}
		}
		
		/**
		 * we test original solution and optimized solution for the results in experiment
		 */
		
		ArrayList<State> original_solutionArrayList = new ArrayList<State>();
		
		ArrayList<State> optimized_solutionArrayList1 = new ArrayList<State>();
		
		ArrayList<State> optimized_solutionArrayList2 = new ArrayList<State>();
		
		original_solutionArrayList.addAll(Map.trajectoryQueue);
		Solution originalSolution = this.trajectory2solution(original_solutionArrayList);
		System.out.println("Original Solutioin: " + "Total Step: " + originalSolution.makespan() + " Total Distance: " + originalSolution.getTotalDistance());
		
		// 现在我们认为 queue 已经尾部是 endstate，将逐个
		ArrayList<byte[]> resArrayList = new ArrayList<byte[]>();
		// 去除抖动解
		AugementSolution AS = new AugementSolution(Map.trajectoryQueue);
		Map.trajectoryQueue = AS.getSolution();
		optimized_solutionArrayList1.addAll(Map.trajectoryQueue);
		Solution optimizedSolution1 = this.trajectory2solution(optimized_solutionArrayList1);
		System.out.println("Optimized Solutioin1: " + "Total Step: " + optimizedSolution1.makespan() + " Total Distance: " + optimizedSolution1.getTotalDistance());

			
		
		
		
	
		AugmentSolution2 AS2 = new AugmentSolution2(Map.trajectoryQueue);
		Deque<State> testDeque= AS2.getSolution();
		optimized_solutionArrayList2.addAll(testDeque);
		Solution optimizedSolution2 = this.trajectory2solution(optimized_solutionArrayList2);
		System.out.println("Optimized Solutioin2: " + "Total Step: " + optimizedSolution2.makespan() + " Total Distance: " + optimizedSolution2.getTotalDistance());


		
		Map.trajectoryQueue = testDeque;
		Vector<Node> firstNodes = Map.trajectoryQueue.pollFirst().stateNodes;
		boolean conflict = true;
		int ct = 0;
		while(!Map.trajectoryQueue.isEmpty()) {
			Vector<Node> secondNodes = Map.trajectoryQueue.pollFirst().stateNodes;
			conflict = conflict && RL.isValidTransition(firstNodes, secondNodes);
			if(!conflict) {
				System.out.println("First Conflit: " + ct);
			}else {
				ct++;
			}
			resArrayList.add(this.getStepshelper(firstNodes, secondNodes));
			firstNodes = secondNodes;
		}
		System.out.println("Conflict? " + conflict);
		
		
		
		return resArrayList;
	}
	
	/**
	 * 
	 * @param states: trajectory
	 * @return Solution 
	 */
	public Solution trajectory2solution(ArrayList<State> states){
		Vector<Node> first_nodes = states.get(0).stateNodes;
		Solution solution = new Solution("test");
		for(int i = 1;i<states.size();i++) {
			Vector<Node> secondNodes = states.get(i).stateNodes;
			solution.addStep(this.getStepshelper(first_nodes, secondNodes));
			first_nodes = secondNodes;
		}
		return solution;

	}
	
	/**
	 * 
	 * @param xmin boundary we deifne
	 * @param xmax
	 * @param ymin
	 * @param ymax
	 * @return obstacles
	 */
	public HashSet<Position> makeObstacle(int xmin,int xmax,int ymin,int ymax){
		HashSet<Position> manmadeHashSet = new HashSet<Position>();
		for(int i = xmin;i<=xmax;i++) {
			manmadeHashSet.add(new Position(i, ymin));
			manmadeHashSet.add(new Position(i, ymax));
		}
		for(int i = ymin;i<=ymax;i++) {
			manmadeHashSet.add(new Position(xmin, i));
			manmadeHashSet.add(new Position(xmax, i));
		}
		
		return manmadeHashSet;
				
	}
	
	
	/**
	 * 
	 * @param startState
	 * @param nextState
	 * @return transform transition to movement N S W E
	 */
	
	public byte[] getStepshelper(Vector<Node> startState,Vector<Node> nextState) {
		int n = startState.size();
		byte[] step = new byte[n];
		for(int i = 0;i<n;i++) {
			byte move;
			Node n1Node = startState.get(i);
			Node n2Node = nextState.get(i);
			if(n2Node.x > n1Node.x) {
				move = 4;
			}else if (n2Node.x < n1Node.x) {
				move = 3;
			}else if(n2Node.y>n1Node.y) {
				move = 1;
			}else if(n2Node.y < n1Node.y) {
				move = 2;
			}else {
				move = 0;
			}
			step[i] = move;
			
		}
		return step;
	}
	
	

}
