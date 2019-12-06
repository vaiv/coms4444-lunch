package lunch.g5;

import lunch.sim.PlayerState;
import lunch.sim.Point;

public class Corner implements Comparable<Corner> {
	Point p;
	int fm;
	int monkey;
	Point player;
	
	public Corner (Point p, int fm, int monkey, Point player) {
		this.p = p;
		this.fm = fm;
		this.monkey = monkey;
		this.player = player;
	}
	
	@Override
	public int compareTo(Corner other) {
		if(this.fm > other.fm) return 1;
		if(this.fm < other.fm) return -1;
		if(Point.dist(player, this.p) > Point.dist(other.player, other.p)) return 1;
		if(Point.dist(player, this.p) < Point.dist(other.player, other.p)) return 1;
//		if(this.monkey > other.monkey) return 1;
//		if(this.monkey < other.monkey) return -1;
		return 0;
	}
}