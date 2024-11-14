package dk.martinersej.plugin.mine.environment;

import dk.martinersej.plugin.mine.Mine;

public abstract class Environment {

    protected final Mine mine;
    protected final int priority;
    protected boolean finished = false;

    public Environment(Mine mine, int priority) {
        this.mine = mine;
        this.priority = priority;
    }

    public void reset() {
        finished = false;
    }

    public abstract double getProgress();

    public abstract String serialize();

    public void deserialize(String data) {
        // Deserialize the data
    }
}
