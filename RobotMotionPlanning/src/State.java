import java.util.Vector;

import javax.naming.InitialContext;

public class State {
	
	int g;
	int h;
	Vector<Node> stateNodes;
	
	public State(Vector<Node> vector) {
		// TODO Auto-generated constructor stub
		
		stateNodes = new Vector<Node>();
		for(Node i:vector) {
			stateNodes.add(i);
		}
		
		g = Integer.MAX_VALUE/2;
		
	}
	
	int size() {
		return this.stateNodes.size();
	}
	
	Node get(int i) {
		return this.stateNodes.get(i);
	}
	
	void setCost(int g,int h ) {
		if(g>Integer.MAX_VALUE/2) {
			this.g = Integer.MAX_VALUE/2;
			this.h = h;
			return ;
		}else {
			this.g = g;
			this.h = h;
			return;
			
		}
	}
	
	
	@Override
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(!(o instanceof State)) {
			return false;
		}
		State test = (State) o;
		
		if(test.stateNodes.size()!=this.stateNodes.size()) {
			return false;
		}
		
		boolean res = true;
		for(int i =0;i<test.stateNodes.size();i++) {
			Node  n1Node = test.stateNodes.get(i);
			Node  n2Node = this.stateNodes.get(i);
			res = res && (n1Node.x == n2Node.x) && (n1Node.y == n2Node.y);
		}
		return res;
		
		}
	public String toString() {
		String reString = "";
		for(Node i:this.stateNodes) {
			reString+=i.toString() + " ";
		}
		return reString;
		
	}
		
	
	@Override
	public int hashCode() {
		int res = 0;
		for(int i = 0;i<this.stateNodes.size();i++) {
			res += this.stateNodes.get(i).x *Math.pow(10, i) + this.stateNodes.get(i).y *Math.pow(10, i-1);
		}
		return new Integer(res).hashCode();
	}
	
	

}
