
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;
/**
 * 
 * @author jijingtian yuyunhao
 * @description: optimization 1 : Rewind2Stabilize
 *
 */

public class AugementSolution {
	public int agentNum; 
	public int totalTime;
	public HashMap<Node,Cell> nodeHistory; // Node-> Cell: history of a node, record the index of the agent who has visited the node
	public Vector<Vector<Node>> statehistory; // stateHistory[k].stateNodes.get(i) at k step, ith agent's current node
	
	public AugementSolution(Deque<State> trajectory) {
		/**
		 * @description: constructor
		 */
		agentNum = trajectory.getFirst().stateNodes.size(); 
		totalTime = trajectory.size(); 
		
		System.out.println("Nums:" +agentNum);
		System.out.println("Makespan: "+ totalTime);
		
		nodeHistory = new HashMap<Node,Cell>();
		statehistory = new Vector<Vector<Node>>(); 
		int t = 0; 
		for (State s : trajectory) {
			statehistory.add(s.stateNodes);
			
			for (int i = 0; i < s.size(); i++) {
				Node tmpNode = s.get(i);				
				if(!nodeHistory.containsKey(tmpNode)) {
					nodeHistory.put(tmpNode, new Cell(tmpNode.position));
					nodeHistory.get(tmpNode).add(i,t);}
				else {
					nodeHistory.get(tmpNode).add(i,t);}
			}
			t++;
		}
		
	}
	
	
	/**
	 * 
	 * @param t: find a pattern before step t(included)，traverse the every node->cell to check whether there is an agent at the node 
	 * @return res[0] agent's index, res[1] last time when this agent was in the node,res[2] the second time when this agent was in the node
	 */
	public int[] findPattern(int t) {
		int[] res = new int[3];
		for(int i=0;i<agentNum;i++) {
			Cell tmp = nodeHistory.get(this.statehistory.get(t).get(i)); 
			Pair robotOld = tmp.find(t);
			if(robotOld!=null && robotOld.agentindex==i && robotOld.t+1<t) {
				res[0]=robotOld.agentindex;
				res[1]=robotOld.t;
				res[2]=t;
				return res;
			}
		}
		
		return null;
	}
	
	
	/**
	 * 
	 * @param agentindex: the agent to be modified
	 * @param lastT, we will change the trajectory from ]lastT, T[
	 * @param T
	 */
	public void update(int agentindex,int lastT,int T) {
		assert lastT+1<T : "间隔时长需要大于1！lastT = "+lastT+" T = "+T;
		Node target = statehistory.get(T).get(agentindex); // 
		for(int t=lastT+1;t<T;t++){
			
			Node originalNode = statehistory.get(t).get(agentindex); //Find where is the agent
			Cell originalCorrespondantCell = this.nodeHistory.get(originalNode); // get the cell of this node
			originalCorrespondantCell.remove(agentindex, t); // agent should not appear in node at step t
			this.statehistory.get(t).set(agentindex, target); // agent should remain at the target node
			Cell cellShouldbe = this.nodeHistory.get(target); // change the target node's history
			cellShouldbe.add(agentindex, t);


			
		}
	}
	
	/**
	 * 
	 * @param end = 1,we update from the last step to the end we set, usually 1.
	 * this loop will end when there is no agent can be updated
	 */
	public void augment(int end){
		int t=totalTime-1;
		while(t>end) {
			int[] res = new int[3];
			do {
				res = findPattern(t);
				if(res!=null) {
					update(res[0],res[1],res[2]);
				}
			}while(res!=null);
			t--;
		}
	}
	
	
	/**
	 * 
	 * @return the optimized trajectory 
	 */
	public Deque<State> getSolution(){
		augment(1);
		Deque<State> res = new LinkedList<State>();
		for(int i=0;i<statehistory.size();i++) {
			State tmpState = new State(this.statehistory.get(i));
			while (res.contains(tmpState)) {
				res.pollLast();
			}
			res.addLast(tmpState);
		}
		
	
		return res;
	}
	
	/**
	 * @deprecated
	 * @param statesqDeque: trajectory
	 * @return delete the cycle in the trajectory
	 */
	public Deque<State> compress(Deque<State> statesqDeque){
				HashMap<Integer, LinkedList<Node>> agenthistory  = new HashMap<Integer, LinkedList<Node>>();
		HashMap<Integer, LinkedList<Node>> deletedhistory = new HashMap<Integer, LinkedList<Node>>();
		for(int i = 0;i<this.agentNum;i++) {
			agenthistory.put(i, new LinkedList<Node>());
			deletedhistory.put(i, new LinkedList<Node>());
		}
		for(State state:statesqDeque) {
			Vector<Node> nodes = state.stateNodes;
			for(int i = 0;i<this.agentNum;i++) {
				agenthistory.get(i).add(nodes.get(i));
			}
		}
		HashMap<Integer, LinkedList<Integer[]>> repetitionHashMap  = this.findRepetition(agenthistory);
		for(LinkedList<Integer[]> list:repetitionHashMap.values()) {
			if(list.size() == 0) {
				return statesqDeque;
			}
		}

		return null;
	

	}
	
	/**
	 * @deprecated
	 * @param agenthistory
	 * @return
	 */
	private HashMap<Integer, LinkedList<Integer[]>> findRepetition(HashMap<Integer, LinkedList<Node>> agenthistory){
		HashMap<Integer, LinkedList<Integer[]>> resHashMap = new HashMap<Integer, LinkedList<Integer[]>>();
		for(int i = 0;i<this.agentNum;i++) {
			resHashMap.put(i, new LinkedList<Integer[]>());
		}
		
		for(int i = 0;i<this.agentNum;i++) {
			LinkedList<Node> historyLinkedList = agenthistory.get(i);
			int Tmax = historyLinkedList.size()-1; // last t stamp
			int Tmin = historyLinkedList.size()-2; // last-1 t stamp
			while(Tmax >=0 && Tmin >=0) {
				if(historyLinkedList.get(Tmax).equals(historyLinkedList.get(Tmin))) {
					// at least we get 1 pair repetition
					while (historyLinkedList.get(Tmax).equals(historyLinkedList.get(Tmin))) {
							Tmin--;
					}
					Integer[] resIntegers = new Integer[2];
					resIntegers[0] = Tmax;
					resIntegers[1] = Tmin + 1;
					resHashMap.get(i).add(resIntegers);
					Tmax = Tmin;
					Tmin -- ;
				}else {
					//go to next pair
					Tmax = Tmin;
					Tmin -- ;
				}
			}
		}
		return resHashMap;
		
		
	}

}

/**
 * 
 * @author jijingtian yuyunhao
 * @description: to track the history of a node
 */
class Cell{
	public HashMap<Integer, Integer> history; // int -> int : step -> agent index
	public Position position;
	public Cell(Position position) {
		this.history=new HashMap<Integer, Integer>();
		this.position = position;
	}
	
	/**
	 * 
	 * @param t ：find the the agent who came at this node before step t
	 * @return: pair: (step t ,agent'index)
	 */
	// 找到最后一个小于t时刻的agent编号
	public Pair find(int t) {
		int time = -1;
		int agentindex = -1;
		int max_t = Collections.max(history.keySet());
		if(t>max_t) {
			return null;
		}
		if(history.keySet().size() <= 1) {
			return null;
		}
		
		ArrayList<Integer> KeySelectionManager = new ArrayList<Integer>(history.keySet());
		Collections.sort(KeySelectionManager);
		for(int timestamp:KeySelectionManager) {
			if(timestamp < t ) {
				time = timestamp;
				agentindex = this.history.get(timestamp);
			}else {
				if(time == -1) {
					return null;
				}
				return new Pair(time, agentindex);
				
				}
		}
		if(time == -1) {
			return null;
		}else if (time < t) {
			return new Pair(time, agentindex);
		}else {
			return null;
		}
	
			
	}
	
	public void add(int agentindex,int t) {
		this.history.put(t,agentindex);
	}
	
	public void remove(int agentindex,int t) {
			history.remove(t);
	}
	
	@Override
	public String toString() {
		return this.history.toString();
	}
	public boolean equals(Object o) {
			if(o == this) {
				return true;
			}
			if(!(o instanceof Cell)) {
				return false;
			}
			Cell test = (Cell) o;
			return test.position.x == this.position.x && test.position.y==this.position.y;}

}

class Pair{
	int t;
	int agentindex;
	
	public Pair(int t,int agentindex) {
		this.t = t;
		this.agentindex = agentindex;
	}
	
	
}