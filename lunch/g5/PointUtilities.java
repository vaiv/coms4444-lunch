package lunch.g5;

import lunch.sim.Point;

class PointUtilities {

    public static Point add(Point p1, Point p2) {
        return new Point(p1.x + p2.x, p1.y + p2.y);
    }

    public static Point substract(Point p1, Point p2) {
        return new Point(p1.x - p2.x, p1.y - p2.y);
    }

    public static boolean isInLine(Point A, Point B, Point C) {
        // if AC is vertical
        if (A.x == C.x) return B.x == C.x;
        // if AC is horizontal
        if (A.y == C.y) return B.y == C.y;
        // match the gradients
        return (B.x - A.x) / (C.x - A.x) - (B.y - A.y) / (C.y - A.y) < 0.0001;
    }
}