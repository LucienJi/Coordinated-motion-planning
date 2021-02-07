import java.util.Vector;


public class MyPriorityQueue {
	
	public Vector<Node> vector;
	private int index;
	
	
	public MyPriorityQueue(int index) {
		this.vector = new Vector<Node>();
		this.index = index;
		
	}
	
	public void siftDown(int currentIdx, int endIdx, Vector<Node> vector) {
	    // Write your code here.
			int leftchild = 2*currentIdx+1;
			int next;
			while(leftchild <= endIdx){
				int rightchild = 2*currentIdx + 2 <= endIdx ? 2*currentIdx + 2:-1;
				if(rightchild!=-1 && vector.get(rightchild).g[index]<vector.get(leftchild).g[index]){
						next = rightchild;
				}else{
						next = leftchild;
				}
				if(vector.get(currentIdx).g[index] > vector.get(next).g[index]){
					Node tmpNode = vector.get(currentIdx);
					vector.set(currentIdx, vector.get(next));
					vector.set(next, tmpNode);
					currentIdx = next;
					leftchild = 2*currentIdx+1;
				}else{
					return;
				}
				
			}
			
	  }
	
	public void siftUp(int currentIdx, Vector<Node> heap) {
	    // Write your code here.
			int parent = (currentIdx-1)/2;
			while(currentIdx > 0 && heap.get(currentIdx).g[index] < heap.get(parent).g[index]){
				
				Node tmpNode = heap.get(currentIdx);
				heap.set(currentIdx, heap.get(parent));
				heap.set(parent, tmpNode);
				currentIdx = parent;
				parent = (currentIdx - 1)/2;
			}
	  }
	
	public Node poll() {
		int i = 0;
		int j = vector.size()-1;
		Node tmpNode = this.vector.get(i);
		this.vector.set(i, this.vector.get(j));
		this.vector.set(j, tmpNode);
		this.vector.remove(j);
		this.siftDown(0, this.vector.size()-1,this.vector);
		return tmpNode;

	}
	
	public void add(Node node) {
		this.vector.add(node);
		this.siftUp(this.vector.size()-1, this.vector);
		
	}
	
	public void remove(Node node) {
		this.vector.remove(node);
		Vector<Node> newvectNodes = this.buildup(this.vector);
		this.vector = newvectNodes;
	}
	
	public void clear() {
		this.vector.clear();
	}
	public int size() {
		return this.vector.size();
	}
	public boolean contains(Node node) {
		return this.vector.contains(node);
	}
	
	public Vector<Node> buildup(Vector<Node>nodes){
		int firstparent = (nodes.size()-2)/2;
		for(int cur = firstparent;cur>=0;cur--) {
			this.siftDown(cur, nodes.size()-1, nodes);
		}
		return nodes;
	}
	


}
