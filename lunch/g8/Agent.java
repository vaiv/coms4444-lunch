package lunch.g8;

import java.util.LinkedList;
import lunch.sim.Point;

/**
 *
 * @author group8
 * @param <T> type of agent
 */
public abstract class Agent<T> {

    protected final LinkedList<T> history;
    protected T lastest;

    public Agent(T original) {
        history = new LinkedList<>();
        history.offer(original);
        lastest = original;
    }

    public void update(T original) {
        lastest = original;
        history.addFirst(lastest);
        if (history.size() > 3) {
            history.removeLast();
        }
    }

    public abstract Point getLocation();

}
