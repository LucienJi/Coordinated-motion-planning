import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;

/**
 * @author: Jingtian Ji ,Yunhao Yu
 * @className: Map
 * @Description: For the MCMC Based Simulated Annealing and contain the all pairs' shortest distance
 * 
 */



public class Map {
	
	
	public static HashMap<Position, Node> totalNodeHashMap = new HashMap<Position, Node>();  // Contain all Nodes
	public static Vector<Agent> agents = new Vector<Agent>(); // Contain all Agents
	public static Deque<State> trajectoryQueue = new LinkedList<State>(); // State is equal to vector of Nodes, the queue contains the final solution
	public static HashSet<Position> obstacleSet = new HashSet<Position>(); // contain the obstacles
	public static int N_sim = 1000; // number of simulation for the simulated annealing
	public static HashMap<Position, HashMap<Position, Integer>> Distance = new HashMap<Position, HashMap<Position,Integer>>(); // contain all pairs'shortest distance
	
	public static void Construct_Map() {
		/**
		 * @description: use N*N times of BFS to calculate the all pairs'shortest distance
		 */
		Set<Position> positionSet = totalNodeHashMap.keySet();
		//System.out.println("Number of Node " + positionSet.size() );
		for(Position position:positionSet) {
			//System.out.println("Posi: " + position);
			//Node node = totalNodeHashMap.get(position);
			HashMap<Position, Integer> nodeHashMap = new HashMap<Position, Integer>();
			nodeHashMap.put(position, 0);
			Queue<Position> queue = new LinkedList<Position>();
			HashSet<Position> cloSet = new HashSet<Position>();
			queue.add(position);
			while (!queue.isEmpty()) {
				//System.out.println("Queue Size: " + queue.size());
				Position tmpPosition = queue.poll();
				int tmpdis = nodeHashMap.get(tmpPosition);
				cloSet.add(tmpPosition);
				Vector<Position> neighborsNodes = Map.getNeighbors(tmpPosition);
				for(Position neighborNode:neighborsNodes) {
					if(!cloSet.contains(neighborNode)) {
						nodeHashMap.put(neighborNode,tmpdis + 1);
						queue.remove(neighborNode);
						queue.add(neighborNode);
					}
				}
				
			}
			
			Distance.put(position, nodeHashMap);
			}
		}
	public static int getDistance(Node node,Node node2) {
		/**
		 * 
		 * @return: the shortest distance between node and node2
		 */
		return Map.Distance.get(node.position).get(node2.position);
	}
	
	public static Vector<Position> getNeighbors(Position position){
		/**
		 * @description: auxiliary function, used for Construct_Map
		 * @return： the neighbors of position and exclude the obstacles
		 */
		Vector<Position> respPositions = new Vector<Position>();
		int x =position.x;
		int y =position.y;
		Position p1Position = new Position(x+1, y);
		Position p2Position = new Position(x-1, y);
		Position p3Position = new Position(x, y+1);
		Position p4Position = new Position(x, y-1);
		Position[] Positions = {p1Position,p2Position,p3Position,p4Position};
		for(int i = 0;i<Positions.length;i++) {
			if(!Map.obstacleSet.contains(Positions[i])) {
				if(!Map.totalNodeHashMap.containsKey(Positions[i])) {
					Node tmpnode = new Node(Positions[i], Map.agents.size());
					Map.totalNodeHashMap.put(Positions[i], tmpnode);
					respPositions.add(Positions[i]);
				}else {
					respPositions.add(Positions[i]);
				};
			}
		}
		
		return respPositions;
	}

	
	private static double calcE(Node u,Node v) {
		/**
		 * @return: Euclidean distance
		 */
		return Math.sqrt(Math.pow(u.x - v.x, 2) + Math.pow(u.y - v.y,2));
	}
	
	public static double Value(Vector<Node> nodes) {
		/**
		 * @description: the value function for the simulated annealing, based on shortest distance and the compromised state
		 */
		if(nodes.size() < Map.agents.size()) {  // Invalid nodes get worst score
			return Integer.MAX_VALUE/2;
		}
		if(Map.trajectoryQueue.contains(new State(nodes))) {  // Repeated ndoes get worst score
			return Integer.MAX_VALUE/2;
		}
		
		int v = 0;
		for(int i = 0;i<nodes.size();i++) {
			if(Map.agents.get(i).iscompro == 0) {				// pure greedy action

				v+=Map.getDistance(Map.agents.get(i).tarNode, nodes.get(i));
				Map.agents.get(i).iscompro = 0;
			}else{
				v+=Map.getDistance(Map.agents.get(i).tarNode, nodes.get(i)) -3 ;// compromise to others
			
				}
			Map.agents.get(i).iscompro = 0;
		}
		return v;
	}
	
	private static Vector<Integer> Proposition_Greedy(Vector<Integer> order){
		/**
		 * @description: we implement different method to propose the order,
		 * this one is based on the progress of a certain agent which represent how close it is to its target, the closest get the top priority
		 * 
		 * @return: a new Order for Simulated Annealing
		 */
		if(order.size()==1) {
			return order;
		}
		HashMap<Integer, LinkedList<Integer>> progress2indexHashMap = new HashMap<Integer, LinkedList<Integer>>();
		for(int i = 0;i<Map.agents.size();i++) {
			int p = Map.agents.get(i).progress;
			if(progress2indexHashMap.containsKey(p)) {
				progress2indexHashMap.get(p).add(i);
			}else {
				progress2indexHashMap.put(p, new LinkedList<Integer>());
				progress2indexHashMap.get(p).add(i);
			}
		}
		Vector<Integer> next_orderIntegers =new Vector<Integer>();
		ArrayList<Integer> keySet = new ArrayList<Integer>(progress2indexHashMap.keySet());
		Collections.sort(keySet,Collections.reverseOrder());
		for(int p :keySet) {
			LinkedList<Integer> tmpIntegers = progress2indexHashMap.get(p);
			Collections.shuffle(tmpIntegers);
			next_orderIntegers.addAll(tmpIntegers);
		}
		return next_orderIntegers;
		
		
		
		
	}
	
	private static Vector<Integer> Proposition_helper(Vector<Integer> order){
		/**
		 * @description: classical method to permute the order with 2 elements
		 * @return: return a new order 
		 */
		if(order.size()==1) {
			return order;
		}
		Vector<Integer> order_copyIntegers =  new Vector<Integer>();
		for(int i :order) {
			order_copyIntegers.add(i);
		}
		int n = order.size();
		double d = Math.random();
		int i = (int)(d*n);
		
		d = Math.random();
		int j = (int)(d*n);
		while(i==j) {
			d = Math.random();
			j = (int)(d*n);
		}
		Collections.swap(order_copyIntegers,i,j);
		return order_copyIntegers;
		
	}
	public static Vector<Integer> Proposition_helper_Test(Vector<Integer> order){
		/**
		 * @description: First randomly choose the i and j then we shuffle all elements in [i,j]
		 * @return: a new order
		 */
		if(order.size()==1) {
			return order;
		}
		Vector<Integer> order_copyIntegers =  new Vector<Integer>();
		for(int i :order) {
			order_copyIntegers.add(i);
		}
		
		int n = order.size();
		double d = Math.random();
		int j = (int)(d*n);
		int k = (int)(d*n);
		while(k==j) {
			d = Math.random();
			k = (int)(d*n);
		}
		List<Integer> subIntegers = order_copyIntegers.subList(Math.min(j, k), Math.max(j, k));
		Collections.shuffle(subIntegers);
		return order_copyIntegers;
		
		
	}
	static Vector<Integer> Proposition(Vector<Integer> init_order,Vector<Node> nextNodes){
		/**
		 * @description: use the proposition_helper method to generate new order and fill in the Vector<Node> nextNodes
		 * this is the main function in the simulated annealing
		 * @return: the new order we finally chosen 
		 */
		boolean valid = false;
		Vector<Integer> next_orderIntegers = new Vector<Integer>();
		int ct = 0;
		while(!valid) {
			nextNodes.clear();
			Map.clear_direction();
			if(ct < Map.agents.size() * Map.agents.size()) {
				next_orderIntegers = Map.Proposition_helper(init_order);
			}else {
				next_orderIntegers = Map.Proposition_helper_Test(init_order);
				init_order = next_orderIntegers;
				ct = 0;
			}			
			valid = Map.order2nodes(next_orderIntegers, nextNodes);
			Map.clear_direction();
			ct++;
		}		
		return next_orderIntegers;
	}
	
	private static double T (int i ) {
		/**
		 * @description: Temperature for simulated annealing
		 */
		return 300*(Math.pow(i+1, -0.03));
		
	}
	
	public static Boolean order2nodes(Vector<Integer> order,Vector<Node> nextNodes) {
		/**
		 * @description: the link between order and Nodes, based on the order we use the method for Agent "recuit_simule_runhelper" to get the potential nextNods
		 * @return if the order is valid, return true else return false and we will change the order
		 */
		
		HashMap<Integer, Node> pre_nodes = new HashMap<Integer, Node>();
		boolean res;
		for(int i :order) {
			res = Map.agents.get(i).recuit_simule_runhelper(pre_nodes);
			if(!res) {
				return res;
			}
		}
		for(int i = 0;i<order.size();i++) {
			nextNodes.add(pre_nodes.get(i));
		}
		return true;
		
		
		
	}
	
	public static Vector<Node> recuit_simule() {
		/**
		 * @description: main function of simulated annealing. we sample N times to get the best next State
		 * @return: the final best next state
		 */
		int N = Map.N_sim;
		Vector<Integer> init_order = new Vector<Integer>();
		for(int i = 0;i<Map.agents.size();i++) {
			init_order.add(i);
		}
		int n = init_order.size();
		Vector<Integer> best_order = init_order;
		Vector<Node> best_nextNodes = new Vector<Node>();
		for(Agent agent:Map.agents) {
			best_nextNodes.add(agent.curNode);
		}
		Vector<Integer> curOrderIntegers = init_order;
		Vector<Integer> nextOrderIntegers = new Vector<Integer>();
		double best_score = Map.Value(best_nextNodes);
		double cur_score = Integer.MAX_VALUE/2;  // For the simplicity, we set the worst score to be Integer.MAX_VALUE/2
		double next_score;
		double h;
		double un;
		for(int i = 0;i<N;i++) {
			Vector<Node> nextNodes = new Vector<Node>();
			nextOrderIntegers = Map.Proposition(curOrderIntegers, nextNodes);
			next_score = Map.Value(nextNodes);
			h = Math.min(1, Math.exp((cur_score - next_score)/Map.T(i))); // acceptance probability
			un = Math.random();
			if(h>un) {
				curOrderIntegers = nextOrderIntegers;
				cur_score = next_score;
				if(cur_score <= best_score) {
					best_score = cur_score;
					best_nextNodes = nextNodes;
	
					best_order = curOrderIntegers;
				}
			}
	
		}
		for(int i = 0;i<Map.agents.size();i++) {
			agents.get(i).nexNode = best_nextNodes.get(i);
		}
		
		return best_nextNodes;
		
		
		
	}
	
	public static void clear_direction() {
		/**
		 * @description: auxiliary function, to clear the direction mark on the nodes
		 */
		for(Agent agent:Map.agents) {
			agent.curNode.direction = Node.None;
			agent.nexNode.direction = Node.None;
		}
	}

}

