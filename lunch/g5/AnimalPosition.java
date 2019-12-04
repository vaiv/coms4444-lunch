package lunch.g5;

import java.util.ArrayList;
import java.util.Iterator;

import javafx.util.Pair;
import lunch.sim.Animal;
import lunch.sim.AnimalType;
import lunch.sim.Point;

public class AnimalPosition extends ArrayList<PositionStruct> {

    private static final long serialVersionUID = 1423662030262649775L;

    // Utility function to create AnimalPosition
    public static AnimalPosition parse(final ArrayList<Animal> curAnimals, final ArrayList<Animal> prvAnimals,
            final Boolean onlyMonkeys) {
        AnimalPosition animalLocs = new AnimalPosition();

        for (Integer i = 0; i < curAnimals.size(); i++) {
            if (onlyMonkeys && curAnimals.get(i).which_animal() != AnimalType.MONKEY)
                continue;

            final Point cur = curAnimals.get(i).get_location();
            final Point prv = prvAnimals.get(i).get_location();

            Point dxs = PointUtilities.substract(cur, prv);
            if (cur.x + dxs.x < -50 || cur.x + dxs.x > 50 || cur.y + dxs.y < -50 || cur.y + dxs.y > 50)
                dxs = null;

            animalLocs.add(new PositionStruct(cur, dxs, 0));
        }

        return animalLocs;
    }

    @Override
    public String toString() {
        final Iterator<PositionStruct> it = this.iterator();
        if (!it.hasNext())
            return "[]";

        final StringBuilder str = new StringBuilder();
        for (PositionStruct e = it.next(); it.hasNext(); e = it.next()) {
            final Point loc = e.location;
            str.append((loc == null) ? "[null]" : String.format("[%.3f, %.3f]", loc.x, loc.y));

            final Point dxs = e.movement;
            str.append((dxs == null) ? "(null)" : String.format("(%.3f, %.3f)", dxs.x, dxs.y));

            str.append(String.format("%d ", e.numSteps));
        }

        return str.toString();
    }
};
