package jp.co.pattirudon.larng.calculator.group;

import java.util.logging.Logger;

import jp.co.pattirudon.larng.RandomList;
import jp.co.pattirudon.larng.calculator.fixed.FixedSeedPredicator;
import jp.co.pattirudon.larng.calculator.fixed.FixedSeedPredicatorConfig;
import jp.co.pattirudon.larng.random.Xoroshiro;

public class GroupSeedCalculator {
    public long solve(long random) {
        return random - Xoroshiro.XOROSHIRO_CONST;
    }

    public void solve(RandomList list, GroupSeedPredicatorConfig filter, Logger logger) {
        FixedSeedPredicatorConfig config = filter.pokemon;
        FixedSeedPredicator subpredicator = new FixedSeedPredicator(config.numShinyRolls,
                config.ability.outwardAbility, config.ability.numAbilities,
                config.nature, config.gender.outwardGender, config.gender.genderRatio,
                config.getWeightMap());
        GroupSeedPredicator predicator = new GroupSeedPredicator(filter.path, subpredicator);
        for (int i = 0; i < list.randoms.size(); i++) {
            long random = list.randoms.get(i);
            long seed = solve(random);
            if (predicator.test(seed)) {
                logger.info(String.format("%016x", seed));
            }
        }
    }
}
