package lunch.g5;

import lunch.sim.Point;

class PointUtilities {

    public static Point add(Point p1, Point p2) {
        return new Point(p1.x + p2.x, p1.y + p2.y);
    }

    public static Point substract(Point p1, Point p2) {
        return new Point(p1.x - p2.x, p1.y - p2.y);
    }
}