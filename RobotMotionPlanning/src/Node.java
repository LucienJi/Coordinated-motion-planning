
/**
 * 
 * @description: contains all the information about a node: shortest distance to i agent's target;direction mark
 * we use to use A* method but now it is deprecated,so we don't use the heuristic cost
 * @
 *
 */
public class Node {
	
	public final static byte None=0, Up=1, Down=2, Left=3, Right=4;
	public int x;
	public int y;
	public Position position;  // simple handle for Node, just containt (x,y)
	public byte direction;
	
	public int[] g; // g[i] == shortest distance from i agent's target to this node
	public int[] h; // heuristic cost
	
	
	public Node(int x,int y,int nagent) {
		this.x = x;
		this.y = y;
		this.g = new int[nagent];
		this.h = new int[nagent];
		for(int i = 0;i<nagent;i++) {
			g[i] = Integer.MAX_VALUE/2;
			h[i] = Integer.MAX_VALUE/2;
		}
		
		this.position = new Position(x, y);
		this.direction = None;
		
		
	}
	
	public Node (Position position,int nagent) {
		this.x = position.x;
		this.y = position.y;
		this.g = new int[nagent];
		this.h = new int[nagent];
		for(int i = 0;i<nagent;i++) {
			g[i] = Integer.MAX_VALUE/2;
			h[i] = Integer.MAX_VALUE/2;
		}
		
		this.position = new Position(x, y);
		this.direction = None;
		
		
	}
	
	public void setCost(int g,int h,int idx) {
		if(g>=Integer.MAX_VALUE/2) {
			g = Integer.MAX_VALUE/2;
		}
		this.g[idx] = g;
		this.h[idx] = h;
	}
	
	
	public byte calcDirection(Node targetNode) {
		/**
		 * @description: if agent move from this node to targetNode, it will have the direction calculated
		 */
		
		byte direction = 0;
		int x = targetNode.x;
		int y = targetNode.y;
		if(x>this.x) {
			direction = Right;
		}else if (x<this.x) {
			direction = Left;
		}else if (y>this.y) {
			direction = Up;
		}else if (y<this.y) {
			direction = Down;
		}else {
			direction = None;
		}
		return direction;
	}
	
	
	@Override
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(!(o instanceof Node)) {
			return false;
		}
		Node test = (Node) o;
		return test.x == this.x && test.y==this.y;}
	
	public String toString() {
		String reString = "Node_x: "+this.x + " Node_y: "+this.y;
		return reString;
	}
	@Override
	public int hashCode() {
		return new Integer(this.x).hashCode() * 1000 + new Integer(this.y).hashCode();
	}
	
}
