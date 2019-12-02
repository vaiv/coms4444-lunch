package lunch.g5;

import lunch.sim.Point;

public class PositionStruct {
    Point location;
    Point movement;
    Integer numSteps;

    public PositionStruct(Point a, Point b, Integer c){
        this.location = a;
        this.movement = b;
        this.numSteps = c;
    }
}