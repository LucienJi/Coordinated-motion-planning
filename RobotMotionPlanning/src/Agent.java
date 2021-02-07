import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;


/**
 * 
 * @author jijingtian yuyunhao
 * @description: we agent class to track a single agent's state like: its current position,next position and whether it's in the compromised state
 * we implement the greedy and compromise search 
 * we implement the Dijkstra method to compute the shortest path, in our case BFS can attain the same result. And to ensure all nodes to be initialized, we don't use the A* 
 * 
 */

public class Agent {
	/**
	 * @description: main attributes of an agent, 
	 */
	public Node curNode; 
	public Node nexNode;
	public Node tarNode;
	public Instance instance;
	public Boolean finishied = false;
	public int progress; // how close it is to its target
	
	public int iscompro = 0; // 0: greedy state, 1: compromised state
	public int index; // index is assigned to each agent to keep track of them
	public int nagent;
	public Queue<Node> self_history;
	
	private MyPriorityQueue opensetNodes; // container for BFS
	private HashMap<Position,Node> closesetNodes; // container for BFS
		
	public Agent(Position curPosition,Position tarPosition,int index,Instance input) {
		/**
		 * @description: constructor function
		 * @param: 
		 * curPosition: initial position
		 * tarPosition: target position
		 * index: the index of this agent
		 * input: the instance of the problem
		 */
		
		this.curNode = Map.totalNodeHashMap.get(curPosition);
		this.nexNode = curNode;
		this.tarNode = Map.totalNodeHashMap.get(tarPosition);
		this.index = index;
		this.instance = input;
		this.instance.getBoundingBox();
		this.nagent = input.starts.n;
		this.opensetNodes = new MyPriorityQueue(index);
		this.closesetNodes = new HashMap<Position, Node>();
		this.self_history = new LinkedList<Node>();
		
		this.progress = 0;
				
	}
	
	public void Initialization() {
		/**
		 * @description: auxiliary function for the compute shortest path
		 */
		this.tarNode.setCost(0, this.calcH(tarNode, curNode),index);
		this.opensetNodes.add(this.tarNode);
		Map.totalNodeHashMap.put(new Position(this.curNode.x,this.curNode.y),this.curNode);
		Map.totalNodeHashMap.put(this.tarNode.position,this.tarNode);
		System.out.println("Agent "+index + " Initialized");
		
	}
	
	private int calcH(Node u,Node v) {
		/**
		 * @deprecated: used to calculate the heuristic cost
		 */
		return 0;
		//return Math.abs(u.x-v.x)+ Math.abs(u.y-v.y);
	}
	private double calcE(Node u,Node v) {
		/**
		 * @deprecated: used to calculate the heuristic cost
		 */
		return Math.sqrt(Math.pow(u.x - v.x, 2) + Math.pow(u.y - v.y,2));
	}
	
	public void ComputeShortestPath() {
		/**
		 * @description: BFS: source target is the agent's target node
		 */
				while(opensetNodes.size()>0) {
			
			Node tmpNode = opensetNodes.poll();

			this.closesetNodes.put(tmpNode.position, tmpNode);
			Vector<Node> neighborsNodes = this.getNeighborNodes(tmpNode);
			
			for(int i = 0;i<neighborsNodes.size();i++) {
				Node neighborNode = neighborsNodes.get(i);
				if(!(this.closesetNodes.containsKey(neighborNode.position))){
					boolean better = false;
					int tentative_cost = tmpNode.g[index] + 1;
					if(this.opensetNodes.contains(neighborNode)) {
						if(tentative_cost < neighborNode.g[index]) {
							better = true;
						}else {
							better = false;
						};
					}else {
						better = true;
					}
					if(better) {
						neighborNode.setCost(tentative_cost, this.calcH(neighborNode, this.curNode),index);  // 我在修改 curnode的时候其实并没有修改成功
						this.opensetNodes.remove(neighborNode);
						this.opensetNodes.add(neighborNode);
					}
					
				}
				
			}
		}
		
	}
		
	private boolean isValid(Node startNode,Node endNode) {
		/**
		 * @description : return true if the the startNode's direction and the endNode's direction won't cause conflict
		 */
		if(this.isValidPoint(endNode.position)) { // whether it's a obstacle
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
	
	public void single_generateNextnodeHelper(Vector<Node> nexNodes) {
		/**
		 * @description: add the next node to Vector according to the agent's current node, the nexNodes is the group's next nodes
		 */
		Node node = this.oneStephelper();
		while(nexNodes.contains(node)) {
			this.curNode = node;
			node = this.oneStephelper();
		}
		nexNodes.add(node);
	}
	private boolean isOrder_compromise(Agent selfAgent,Agent otherAgent) {
		/**
		 * @description: it correspond to the determination of the Sequence Problem in out paper
		 */
		boolean res1 = otherAgent.curNode.g[otherAgent.index] > selfAgent.curNode.g[selfAgent.index];
		boolean res2 = selfAgent.tarNode.g[otherAgent.index] < otherAgent.curNode.g[otherAgent.index];
		boolean res3 = Map.getDistance(otherAgent.curNode, selfAgent.tarNode) < Map.getDistance(otherAgent.curNode, otherAgent.tarNode);
		boolean res4 = selfAgent.curNode.g[otherAgent.index] <  otherAgent.curNode.g[otherAgent.index];
		return res1 && res2 && res3 && res4 && this.isProximity(selfAgent, otherAgent, 2);
		
	}
	private Node tackleOrderCompromise(int idx,HashMap<Integer, Node> nextNodes, Vector<Node> neighborsNodes) {
		/**
		 * @description: More likely to choose the next node which keep away from the other agent's target and the other agent
		 */
		RandomCollection<Node> rc = new RandomCollection<Node>(); // random node generator
		neighborsNodes.add(this.curNode);
		
		for(Node node:neighborsNodes) {
			int dis = Map.getDistance(this.curNode, Map.agents.get(idx).tarNode);
			if(!nextNodes.containsValue(node) && this.isValid(this.curNode, node)) {
				if(Map.getDistance(node, Map.agents.get(idx).tarNode) >= dis) {
					rc.add(5, node);
				}else {
					rc.add(1, node);
				}
			}
		}
		return rc.next();
	}
	
	private boolean isConflict_compromise(Agent selfAgent,Agent otherAgent) {
		/**
		 * @description: it correspond to the determination of the Collision Problem in out paper
		 */
		boolean res1 = selfAgent.curNode.g[otherAgent.index] < otherAgent.curNode.g[otherAgent.index];
		boolean res2 = Map.getDistance(selfAgent.curNode, otherAgent.tarNode) < Map.getDistance(otherAgent.curNode, otherAgent.tarNode);
		
		boolean res3 = otherAgent.curNode.g[selfAgent.index] < selfAgent.curNode.g[selfAgent.index];
		boolean res4 = Map.getDistance(otherAgent.curNode, selfAgent.tarNode) < Map.getDistance(selfAgent.curNode,selfAgent.tarNode);
		
		return res1 && res2 && res3 && res4 && this.isProximity(selfAgent, otherAgent, 3);
	}
	
	private Node tackleConflictCompromise(int idx,HashMap<Integer, Node> nextNodes, Vector<Node> neighborsNodes) {
		/**
		 * @description: more likely to choose the node which keep away from the agent and its target. It;s more preferential to move perpendicular to the other
		 */
	
		RandomCollection<Node> rc = new RandomCollection<Node>();
		neighborsNodes.add(this.curNode);
		for(Node node: neighborsNodes) {
			int dis1 = Map.getDistance(this.curNode,Map.agents.get(idx).curNode);
			int dis2 = Map.getDistance(this.curNode,Map.agents.get(idx).tarNode);
			if(!nextNodes.containsValue(node) && this.isValid(this.curNode, node)) {
				double w = 1;
				if(Map.getDistance(node, Map.agents.get(idx).curNode)>= dis1 && Map.getDistance(node, Map.agents.get(idx).tarNode) >=dis2) {
					if((node.x != Map.agents.get(idx).curNode.x) && (node.y != Map.agents.get(idx).curNode.y)){
						w+=5;
					}else {
						w+=1;
					}
				}
				rc.add(w, node);
			}
		}
		if(rc.size() == 0) {
			return curNode;
		}
		
		return rc.next();
		
	}
	
	private boolean isProximity(Agent agent1,Agent agent2,int dis) {
	/**
	 * @description: when two agents are close enough, we will use compromise judgment
	 */
		return Map.getDistance(agent1.curNode, agent2.curNode)<=dis;
	}
	private int isCompromise() {
		/**
		 * @description: use isOrder_compromis, isConflict_compromise to determine the whether the agent need to be set in the compromised state
		 * @return the index of that agent to which this agent should make compromise(if it's own index, it means that we don't need to make compromise) 
		 */
		int max_g = 0;
		int max_idx = this.index;
		for(int i = 0 ;i<this.nagent;i++) {
			if(Map.agents.get(i).curNode.g[i] > max_g &&(this.isOrder_compromise(Map.agents.get(this.index), Map.agents.get(i)) || this.isConflict_compromise(Map.agents.get(this.index), Map.agents.get(i)))) {
				max_idx = i;
				max_g = Map.agents.get(i).curNode.g[i];
			}
		}
		if(max_idx!=this.index) {
			this.iscompro = 1;
			Map.agents.get(max_idx).iscompro = 2;
		}
		
		
		return max_idx;
	}
	
	public boolean recuit_simule_runhelper(HashMap<Integer, Node> nextNodes) {
		/**
		 * @description: Input is the container of nextNodes chose by other agents, nextNodes: index->node,because they are executed in a different order so we use hashmap
		 * @return: return true if we add a conflict-free node to the nextNodes; return false if in this order no valid movement can be chose
		 */
		Vector<Node> neighNodes = getNeighborNodes(curNode);
		
		/**
		 * @description:  other agent will move to this agent's current node, so this agent has no choice
		 */
		if(this.curNode.direction!=Node.None) {
			for(Node node:neighNodes) {
				if(!nextNodes.containsValue(node)&&this.isValid(this.curNode, node)) {
					nextNodes.put(this.index, node);
					node.direction = this.curNode.direction;
					this.nexNode = node;
					this.iscompro = 1;
					return true;
				}
			}
			return false;
		}
		
		int compromise = this.isCompromise();
		Node min_node = this.curNode;
		/**
		 * @description: in the compromised state
		 */
		if(compromise!=this.index) {
			if(this.isConflict_compromise(Map.agents.get(this.index), Map.agents.get(compromise))) {
				min_node = this.tackleConflictCompromise(compromise, nextNodes, neighNodes);	
				
			}else {
				min_node = this.tackleOrderCompromise(compromise, nextNodes, neighNodes);
			}
			
		/**
		 * @description: in the greedy state
		 */
		}else {
			int min_g = curNode.g[index];
			for(Node node:neighNodes) {
				if(!nextNodes.containsValue(node) && this.isValid(this.curNode, node) && node.g[index] < min_g) {
					min_g = node.g[index];
					min_node = node;
				}
			}
		}
		
		/**
		 * @description: check the direction in the chose node, and leave the direction mark on the current node and the next node
		 */
		if(min_node.equals(this.curNode)) {
			if(min_node.calcDirection(min_node) == min_node.direction) {
				nextNodes.put(this.index, min_node);
				this.nexNode = min_node;
				min_node.direction = this.curNode.calcDirection(min_node);
				curNode.direction = min_node.direction;
				return true;
			}else {
				this.nexNode = curNode;
				return false;
			}
		}
		nextNodes.put(this.index, min_node);
		min_node.direction = this.curNode.calcDirection(min_node);
		curNode.direction = min_node.direction;
		this.nexNode = min_node;
		
		

		return true;
		
	}
	
	public boolean single_runhelper(Vector<Node> nextNodes) {
		/**
		 * @deprecated
		 */
		Vector<Node> neighNodes = getNeighborNodes(curNode);
		if(this.curNode.direction!=Node.None) {
			for(Node node:neighNodes) {
				if(this.curNode.calcDirection(node) == this.curNode.direction && !nextNodes.contains(node)) {
					nextNodes.add(node);
					node.direction = this.curNode.direction;
					this.nexNode = node;
					return true;
				}
			}
		}
		int compromise = this.isCompromise();
		
		Node min_node = this.curNode;
		if(compromise!=this.index) {
			int max_mixed_g = this.curNode.g[compromise];
			for(Node nextNode:neighNodes) {
				if(!nextNodes.contains(nextNode) && this.isValid(this.curNode, nextNode) && ((nextNode.g[compromise]) >= max_mixed_g)) {
					if(nextNode.g[compromise] == max_mixed_g && nextNode.g[this.index] < curNode.g[index]) {
						max_mixed_g = nextNode.g[compromise]-nextNode.g[index];
						min_node = nextNode;
					}else if(nextNode.g[compromise] > max_mixed_g){
						max_mixed_g = nextNode.g[compromise]-nextNode.g[index];
						min_node = nextNode;
					}
				}
			}

		}else {
			int min_g = curNode.g[index];
			for(Node node:neighNodes) {
				if(!nextNodes.contains(node) && this.isValid(this.curNode, node) && node.g[index] < min_g) {
					min_g = node.g[index];
					min_node = node;
				}
			}
		}
		
		if(min_node.equals(this.curNode)) {
			if(min_node.calcDirection(min_node) == min_node.direction) {
				nextNodes.add(min_node);
				this.nexNode = min_node;
				min_node.direction = this.curNode.calcDirection(min_node);
				curNode.direction = min_node.direction;
				return true;
			}else {
				this.nexNode = curNode;
				return false;
			}
		}
		nextNodes.add(min_node);
		min_node.direction = this.curNode.calcDirection(min_node);
		curNode.direction = min_node.direction;
		this.nexNode = min_node;
		return true;
		
	}
	public Node oneStephelper() {
		/**
		 * @deprecated
		 */
		Vector<Node> neighNodes = getNeighborNodes(curNode);
		int min_index = -1;
		int min_g = curNode.g[index];
		for(int i = 0;i<neighNodes.size();i++) {
			
			if((!Map.obstacleSet.contains(neighNodes.get(i).position))&&neighNodes.get(i).g[index]<min_g) {
				min_g = neighNodes.get(i).g[index];
				min_index = i;
			}
		}
		if(min_index == -1) {
			this.nexNode = this.curNode;			
			return this.nexNode;
		}
		Node nextNode = neighNodes.get(min_index);
		this.nexNode = nextNode;
		return this.nexNode;
		
	}
	
	
	public byte oneStep() {
		/**
		 * @deprecated
		 */
			Node nextNode = this.nexNode;

			byte step;
			if(nextNode.x > curNode.x) {
				step = 4;
			}else if (nextNode.x < curNode.x) {
				step = 3;
			}else if (nextNode.y > curNode.y) {
				step = 1;
			}else if (nextNode.y < curNode.y) {
				step = 2;
			}else {
				step = 0;
			}
			
			return step;
			
			
	
	}
	
	public Vector<Node> getPotentialObstacles(Node n){
		/**
		 * @deprecated
		 */
		Vector<Node> resNodes = new Vector<Node>();
		int[] X = {-2,-1,-1,-1,0,0,0,0,1,1,1,2};
		int[] Y = {0,1,0,-1,2,1,-1,-2,1,0,-1,0};
		int x = n.x;
		int y = n.y;
		for(int i = 0;i<X.length;i++) {
			Position position = new Position(x+X[i], y+Y[i]);
			if(isValidPoint(position)) {
				if(Map.totalNodeHashMap.containsKey(position)) {
					if(Map.obstacleSet.contains(position)) {
						Map.obstacleSet.add(Map.totalNodeHashMap.get(position).position);
					}
					resNodes.add(Map.totalNodeHashMap.get(position));
				}else {
					Node n1Node = new Node(x+X[i], y+Y[i],nagent);
					resNodes.add(n1Node);
					
				}
				
			}
		}
		return resNodes;
	}


	
	public Vector<Node> getNeighborNodes(Node n){
		/**
		 * @description: auxiliary function for the compute shortest path
		 */
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
	private boolean isValidPoint(Position p) {
		//return p.x >=this.instance.xmin && p.x <=this.instance.xmax && p.y >=this.instance.ymin && p.y <= this.instance.ymax && !obstacleSet.contains(p);
		return !Map.obstacleSet.contains(p);
	}
	
	@Override
	public int hashCode() {
		return new Integer(this.index).hashCode();
	}
	public String toString() {
		return "Agent: "+index + " " +  curNode.x  + " " + curNode.y;
	}
	
	

}


