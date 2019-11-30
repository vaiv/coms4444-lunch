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

    public static Point normalizedSubtract(Point p1, Point p2, Double mvmt){
        Double dx = p2.x - p1.x;
        Double dy = p2.y - p1.y;
        Double magnitude = Math.max(mvmt, Math.hypot(dx, dy));

        // Log.log("p1 = "+p1.toString() + " p2 = "+p2.toString()+ " dx="+dx+" dy="+dy+" magnitude="+magnitude);
        return new Point(dx/magnitude, dy/magnitude);
    }
}