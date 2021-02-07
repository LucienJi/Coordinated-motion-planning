import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.Vector;

import javax.security.auth.x500.X500Principal;

import processing.opengl.Texture;
/**
 * @deprecated
 * @author jijingtian yuyunhao
 * @description: pure BFS to find the optimal trajectory but only can be used for very very small map
 */
public class Global_Planning {
	public final static byte None=0, Up=1, Down=2, Left=3, Right=4;

	
	PriorityQueue<State> openset;
	HashMap<State, State> parentHashMap;
	HashSet<State> closeset;
	HashMap<Position, Node> totalSet;
	Instance instance;
	State startState;
	State endState;
	int nagent;
	
	
	
	public Global_Planning(Instance input, Vector<Node> startNodes,Vector<Node> endNodes) {
		// TODO Auto-generated constructor stub
		this.instance = input;
		this.nagent  = input.starts.n;
		this.startState = new State(startNodes);
		this.endState = new State(endNodes);
		this.openset = new PriorityQueue<State>(new StateComparator());
		this.totalSet = Map.totalNodeHashMap;
		this.closeset = new HashSet<State>();
		this.parentHashMap = new HashMap<State, State>();

	}
	
	public void initialization() {
		for(Node i:this.startState.stateNodes) {
			totalSet.put(i.position,i);
		}
		this.startState.setCost(0, calcM(startState, endState));
		this.parentHashMap.put(this.startState, this.startState);
		this.openset.add(this.startState);
		
	}
	
	public void nextStates(State initialState,int pos,Vector<Node> cur, Vector<State> res){
		if(pos == initialState.size()&&cur.size() == initialState.size()) {
			State newState = new State(cur);
			res.add(newState);
			return;
		}
		
		Node movingNode = initialState.get(pos);
		
		Vector<Node> neighborsNodes = this.getPotentialNeighbor(movingNode);
		for(Node i:neighborsNodes) {
			if(this.isValid(movingNode, i) && (!cur.contains(i))) {
				byte original_direction = i.direction;
				byte original_direction2 = movingNode.direction;
				i.direction = movingNode.calcDirection(i);
				//System.out.println(original_direction + " " + i.direction);
				//System.out.println("1: "+i.direction + "2: "+totalSet.get(i.position).direction);
				movingNode.direction = i.direction;
				cur.add(i);
				nextStates(initialState, pos+1, cur, res);
				i.direction = original_direction;
				movingNode.direction = original_direction2;
				
				cur.remove(i);
			}
		}
	
	}
	
	public ArrayList<byte[]> getSteps(Stack<State> planStack){
		ArrayList<byte[]> steps = new ArrayList<byte[]>();
		State tmpState = planStack.pop();
		while(!planStack.empty()) {
			State nextState = planStack.pop();
			steps.add(Global_Planning.getStepshelper(tmpState, nextState));
			tmpState = nextState;
		}
		return steps;
	}
	
	static byte[] getStepshelper(State startState,State nextState) {
		int n = startState.size();
		byte[] step = new byte[n];
		for(int i = 0;i<n;i++) {
			byte move;
			Node n1Node = startState.stateNodes.get(i);
			Node n2Node = nextState.stateNodes.get(i);
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
	
	public boolean plan(Stack<State> planStack) {
		while(openset.size()>0) {
			
			State tmpState = openset.poll();
			
			//System.out.println("Temp: "+ tmpState + " Target: " +this.endState);
			//System.out.println(tmpState.g);
			closeset.add(tmpState);
			if(tmpState.equals(this.endState)) {
				while(!tmpState.equals(startState)) {
					planStack.add(tmpState);
					tmpState = this.parentHashMap.get(tmpState);
				}
				planStack.add(tmpState);
				return true;
			}else {
				Vector<State> nextStates = new Vector<State>();
				Vector<Node> curNodes = new Vector<Node>();
				for(Node i:tmpState.stateNodes) {
					i.direction = None;
				}
				this.nextStates(tmpState, 0, curNodes, nextStates);
				
				for(int i = 0;i<nextStates.size();i++) {
					if(!closeset.contains(i)) {
						boolean better = false;
						int tentative_cost = 1 + tmpState.g;
						if(openset.contains(i)) {
							if(tentative_cost < nextStates.get(i).g) {
								better = true;
							}else {
								better = false;
							}
						}else {
							better = true;
						}
						if(better) {
							nextStates.get(i).g = tentative_cost;
							nextStates.get(i).setCost(tentative_cost, this.calcM(nextStates.get(i), this.endState));
							this.parentHashMap.put(nextStates.get(i), tmpState);
							openset.remove(nextStates.get(i));
							openset.add(nextStates.get(i));
						}
					}
					
				}
				
			}
		}
		return false;
	}
	
	private boolean isValid(Node startNode,Node endNode) {
		if(startNode.direction!=None) {
			if(endNode.direction == None) {
				return startNode.calcDirection(endNode) == startNode.direction;
			}else {
				return (startNode.calcDirection(endNode) == startNode.direction) && (startNode.calcDirection(endNode)==endNode.direction);
			}
		}else {
			if(endNode.direction == None) {
				return true;
			}else {
				return startNode.calcDirection(endNode) == endNode.direction;
			
			
		}
			}
	
	}
	
	private Vector<Node> getPotentialNeighbor(Node node){
		Vector<Node> resNodes = new Vector<Node>();
		if(Map.obstacleSet.contains(node.position)) {
			resNodes.add(node);
			return resNodes;
		}
		int x = node.x;
		int y = node.y;
		Position p1Position = new Position(x+1, y);
		Position p2Position = new Position(x-1, y);
		Position p3Position = new Position(x, y+1);
		Position p4Position = new Position(x, y-1);
		Position[] Positions = {p1Position,p2Position,p3Position,p4Position};
		for(int i =0;i<Positions.length;i++) {
			p1Position = Positions[i];
			if(!isObstacle(p1Position)) {
				if(this.totalSet.containsKey(p1Position)) {
					resNodes.add(this.totalSet.get(p1Position));
				}else {
					Node n1Node = new Node(p1Position,nagent);
					resNodes.add(n1Node);
					totalSet.put(p1Position, n1Node);
				}
			}
			
		}
		resNodes.add(node);
		
		return resNodes;
		
	}
	
	private int calcM(State s1,State s2) {
		int res = 0;
		for (int i= 0;i<s1.stateNodes.size();i++) {
			res+=Math.abs(s1.stateNodes.get(i).x - s2.stateNodes.get(i).x) + Math.abs(s1.stateNodes.get(i).y - s2.stateNodes.get(i).y);
		}
		return res;
	}
	
	private int calcG(State s1,State s2) {
		// 假设 i 位置上就是 i 号agent；
		int res = 0;
		for(int i = 0;i<s1.stateNodes.size();i++) {
			res+= Math.abs(s1.stateNodes.get(i).g[i] - s2.stateNodes.get(i).g[i]);
		}
		return res;
	}
	private boolean isObstacle(Position position) {
		return Map.obstacleSet.contains(position);
	}
	
	
	
	

}

class StateComparator implements Comparator<State>{
	public int compare(State p1,State p2) {
		if(p1.g + p1.h <= p2.g + p2.h) {
			return -1;
		}else {
			return 1;
		}
		
	}
}