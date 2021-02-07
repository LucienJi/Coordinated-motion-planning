import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;

/**
 * 
 * @author jijingtian yuyunhao
 * @description: Rewind2Parallelize
 */

public class AugmentSolution2 {
	public ArrayList<Vector<Node>> originalNodes; // original trajectory
	public ArrayList<ActiveGroup> activeGroups; // activeGroups[t] =  the active agent set at step t
	public int nagent;
	
	/**
	 * @description: constructor,initialize the active groups
	 * @param states: original trajectory
	 */
	public AugmentSolution2(Deque<State> states) {
		this.originalNodes = new ArrayList<Vector<Node>>();
		this.activeGroups = new ArrayList<ActiveGroup>();
		for(State state:states) {
			this.originalNodes.add(state.stateNodes);
		}
		this.nagent = this.originalNodes.get(0).size();
		for(int i = 0;i<this.originalNodes.size()-1;i++) {
			Vector<Node> curNodes = this.originalNodes.get(i);
			Vector<Node> nexNodes = this.originalNodes.get(i+1);
			
			ActiveGroup activeGroup = new ActiveGroup(i);
			for(int j = 0;j<curNodes.size();j++) {
				if(!curNodes.get(j).equals(nexNodes.get(j))) {
					// active
					activeGroup.add(j, curNodes.get(j),nexNodes.get(j));
				}
			}
			this.activeGroups.add(activeGroup);
		}
	}
	
	/**
	 * 
	 * @return the optimized solution
	 */
	public Deque<State> getSolution(){
		this.Update2();
		System.out.println("Update Finished");
		Deque<State> solutionDeque = new LinkedList<State>();
		for(int i = 0;i<this.originalNodes.size();i++) {
			State state = new State(this.originalNodes.get(i));
			while(solutionDeque.contains(state)) {
				solutionDeque.pollLast();
			}
			solutionDeque.addLast(new State(this.originalNodes.get(i)));
		}
		return solutionDeque;
	}
	
	/**
	 * @description: the update loop will end if no agent can be updated.
	 */
	private void Update2() {
		boolean ischange = true;
		while(ischange) {
			ischange = false;
			for(int i = 0;i<this.nagent;i++) {
				for(int t = 0;t<this.activeGroups.size()-1;t++) {
					if(this.activeGroups.get(t).contain(i) && !this.activeGroups.get(t+1).contain(i)) {
						// agent i is active at step t and inactive at step t+1,we try to optimize this agent
						ischange = ischange || this.updatehelper2(i, t);
						
					}
				}
			}			
			for(int i = this.nagent-1;i>=0;i--) {
				// we reverse the order, because the order of optimization matters
				for(int t = 0;t<this.activeGroups.size()-1;t++) {
					if(this.activeGroups.get(t).contain(i) && !this.activeGroups.get(t+1).contain(i)) {
						ischange = ischange || this.updatehelper2(i, t);
						
					}
				}
			}

		}
	}
	
	/**
	 * 
	 * @param index:agent index
	 * @param start_time : the active time
	 * @return true = update successfully , false can't be updated
	 */
	private boolean updatehelper2(int index, int start_time) {
		
		int to_be_active_time = start_time + 1;
		int next_active_time = this.find_next_active_time(index, start_time); // agent is inactive between to_be_active_time and next_active_time
		if(next_active_time == start_time) {
			// start_time is the last active time
			return false;
		}
		Node to_be_check_Node = this.activeGroups.get(next_active_time).nextNodes.get(index); // perhaps this agent can move to the to_be_check_node before next_active_time 
		Node node = this.activeGroups.get(next_active_time).currentNodes.get(index);
		int T = next_active_time;
		boolean res = true;
		while(T>= to_be_active_time+1 && res) {
			res = res && !this.originalNodes.get(T).contains(to_be_check_Node); // we want to check whether the to_be_check_node is occupied by other agent
			if(res) {
				T--;
			}
		}
		T++;
		if(T >= next_active_time) {
			return false;
		}else {
			/**
			 * the agent can move before next_active_time
			 * we change the active group and the trajectory
			 */
			this.activeGroups.get(next_active_time).remove(index);
			this.activeGroups.get(T).add(index, node, to_be_check_Node);
			for(int t = T+1;t<=next_active_time;t++) {
				this.originalNodes.get(t).set(index, to_be_check_Node);
			}
		}
		return true;
		
	}
	private int find_next_active_time(int index,int start_time) {
		// suppose agent is active start time
		for(int t = start_time + 1;t < this.activeGroups.size();t++) {
			if(this.activeGroups.get(t).contain(index)) {
				return t;
			}
		}
		return start_time;
	}
	

}


/**
 * 
 * @author jijingtian yuyunhao
 * @description: contain the active agents set at step t, we track their current node and the next node
 */

class ActiveGroup{
	
	int timestamp;
	int nums = 0;
	HashMap<Integer, Node> currentNodes;
	HashMap<Integer, Node> nextNodes;
	
	public ActiveGroup(int time) {
		this.timestamp = time;
		this.currentNodes = new HashMap<Integer, Node>();
		this.nextNodes = new HashMap<Integer, Node>();
		
	}
	
	public boolean contain(int index) {
		return this.currentNodes.containsKey(index);
	}
	
	public void add(int index, Node curNode,Node nexNode) {
		this.currentNodes.put(index, curNode);
		this.nextNodes.put(index, nexNode);
		this.nums = this.currentNodes.keySet().size();
	}
	
	public void remove(int index) {
		this.currentNodes.remove(index);
		this.nextNodes.remove(index);
		this.nums = this.currentNodes.keySet().size();
	}
	
	public Set<Integer> getSet() {
		
		Set<Integer> set = new HashSet<Integer>(this.currentNodes.keySet());
		return set;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(!(o instanceof ActiveGroup)) {
			return false;
		}
		ActiveGroup test = (ActiveGroup) o;
		return test.timestamp == this.timestamp;
		}
	
	public int hashCode() {
		return new Integer(this.timestamp).hashCode();
	}
	
	
	
}