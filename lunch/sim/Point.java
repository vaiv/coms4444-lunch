package lunch.sim;
import java.io.Serializable;
public class Point implements Serializable {
    public double x;
    public double y;

    private static Double eps = 1e-7;
    private static Double min_x = -50.0, max_x=50.0, min_y=-50.0, max_y=50.0;

    public static boolean within_bounds(Point a)
    {
        return (a.x <=max_x + eps && a.x>=min_x - eps && a.y>=min_y - eps && a.y<=max_y + eps);
    }

    public static double dist(Point a, Point b)
    {
        return Math.sqrt(Math.pow(a.x-b.x,2)+Math.pow(a.y-b.y,2));
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point a)
    {
        this.x = a.x;
        this.y = a.y;
    }

    public String toString() 
    { 
        return "(" + x + ", " + y + ")"; 
    } 

    @Override
    public boolean equals(Object other) {
    	if(!(other instanceof Point)) return false;
    	Point o = (Point) other;
    	return x == o.x && y == o.y;
    }

    @Override
    public int hashCode() {
    	return new Integer((int) (x * 10000 + y)).hashCode();
    }
}