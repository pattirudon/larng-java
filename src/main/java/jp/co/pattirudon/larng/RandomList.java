package jp.co.pattirudon.larng;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RandomList {
    public List<Long> randoms;

    @JsonProperty(value = "randoms")
    public void setRandoms(List<String> hexStrings) {
        randoms = new ArrayList<>();
        for (int i = 0; i < hexStrings.size(); i++) {
            String s = hexStrings.get(i);
            long random = Long.parseUnsignedLong(s, 16);
            randoms.add(random);
        }
    }
}
