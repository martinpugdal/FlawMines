package dk.martinersej.plugin.mine.environment;

import dk.martinersej.plugin.mine.Mine;

public class DestroyedEnvironment extends Environment {

    // blocks
    private float ratio; // The ratio of blocks that should be destroyed (defined by %, 0-100, where we convert it to 0-1)
    private int blocksNeeded; // The amount of blocks needed to be destroyed
    private int blocksDestroyed = 0; // The amount of blocks destroyed

    public DestroyedEnvironment(Mine mine, int priority, int ratio) {
        super(mine, priority);

        if (ratio < 0 || ratio > 100) {
            throw new IllegalArgumentException("The ratio must be between 0 and 100");
        }
        this.ratio = ratio / 100f; // Convert the ratio to 0-1
        this.blocksNeeded = Math.round(mine.getTotalBlocks() * ratio);
    }

    @Override
    public void reset() {
        super.reset();
        // Reset the environment
        blocksDestroyed = 0;
    }

    public void onMineResize() {
        blocksNeeded = Math.round(mine.getTotalBlocks() * ratio);
    }

    @Override
    public double getProgress() {
        double progress = (double) blocksDestroyed / blocksNeeded;
        // convert the progress to percentage
        return Math.min(progress, 0) * 100; // Return the progress as percentage
    }

    public void increaseBlocksDestroyed() {
        blocksDestroyed++;
        if (blocksDestroyed >= mine.getRegion().getArea()) {
            finished = true;
        }
    }
}
