package jp.co.pattirudon.larng.pokemon;

import java.util.OptionalInt;

/**
 * https://bulbapedia.bulbagarden.net/wiki/Personality_value
 */
public enum Gender {
    Male, Female, Unknown;

    public Gender of(int value, Ratio ratio) {
        if (!(1 <= value && value <= 253))
            throw new IllegalArgumentException("value must lie between 1 and 253");
        if (ratio == Ratio.Genderless)
            throw new IllegalArgumentException("the genderless cannot have internal gender value");
        else if (value < ratio.threshold)
            return Female;
        else
            return Male;
    }

    public static class Internal {
        public final Gender gender;
        public final Ratio ratio;
        public final OptionalInt value;

        public Internal(Gender gender, Ratio ratio, OptionalInt value) {
            this.gender = gender;
            this.ratio = ratio;
            this.value = value;
        }

        public static Internal of(int value, Ratio ratio) {
            if (!(1 <= value && value <= 253))
                throw new IllegalArgumentException("value must lie between 1 and 253");
            if (ratio == Ratio.Female || ratio == Ratio.Male || ratio == Ratio.Genderless)
                throw new IllegalArgumentException("the following gender types cannot have internal gender value: "
                        + "females only, males only, genderless");
            else if (value < ratio.threshold)
                return new Internal(Female, ratio, OptionalInt.of(value));
            else
                return new Internal(Male, ratio, OptionalInt.of(value));
        }

        public static Internal of(Ratio ratio) {
            if (ratio == Ratio.Female)
                return new Internal(Female, ratio, OptionalInt.empty());
            else if (ratio == Ratio.Male)
                return new Internal(Male, ratio, OptionalInt.empty());
            else if (ratio == Ratio.Genderless)
                return new Internal(Unknown, ratio, OptionalInt.empty());
            else
                throw new IllegalArgumentException("given gender type requires gender value");
        }

        @Override
        public String toString() {
            return "Gender.Internal [gender=" + gender + ", ratio=" + ratio + ", value=" + value + "]";
        }

    }

    public enum Ratio {
        Male(0),
        OneFemaleOutOfEight(31),
        OneFemaleOutOfFour(63),
        Even(127),
        ThreeFemaleOutOfFour(191),
        SevenFemaleOutOfEight(225),
        Female(254),
        Genderless(255);

        public final int threshold;

        private Ratio(int threshold) {
            this.threshold = threshold;
        }
    }
}
