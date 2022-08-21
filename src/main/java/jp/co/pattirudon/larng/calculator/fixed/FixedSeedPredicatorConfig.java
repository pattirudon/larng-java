package jp.co.pattirudon.larng.calculator.fixed;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jp.co.pattirudon.larng.pokemon.Gender;
import jp.co.pattirudon.larng.pokemon.Nature;
import jp.co.pattirudon.larng.pokemon.Gender.Ratio;

public class FixedSeedPredicatorConfig {
    public int numShinyRolls;
    public IV ivs;
    public Nature nature;
    public AbilityConfig ability;
    public GenderConfig gender;
    public List<BodyMeasurement> bodyMeasurements;

    public void setNumShinyRolls(@JsonProperty int numShinyRolls) {
        if (numShinyRolls < 1)
            throw new IllegalArgumentException("numShinyRolls must not be less than 1");
        this.numShinyRolls = numShinyRolls;
    }

    @JsonCreator
    public FixedSeedPredicatorConfig(@JsonProperty(value = "numShinyRolls", required = true) int numShinyRolls,
            @JsonProperty(value = "ivs", required = true) IV ivs,
            @JsonProperty(value = "nature", required = true) Nature nature,
            @JsonProperty(value = "ability", required = true) AbilityConfig ability,
            @JsonProperty(value = "gender", required = true) GenderConfig gender,
            @JsonProperty(value = "bodyMeasurements", required = true) List<BodyMeasurement> bodyMeasurements) {
        setNumShinyRolls(numShinyRolls);
        this.ivs = ivs;
        this.nature = nature;
        this.ability = ability;
        this.gender = gender;
        this.bodyMeasurements = bodyMeasurements;
    }

    public Map<Integer, Set<Integer>> getWeightMap() {
        Map<Integer, Set<Integer>> weightMap = new TreeMap<>();
        for (BodyMeasurement m : bodyMeasurements) {
            if (!weightMap.containsKey(m.height))
                weightMap.put(m.height, new TreeSet<>());
            weightMap.get(m.height).add(m.weight);
        }
        return weightMap;
    }

    public static class IV {
        public int h, a, b, c, d, s;

        @JsonCreator
        public IV(@JsonProperty(value = "h", required = true) int h,
                @JsonProperty(value = "a", required = true) int a,
                @JsonProperty(value = "b", required = true) int b,
                @JsonProperty(value = "c", required = true) int c,
                @JsonProperty(value = "d", required = true) int d,
                @JsonProperty(value = "s", required = true) int s) {
            setH(h);
            setA(a);
            setB(b);
            setC(c);
            setD(d);
            setS(s);
        }

        public void setH(@JsonProperty int h) {
            if (!(0 <= h && h <= 31))
                throw new IllegalArgumentException("h must lie in [1, 31]");
            this.h = h;
        }

        public void setA(@JsonProperty int a) {
            if (!(0 <= a && a <= 31))
                throw new IllegalArgumentException("a must lie in [1, 31]");
            this.a = a;
        }

        public void setB(@JsonProperty int b) {
            if (!(0 <= b && b <= 31))
                throw new IllegalArgumentException("b must lie in [1, 31]");
            this.b = b;
        }

        public void setC(@JsonProperty int c) {
            if (!(0 <= c && c <= 31))
                throw new IllegalArgumentException("c must lie in [1, 31]");
            this.c = c;
        }

        public void setD(@JsonProperty int d) {
            if (!(0 <= d && d <= 31))
                throw new IllegalArgumentException("d must lie in [1, 31]");
            this.d = d;
        }

        public void setS(@JsonProperty int s) {
            if (!(0 <= s && s <= 31))
                throw new IllegalArgumentException("s must lie in [1, 31]");
            this.s = s;
        }

    }

    public static class AbilityConfig {
        public int outwardAbility;
        public int numAbilities;

        @JsonCreator
        public AbilityConfig(@JsonProperty(value = "outwardAbility", required = true) int outwardAbility,
                @JsonProperty(value = "numAbilities", required = true) int numAbilities) {
            if (!(1 <= outwardAbility && outwardAbility <= numAbilities && numAbilities <= 2))
                throw new IllegalArgumentException("1 <= outwardAbility <= numAbilities <= 2 must hold");
            this.outwardAbility = outwardAbility;
            this.numAbilities = numAbilities;
        }
    }

    public static class GenderConfig {
        public Gender outwardGender;
        public Gender.Ratio genderRatio;

        @JsonCreator
        public GenderConfig(@JsonProperty(value = "outwardGender", required = true) Gender outwardGender,
                @JsonProperty(value = "genderRatio", required = true) Gender.Ratio genderRatio) {
            if (outwardGender != Gender.Female && genderRatio == Ratio.Female)
                throw new IllegalArgumentException();
            else if (outwardGender != Gender.Male && genderRatio == Ratio.Male)
                throw new IllegalArgumentException();
            else if (outwardGender != Gender.Unknown && genderRatio == Ratio.Genderless)
                throw new IllegalArgumentException();
            this.outwardGender = outwardGender;
            this.genderRatio = genderRatio;
        }
    }

    public static class BodyMeasurement {
        public int height;
        public int weight;

        @JsonCreator
        public BodyMeasurement(@JsonProperty(value = "height", required = true) int height,
                @JsonProperty(value = "weight", required = true) int weight) {
            setHeight(height);
            setWeight(weight);
        }

        public void setHeight(@JsonProperty int height) {
            if (!(0 <= height && height <= 255))
                throw new IllegalArgumentException("height must lie in [0, 255]");
            this.height = height;
        }

        public void setWeight(@JsonProperty int weight) {
            if (!(0 <= weight && weight <= 255))
                throw new IllegalArgumentException("weight must lie in [0, 255]");
            this.weight = weight;
        }

    }
}
