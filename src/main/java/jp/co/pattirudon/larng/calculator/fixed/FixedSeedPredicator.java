package jp.co.pattirudon.larng.calculator.fixed;

import java.util.Map;
import java.util.Set;
import java.util.function.LongPredicate;

import jp.co.pattirudon.larng.Sampler;
import jp.co.pattirudon.larng.pokemon.Gender;
import jp.co.pattirudon.larng.pokemon.Gender.Ratio;
import jp.co.pattirudon.larng.pokemon.Nature;
import jp.co.pattirudon.larng.pokemon.Pokemon;
import jp.co.pattirudon.larng.random.Xoroshiro;

public class FixedSeedPredicator implements LongPredicate {

    public int numShinyRolls;
    public final int numGuaranteedIVs = 0;
    public int ability;
    public int numAbilities;
    public Nature nature;
    public Gender gender;
    public Gender.Ratio genderRatio;
    public Map<Integer, Set<Integer>> weightMap;

    public FixedSeedPredicator(int numShinyRolls, int ability, int numAbilities, Nature nature, Gender gender,
            Ratio genderRatio,
            Map<Integer, Set<Integer>> weightMap) {
        this.numShinyRolls = numShinyRolls;
        this.ability = ability;
        this.numAbilities = numAbilities;
        this.nature = nature;
        this.gender = gender;
        this.genderRatio = genderRatio;
        this.weightMap = weightMap;
    }

    @Override
    public boolean test(long seed) {
        Xoroshiro random = new Xoroshiro(seed);
        Pokemon pokemon = Sampler.getPokemon(random, numShinyRolls, numGuaranteedIVs, numAbilities, genderRatio);
        if (pokemon.ability != this.ability)
            return false;
        else if (pokemon.nature != this.nature)
            return false;
        else if (pokemon.getOutwardGender() != this.gender)
            return false;
        return (this.weightMap.containsKey(pokemon.height))
                && (this.weightMap.get(pokemon.height).contains(pokemon.weight));
    }

}
