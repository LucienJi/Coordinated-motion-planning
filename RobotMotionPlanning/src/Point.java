
public class Point {
	public int x;
	public int y;
	public double g;
	public double f;
	public double h; 
	
	public Point(int x,int y) {
		this.x = x;
		this.y = y;
		
		this.g = 99;
		this.h = 99;
		this.f = g + h;
	}
	
	public void setCost(double tentative_cost,Point target) {
		g = tentative_cost;
		h = Math.abs(target.x-x) + Math.abs(target.y - y);
		f = g + h;
	}
	public void setH(Point tartPoint) {
		this.h = Math.abs(tartPoint.x-x) + Math.abs(tartPoint.y - y);
		this.f = this.h + this.g;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(!(o instanceof Point)) {
			return false;
		}
		Point test = (Point) o;
		return test.x == this.x && test.y==this.y;
	}
	
	
		

}
