package jp.co.pattirudon.larng.calculator.group;

import java.util.function.LongPredicate;

import jp.co.pattirudon.larng.calculator.fixed.FixedSeedPredicator;
import jp.co.pattirudon.larng.random.Xoroshiro;

public class GroupSeedPredicator implements LongPredicate {
    public final int[] path;
    public final FixedSeedPredicator subpredicator;

    public GroupSeedPredicator(int[] path, FixedSeedPredicator subpredicator) {
        this.path = path;
        this.subpredicator = subpredicator;
    }

    @Override
    public boolean test(long seed) {
        /*
         * [0] -> group
         * [1] -> spawner
         * [2] -> fixed
         */
        Xoroshiro[] rngs = { new Xoroshiro(seed), null, null };
        for (int i = 0; i < path.length; i++) {
            int numPokeOnPhase = path[i];
            for (int j = 0; j < numPokeOnPhase; j++) {
                rngs[1] = new Xoroshiro(rngs[0].nextLong());
                rngs[0].nextLong();
                rngs[1].nextLong();
                rngs[2] = new Xoroshiro(rngs[1].nextLong());
            }
            rngs[0] = new Xoroshiro(rngs[0].nextLong());
        }
        return subpredicator.test(rngs[2].x0);
    }

}
