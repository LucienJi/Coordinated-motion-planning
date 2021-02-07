import java.awt.List;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.sampled.LineListener;

/**
 * A class defining a solution to the coordinated (robot) motion planning problem. <br>
 * 
 * The parallel-motion steps of all robots are stored in an ArrayList<byte[]>, whose 'k-th' element is an array of size 'n'.<br>
 * 
 * @author Luca Castelli Aleardi (INF421, Ecole Polytechnique, nov 2020)
 *
 */
public class Solution {
	/** Possible robot movements into Western, Eastern, Northern or Southern direction. (FIXED means that the robot is not moving) */
	
	/** 唉，E 是往左，W 是往右 */
	public final static byte FIXED=0, N=1, S=2, E=4, W=3;

	/** Name of the input instance */
	public String name;
	public HashMap<Integer, Position> curHashMap;
	public HashMap<Integer, Position> nextHashMap;
	public Coordinates startCoordinates;
	public Coordinates targetCoordinates;
	private Instance input;
	
	/** 
	 * A dynamic array storing all motion parallel steps: 'k-th' element is an array of size 'n' representing
	 * the displacement of all robots at time 'k'. <br>
	 * Remark: if the 'i-th' is not moving at time 'k' then the corresponding entry in the array is FIXED (equal to 0)
	 **/
	public ArrayList<byte[]> steps;
	
	/**
	 * Initialize the motion of robots (no steps at the beginning)
	 */
	public Solution(String name) {
		this.name=name;
		this.steps=new ArrayList<byte[]>(); // empty list of steps at the beginning
		
		this.curHashMap = new HashMap<Integer, Position>();
		this.nextHashMap = new HashMap<Integer, Position>();
		
	}
	
	/**
	 * Add a new step to the current solution
	 */
	public void addStep(byte[] mov) {
		if(mov!=null)
			this.steps.add(mov);
	}

	/**
	 * The makespan is just the number of parallel steps (e.g. the time until all robots have reached their destinations).
	 * 
	 * @return the makespan
	 */
	public int makespan() {
		return this.steps.size();
	}
	
	/**
	 * Compute and return the total distance traveled by all robots (until they have all reached their destinations).
	 * @return the total traveled distance of all robots (in all steps)
	 */
	public int getTotalDistance() {
		int result=0;
		for(byte[] moves: this.steps) {
			for(int i=0;i<moves.length;i++)
				if(moves[i]!=Solution.FIXED)
					result++;
		}
		return result;
	}
	
	/**
	 * Check whether the solution describe a valid trajectory for all robots and at any step.
	 * 
	 * @return TRUE is all motion parallel steps are valid (according to the rules of the problem)
	 */
	public void getInstance(Instance input) {
		this.input = input;
	}
	public boolean isValid() {
		
		this.startCoordinates = input.starts;
		this.targetCoordinates = input.targets;
		int[][] starts = this.startCoordinates.getPositions();
		int[][] targets = this.targetCoordinates.getPositions();
		int n = this.startCoordinates.n;
		int x,y;
		for (int i = 0;i<n;i++) {
			curHashMap.put(i, new Position(starts[0][i], starts[1][i]));
		}
		if(curHashMap.size() < n) {
			return false;
		}
		
		int span = this.makespan();
		byte move;
		Position curPosition;
		Position nextPosition;
		for(int i = 0;i<span;i++) {
			for(int j = 0;j<n;j++) {
				// i : span
				// j : index of agent
				move = this.steps.get(i)[j];
				curPosition = this.curHashMap.get(j);
				nextPosition = this.agentmove(curPosition, move);
				if(this.nextHashMap.containsValue(nextPosition)) {
					System.out.println("Current: " + curPosition);
					System.out.println(("Move: " + move));
					System.out.println("Next: " + nextPosition);
					return false;
				}
				this.nextHashMap.put(j, nextPosition);
				if(this.curHashMap.containsValue(nextPosition)) {
					ArrayList<Integer> KeyList = this.getKey(nextPosition);
					if(KeyList.size()>1) {
						return false;
					}
					int otheragent_index = KeyList.get(0);
					if((move!=this.steps.get(i)[otheragent_index])) {

						return false;
					}
				}
			}
			for(int p = 0;p<n;p++) {
				curHashMap.replace(p, nextHashMap.get(p));
			}
			
			nextHashMap.clear();
			
		}
		
		return true;
		
		//throw new Error("TO BE COMPLETED");
	}
	private ArrayList<Integer> getKey(Position position) {
		ArrayList<Integer> keyArrayList = new ArrayList<Integer>();
		for(int k:this.curHashMap.keySet()) {
			if(this.curHashMap.get(k).equals(position)) {
				keyArrayList.add(k);
			}
		}
		return keyArrayList;
	}
	private Position agentmove(Position cur, byte move) {
		int x = cur.x;
		int y = cur.y;
		if(move == 1) {
			y++;
		}else if (move == 2) {
			y--;
		}else if(move==3) {
			x--;
		}else  if(move == 4){
			x++;
		}
		Position nextPosition = new Position(x, y);
		return nextPosition;
	}
	public String toString() {
		String result="Solution to the input instance: "+this.name+"\n";
		result=result+"\tnumber of steps (makespan): "+this.makespan()+"\n";
		result=result+"\ttotal distance (total number of robot moves): "+this.getTotalDistance();
		return result;
	}
	
}
