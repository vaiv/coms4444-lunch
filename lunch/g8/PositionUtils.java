package lunch.g8;

import lunch.sim.Point;

/**
 *
 * @author group8
 */
public class PositionUtils {

    public static final Double MIN_X = -50.0, MAX_X = 50.0, MIN_Y = -50.0, MAX_Y = 50.0;

    public static Point moveTowards(Point currentPos, Point destPos) {
        Double angle = getDirection(currentPos, destPos);
        if (angle == null) {
            return destPos;
        }
        return moveInDirection(currentPos, angle);
    }

    public static Point moveInDirection(Point point, double direction) {
        return moveInDirection(point, direction, 1);
    }

    public static Point moveInDirection(Point point, double direction, double distance) {
        return new Point(point.x + Math.cos(direction) * distance, point.y + Math.sin(direction) * distance);
    }

    /**
     * Determines the angle based on the origin point and destination point
     *
     * @param o origin
     * @param d destination
     * @return the angle or null if the two points are the same
     */
    public static Double getDirection(Point o, Point d) {
        if (distance(o, d) < 1e-10) {
            return null;
        }
        return Math.atan2(d.y - o.y, d.x - o.x);
    }

    public static double distance(Point p1, Point p2) {
        return Math.hypot(p1.x - p2.x, p1.y - p2.y);
    }
}
