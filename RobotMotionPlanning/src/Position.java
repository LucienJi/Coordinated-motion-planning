import java.util.Objects;

public class Position {
	public int x;
	public int y;
	public Position(int x,int y) {
		this.x = x;
		this.y = y;
	}
	@Override
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(!(o instanceof Position)) {
			return false;
		}
		Position test = (Position) o;
		return test.x == this.x && test.y==this.y;}
	
	@Override
	public int hashCode() {
		return new Integer(this.x).hashCode() * 1000 + new Integer(this.y).hashCode();
	}
	public String toString() {
		String reString = "x: "+this.x + " y: "+this.y;
		return reString;
	}
}
