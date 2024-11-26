package dk.martinersej.api.worldedit;

import com.sk89q.worldedit.function.pattern.Pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandomPattern extends com.sk89q.worldedit.function.pattern.RandomPattern {

    private final List<Pattern> patterns = new ArrayList<>();
    private final List<Double> chances = new ArrayList<>();

    public RandomPattern() {
        super();
    }

    @Override
    public void add(Pattern pattern, double chance) {
        super.add(pattern, chance);
        patterns.add(pattern);
        chances.add(chance);
    }

    public Map<Pattern, Double> getPatternChances() {
        Map<Pattern, Double> patternChances = new HashMap<>();
        for (int i = 0; i < patterns.size(); i++) {
            patternChances.put(patterns.get(i), chances.get(i));
        }
        return patternChances;
    }
}
