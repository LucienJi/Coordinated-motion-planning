import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;
/**
 * 
 * @author jijingtian
 * @description: we tried to use the reinforcement learning method, it works but it takes too much time! 
 * 
 *
 */
public class RL {
	
	public int n_rounds;
	public int N;
	public int max_steps;
	public double gamma;
	public double lr;
	public boolean  minmakespan;
	public int nagent;
	public Vector<Node> startNodes;
	public Vector<Node> targetNodes;
	public HashMap<Vector<Node>, Double> ValueTable;
	public HashMap<Vector<Node>, Vector<Node>> FastSearch;
	private double epsilon;
	private Deque<Vector<Node>> training_trajectoryDeque = new LinkedList<Vector<Node>>();
	private Deque<Integer> training_rewardDeque  = new LinkedList<Integer>();
	private double higher_bound;
	
	
	public RL(int n_rounds,int max_steps,int N, double gamma,boolean makespan,Vector<Node> startNodes,Vector<Node> targetNodes) {
		
		this.minmakespan = false;
		this.n_rounds = n_rounds;
		this.max_steps = max_steps;
		this.N = N;
		this.gamma = gamma;
		this.lr = 0.01;
		this.ValueTable = new HashMap<Vector<Node>, Double>();
		this.FastSearch = new HashMap<Vector<Node>, Vector<Node>>();
		this.startNodes = startNodes;
		this.targetNodes = targetNodes;
		this.nagent = targetNodes.size();
		
	}
	
	
	public ArrayList<byte[]> run(){
		Map.Construct_Map();
		
		this.higher_bound = Map.Value(startNodes)*1.5;

		for(int i = 0;i<this.n_rounds;i++) {
			System.out.println(">>>>Round " + (i+1) + "<<<<");
			this.training();

		}
		Deque<Vector<Node>> trajectory = new LinkedList<Vector<Node>>();
		Vector<Node> currentNodes = this.startNodes;
	
		this.Reset();
		trajectory.addLast(currentNodes);
		while(!currentNodes.equals(targetNodes)) {
				currentNodes = this.FastSearch.get(currentNodes);
				currentNodes = this.Policy_TestVersion(currentNodes);
			trajectory.addLast(currentNodes);
		}
		ArrayList<byte[]> resArrayList = new ArrayList<byte[]>();
		Vector<Node> tmp1 = trajectory.pollFirst();
		while(!trajectory.isEmpty()) {
			Vector<Node> tmp2 = trajectory.pollFirst();
			resArrayList.add(this.getStepshelper(tmp1, tmp2));
			tmp1 = tmp2;
		}
		return resArrayList;
		
	}
	
	
	
	private double greedy(int i) {
		return 1.0 - Math.exp(-this.n_rounds/(4*i));
		
	}

	
	
	public void training() {
		int step = 0;
		boolean done = false;
		Vector<Node> currentNodes = this.startNodes;
		Vector<Node> nextNodes = new Vector<Node>();
		this.Reset();
		
		this.training_trajectoryDeque.addLast(currentNodes);

		while(step<=this.max_steps && !done) {
			
			nextNodes = this.Policy_TestVersion(currentNodes);
			//nextNodes = this.Policy(currentNodes);
			int reward = this.reward(currentNodes, nextNodes);
			this.training_trajectoryDeque.addLast(nextNodes);
			this.training_rewardDeque.addLast(reward);
	
			// online update
			done = this.Set(nextNodes);
			currentNodes = nextNodes;
			step++;
			
		}

		this.Update_TestVersion(this.training_trajectoryDeque, this.training_rewardDeque);
		
		
	}
	
	public Vector<Node> Policy_TestVersion(Vector<Node> nodes){
		
		Vector<Integer> init_order = new Vector<Integer>();
		for(int i = 0;i<Map.agents.size();i++) {
			init_order.add(i);
		}
		Vector<Node> best_nextNodes = nodes;
		Vector<Integer> curOrderIntegers = init_order;
		Vector<Integer> nextOrderIntegers = new Vector<Integer>();
		double best_score = this.Value(best_nextNodes);
		double cur_score = Integer.MAX_VALUE/2;
		double next_score;
		double h;
		double un;
		for(int i = 0;i<this.N;i++) {
			Vector<Node> nextNodes = new Vector<Node>();
			nextOrderIntegers = Map.Proposition(curOrderIntegers, nextNodes);
			
			next_score = this.Value(nextNodes);
			h = Math.min(1, Math.exp((cur_score - next_score)/this.Temp(i)));
			un = Math.random();
			if(h>un) {
				curOrderIntegers = nextOrderIntegers;
				cur_score = next_score;
				if(cur_score <= best_score) {
					best_score = cur_score;
					best_nextNodes = nextNodes;
					
				}
			}
	
		}
		for(int i = 0;i<Map.agents.size();i++) {
			Map.agents.get(i).nexNode = best_nextNodes.get(i);
		}
		
		return best_nextNodes ;
	}
	public Vector<Node> Policy(Vector<Node> nodes){
		
				// in case of being fixed
		Vector<Node> currentNodes = nodes;
		/*
		if(this.FastSearch.containsKey(currentNodes)) {
			if(Math.random() > this.epsilon){
				return this.FastSearch.get(currentNodes);
			}
		}*/
		Vector<Node> nextNodes = new Vector<Node>();
		Vector<Node> best_nodes = currentNodes;
		double best_value = this.Value(best_nodes);
		double current_value = best_value;
		double next_value;
		double temperature;
		double h;
		double u;
		for(int i = 0;i<this.N;i++) {
			//System.out.println(">>>>>>>>>>>>>>Iteration " + (i+1) + " <<<<<<<<<");
			nextNodes = this.NodesGenerator(currentNodes);
			//nextNodes = this.NodesGeneratorTestVersion();
			//System.out.println("Test  " + nextNodes.equals(this.targetNodes));
			next_value = this.Value(nextNodes);
			temperature = this.Temp(i);
			h = Math.min(1, Math.exp((current_value - next_value)/temperature));
			u = Math.random();
			if(h>u) {
				current_value = next_value;
				//currentNodes = nextNodes;
				if(current_value < best_value) {
					best_value = current_value;
					best_nodes = nextNodes;
				}
			}
			
		}
		
		
		return best_nodes;
		
		
	}
	private double Temp (int i ) {
		
		return 300*(Math.pow(i+1, -0.03));
		
	}
	
	public double Value(Vector<Node> nodes) {
		if(nodes.size() < Map.agents.size()) {
			return Integer.MAX_VALUE/2;
		}
		if(!this.ValueTable.containsKey(nodes)) {
			return initialize_value(nodes);
		}
		return this.ValueTable.get(nodes);
	
		
	}
	
	private double initialize_value(Vector<Node> nodes) {
		double v = 0;
		if(this.minmakespan) {
			for(int i = 0;i<nodes.size();i++) {
				v = Math.max(v, Map.getDistance(nodes.get(i), this.targetNodes.get(i)));
			}
			
		}else {
				if(nodes.equals(this.startNodes)) {
					//sSystem.out.println(Map.Value(nodes));
				}
				v = Map.Value(nodes);
				//System.out.println("Initialize: " + nodes + " " + v);


		}
		if(v < this.higher_bound) {
			return v;
		}else {
			return Integer.MAX_VALUE/2;
		}
	}
	
	
	public Vector<Node> NodesGenerator(Vector<Node> nodes){
		boolean valid = false;
		Vector<Node> tentativeNodes = new Vector<Node>();
		while(!valid) {
			tentativeNodes = this.NodesGenerator_helper(nodes);
			valid = this.isValidTransition(nodes, tentativeNodes);
			//System.out.println("Valid: " + valid);
		}
		return tentativeNodes;
	}
	
	
	private Vector<Node> NodesGenerator_helper(Vector<Node> nodes){
		
		Vector<Node> resNodes = new Vector<Node>();
		
		int n;
		double d;
		int i;
		for(Node node:nodes) {
			Vector<Node> tmpNodes = this.getNeighborNodes(node);
			n = tmpNodes.size();
			d = Math.random();
			i = (int)(d*n);
			resNodes.add(tmpNodes.get(i));
		}
		return resNodes;
		
	}
	public Vector<Node> NodesGeneratorTestVersion(){
		// 只能生产可行的 nodes
		boolean valid = false;
		Vector<Node> nextNodes = new Vector<Node>();
		while(!valid) {
			nextNodes = new Vector<Node>();
			Map.clear_direction();
			Vector<Integer> order = this.randomorder();
			valid = Map.order2nodes(order, nextNodes);
		}
		return  nextNodes;
		
		
	}
	private Vector<Integer> randomorder(){
		Vector<Integer> resIntegers = new Vector<Integer>();
		while (resIntegers.size() < this.nagent) {
			double d = Math.random();
			int i = (int)(d*this.nagent);
			if(!resIntegers.contains(i)) {
				resIntegers.add(i);
			}
		}
		return resIntegers;
	}
		
	
	
	public Vector<Node> getNeighborNodes(Node n){
		Vector<Node> resNodes = new Vector<Node>();
		int x =n.x;
		int y =n.y;
		Position p1Position = new Position(x+1, y);
		Position p2Position = new Position(x-1, y);
		Position p3Position = new Position(x, y+1);
		Position p4Position = new Position(x, y-1);
		Position[] Positions = {p1Position,p2Position,p3Position,p4Position};
		for(int i = 0;i<Positions.length;i++) {
			if(isValidPoint(Positions[i])) {
				if(!Map.totalNodeHashMap.containsKey(Positions[i])) {
					Node node = new Node(Positions[i], this.nagent);
					Map.totalNodeHashMap.put(Positions[i], node);
					resNodes.add(node);
				}else {
					resNodes.add(Map.totalNodeHashMap.get(Positions[i]));
				};
			}
		}
		
		return resNodes;
	}
	
	private static boolean isValidPoint(Position p) {
		return !Map.obstacleSet.contains(p);

	}
	
	static public boolean isValidTransition(Vector<Node> nodes,Vector<Node> nodes2) {
		for(int i = 0;i<nodes.size();i++) {
			nodes.get(i).direction = Node.None;
			nodes2.get(i).direction = Node.None;
		}
		boolean res;
		Vector<Node> tmpVector = new Vector<Node>();
		for(int i = 0;i<nodes.size();i++) {
			Node tmpNode1 = nodes.get(i);
			Node tmpNode2 = nodes2.get(i);
			res = (!tmpVector.contains(tmpNode2)) && RL.isValidTransition_helper(tmpNode1,tmpNode2);
			if(res) {
				tmpVector.add(nodes2.get(i));
				tmpNode1.direction = tmpNode1.calcDirection(tmpNode2);
				tmpNode2.direction = tmpNode1.direction;
			}else {
				return false;
			}
		}
		
		return true;
		
	}
	
	private static boolean isValidTransition_helper(Node startNode,Node endNode) {
		
		if(RL.isValidPoint(endNode.position)) {
			if(startNode.direction!=Node.None) {
				if(endNode.direction == Node.None) {
					return startNode.calcDirection(endNode) == startNode.direction;
				}else {
					return (startNode.calcDirection(endNode) == startNode.direction) && (startNode.calcDirection(endNode)==endNode.direction);
				}
			}else {
				if(endNode.direction == Node.None) {
					return true;
				}else {
					return startNode.calcDirection(endNode) == endNode.direction;
			
			}
				}
		}else {
			return false;
		}
		
	
	}
	
	public int reward(Vector<Node> nodes,Vector<Node> nodes2) {
		if(nodes.equals(nodes2)) {
			// We don't want to be fixed
			return 10;
		}
		
		if(this.minmakespan) {
			return 1;
		}else {
			int res = 0;
			for(int i = 0; i < nodes.size();i++) {
				if(!nodes.get(i).equals(nodes2.get(i))) {
					res++;
				}
			}
			
			return res;
		}
		
	}
	
	public void Update(Vector<Node> nodes,Vector<Node> nodes2,int reward) {
		// 要被update 的点，必定都是在policy 中被选出来的，起码也是次优点，值的我们将它记录在 hashtable中
		double old_value;
		double next_value;
		if(this.ValueTable.containsKey(nodes)) {
			old_value = this.ValueTable.get(nodes);
		}else {
			this.ValueTable.put(nodes, this.Value(nodes));
			old_value = this.ValueTable.get(nodes);
		}
		
		if(this.ValueTable.containsKey(nodes2)) {
			next_value = this.ValueTable.get(nodes2);
		}else {
			this.ValueTable.put(nodes2, this.Value(nodes2));
			next_value = this.ValueTable.get(nodes2);
		}
		
		double new_value = old_value + this.lr*(reward + this.gamma * next_value - old_value);
		this.ValueTable.put(nodes, new_value);
		
		if(!this.FastSearch.containsKey(nodes)) {
			this.FastSearch.put(nodes, nodes2);
			
		}else {
			if(this.ValueTable.get(this.FastSearch.get(nodes)) > this.ValueTable.get(nodes2)) {
				this.FastSearch.put(nodes, nodes2);
			}
		}
		
	}
	public void Update_TestVersion(Deque<Vector<Node>> trajectory, Deque<Integer> rewards) {
		Vector<Node> target = trajectory.pollLast();
		while(trajectory.size()>0) {
			
			Vector<Node> currNodes = trajectory.pollLast();
			if(trajectory.size() == 2) {
				System.out.println("Check: " + this.Value(currNodes));
			}
			int rew = rewards.pollLast();
			this.Update(currNodes, target, rew);
			if(trajectory.size() == 2) {
				System.out.println("Check: " + this.Value(currNodes));
			}
			target = currNodes;
		}
	}
	
	private byte[] getStepshelper(Vector<Node> startNodes,Vector<Node> nextNodes) {
		int n = startNodes.size();
		byte[] step = new byte[n];
		for(int i = 0;i<n;i++) {
			byte move;
			Node n1Node = startNodes.get(i);
			Node n2Node = nextNodes.get(i);
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
	private boolean Set(Vector<Node> nextNodes) {
		for(int i = 0;i<this.nagent;i++) {
			Map.agents.get(i).curNode = nextNodes.get(i);
		}
		return nextNodes.equals(this.targetNodes);
	}
	
	private void Reset() {
		for(int i = 0;i<nagent;i++) {
			Map.agents.get(i).curNode = this.startNodes.get(i);
			Map.agents.get(i).tarNode = this.targetNodes.get(i);
			Map.agents.get(i).nexNode = this.startNodes.get(i);

		}
		this.training_trajectoryDeque = new LinkedList<Vector<Node>>();
		this.training_rewardDeque = new LinkedList<Integer>();
		Map.clear_direction();
	}
	

}
