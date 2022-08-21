package jp.co.pattirudon.larng.pokemon;

import java.util.Arrays;

public class Pokemon {
    public final int ec, tid, pid;
    public final int[] ivs;
    public final int ability;
    public final Gender.Internal gender;
    public final Nature nature;
    public final int height, weight;

    public Pokemon(int ec, int tid, int pid, int[] ivs, int ability, Gender.Internal gender,
            Nature nature, int height, int weight) {
        this.ec = ec;
        this.tid = tid;
        this.pid = pid;
        this.ivs = ivs;
        this.ability = ability;
        this.gender = gender;
        this.nature = nature;
        this.height = height;
        this.weight = weight;
    }

    public boolean isShiny() {
        int s = (pid >>> 16) ^ (pid & 0xffff) ^ (tid >>> 16) ^ (tid & 0xffff);
        return s < 0x10;
    }

    @Override
    public String toString() {
        String f = String.join(", ", "ec=%08x", "tid=%08x", "pid=%08x",
                "shiny=%b", "ivs=%s", "ability=%d", "gender=%s", "nature=%s", "height=%d",
                "weight=%d");
        String ivString = Arrays.toString(ivs);
        String g = String.format(f, ec, tid, pid, isShiny(), ivString, ability, gender, nature,
                height, weight);
        String h = this.getClass().getSimpleName() + "[" + g + "]";
        return h;
    }

    public Gender getOutwardGender() {
        return this.gender.gender;
    }

}
