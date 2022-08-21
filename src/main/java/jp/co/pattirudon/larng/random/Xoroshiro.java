package jp.co.pattirudon.larng.random;

public class Xoroshiro implements Cloneable {
    public static final long XOROSHIRO_CONST = 0x82a2b175_229d6a5bL;

    public long x0, x1;

    public Xoroshiro(long x0, long x1) {
        this.x0 = x0;
        this.x1 = x1;
    }

    public Xoroshiro(long seed) {
        this(seed, XOROSHIRO_CONST);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (x0 ^ (x0 >>> 32));
        result = prime * result + (int) (x1 ^ (x1 >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Xoroshiro other = (Xoroshiro) obj;
        if (x0 != other.x0)
            return false;
        if (x1 != other.x1)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s [%16x, %16x]", this.getClass().getName(), x0, x1);
    }

    public long nextLong() {
        long s0 = x0;
        long s1 = x1;
        long result = s0 + s1;
        s1 ^= s0;
        x0 = Long.rotateLeft(s0, 24) ^ s1 ^ (s1 << 16);
        x1 = Long.rotateLeft(s1, 37);
        return result;
    }

    /**
     * デフォルトだと {@code nextLong} の上32ビットを返すので，
     * 下32ビットを返すよう書き換えておく．
     */
    public int nextInt() {
        return (int) nextLong();
    }

    public int nextInt(int bound) {
        int boundInclusive = bound - 1;
        int mask = getMask(boundInclusive);
        int result = nextInt() & mask;
        while (result >= bound) {
            result = nextInt() & mask;
        }
        return result;
    }

    public int nextInt(int origin, int bound) {
        return origin + nextInt(bound - origin);
    }

    public static final int getMask(int x) {
        for (int i = 0; i < 4; i++) {
            x |= x >>> (1 << i);
        }
        return x;
    }

    @Override
    public Xoroshiro clone() {
        Xoroshiro copy = null;
        try {
            copy = (Xoroshiro) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return copy;
    }

}
