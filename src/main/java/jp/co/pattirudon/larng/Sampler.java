package jp.co.pattirudon.larng;

import jp.co.pattirudon.larng.pokemon.Gender;
import jp.co.pattirudon.larng.pokemon.Nature;
import jp.co.pattirudon.larng.pokemon.Pokemon;
import jp.co.pattirudon.larng.random.Xoroshiro;

public class Sampler {
    static int getPid(Xoroshiro random, int tid, int numShinyRolls) {
        int pid = 0;
        for (int i = 0; i < numShinyRolls; i++) {
            pid = random.nextInt();
            int shininess = (pid >>> 16) ^ (pid & 0xffff) ^ (tid >>> 16) ^ (tid & 0xffff);
            if (shininess < 0x10)
                break;
        }
        return pid;
    }

    static void setFlawlessIVs(int[] ivs, Xoroshiro random, int numGuaranteedIVs) {
        if (!(0 <= numGuaranteedIVs && numGuaranteedIVs < ivs.length))
            throw new IllegalArgumentException(
                    "numGuaranteedIVs must be greater than or equal 0 and less than ivs.length");
        for (int i = 0; i < numGuaranteedIVs;) {
            int j = random.nextInt(ivs.length);
            if (ivs[j] < 31) {
                ivs[j] = 31;
                i++;
            }
        }
    }

    static void setIVs(int[] ivs, Xoroshiro random) {
        for (int i = 0; i < ivs.length; i++) {
            if (ivs[i] == 0)
                ivs[i] = random.nextInt(32);
        }
    }

    static int[] getIVs(Xoroshiro random, int numGuaranteedIVs) {
        int[] ivs = new int[6];
        setFlawlessIVs(ivs, random, numGuaranteedIVs);
        setIVs(ivs, random);
        return ivs;
    }

    static int getBodyMeasurement(Xoroshiro random) {
        int m = random.nextInt(0x81) + random.nextInt(0x80);
        return m;
    }

    /**
     * 特性の決まり方:
     * <pre>
     * | rand | single ability | double abilities |
     * | ---- | -------------- | ---------------- |
     * | 0    | 1              | 2                |
     * | 1    | 1              | 1                |
     * </pre>
     * 
     * @param random
     * @param numShinyRolls
     * @param numGuaranteedIVs
     * @param numAbilities
     * @param genderRatio
     * @return
     */
    public static Pokemon getPokemon(Xoroshiro random, int numShinyRolls, int numGuaranteedIVs,
            int numAbilities, Gender.Ratio genderRatio) {
        int ec = random.nextInt();
        int tid = random.nextInt();
        int pid = getPid(random, tid, numShinyRolls);
        int[] ivs = getIVs(random, numGuaranteedIVs);
        int ability = 2 - (random.nextInt(2) | (2 - numAbilities));
        Gender.Internal gender;
        if (genderRatio == Gender.Ratio.Female || genderRatio == Gender.Ratio.Male
                || genderRatio == Gender.Ratio.Genderless)
            gender = Gender.Internal.of(genderRatio);
        else {
            int value = random.nextInt(252) + 1;
            gender = Gender.Internal.of(value, genderRatio);
        }
        Nature nature = Nature.valueOf(random.nextInt(25));
        int height = getBodyMeasurement(random);
        int weight = getBodyMeasurement(random);
        return new Pokemon(ec, tid, pid, ivs, ability, gender, nature, height, weight);
    }
}
