package jp.co.pattirudon.larng.pokemon;

public enum Nature {
    Hardy(0), Lonely(1), Brave(2), Adamant(3), Naughty(4), Bold(5), Docile(6), Relaxed(7), Impish(8), Lax(9), Timid(10),
    Hasty(11), Serious(12), Jolly(13), Naive(14), Modest(15), Mild(16), Quiet(17), Bashful(18), Rash(19), Calm(20),
    Gentle(21), Sassy(22), Careful(23), Quirky(24);

    private final int id;

    Nature(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static Nature valueOf(int id) {
        return Nature.values()[id];
    }
}
