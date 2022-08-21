package jp.co.pattirudon.larng.calculator.group;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jp.co.pattirudon.larng.calculator.fixed.FixedSeedPredicatorConfig;

public class GroupSeedPredicatorConfig {
    int[] path;
    FixedSeedPredicatorConfig pokemon;

    @JsonCreator
    public GroupSeedPredicatorConfig(@JsonProperty(value = "path", required = true) int[] path,
            @JsonProperty(value = "pokemon", required = true) FixedSeedPredicatorConfig pokemon) {
        this.path = path;
        this.pokemon = pokemon;
    }
}
