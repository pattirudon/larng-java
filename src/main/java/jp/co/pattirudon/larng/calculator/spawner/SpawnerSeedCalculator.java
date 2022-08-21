package jp.co.pattirudon.larng.calculator.spawner;

import java.util.List;

public interface SpawnerSeedCalculator {
    public List<Long> seeds(long random);
}
