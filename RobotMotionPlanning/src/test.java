import java.time.Year;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Vector;

public class test {
	public final static byte None=0, Up=1, Down=2, Left=3, Right=4;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		HashMap<Integer, Integer>hashMap = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer>hashMap2 = new HashMap<Integer, Integer>();
		
		hashMap.put(1, 1);
		hashMap.put(2,2);
		hashMap.put(3, 3);
		
		hashMap2.put(4, 4);
		hashMap2.put(5, 5);
		
		Set<Integer> set = hashMap.keySet();
		Set<Integer> set2 = hashMap2.keySet();
		
		Set<Integer> set3 = new HashSet<Integer>(set) {
		};
		Set<Integer> set4 = new HashSet<Integer>(set2) {};
		
		
		
		set3.retainAll(set4);
		
		System.out.println(set3.size());
		System.out.println(hashMap.keySet().size());
		System.out.println(hashMap);
		
	
		
		
	}
	


}

class Pos{
	public int x;
	public int y;
	
	public Pos(int x,int y) {
		this.x = x;
		this.y =y;
		
	}
	@Override
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(!(o instanceof Pos)) {
			return false;
		}
		Pos test = (Pos) o;
		return test.x == this.x && test.y==this.y;}
	
	@Override
	public int hashCode() {
		return new Integer(this.x).hashCode() * 1000 + new Integer(this.y).hashCode();
	}
}
