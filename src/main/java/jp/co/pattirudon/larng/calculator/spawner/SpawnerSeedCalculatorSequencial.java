package jp.co.pattirudon.larng.calculator.spawner;

import java.util.ArrayList;
import java.util.List;

import jp.co.pattirudon.larng.random.Xoroshiro;

/**
 * Xoroshiro が2回目の nextLong() で返す 64 bit 整数 {@code random} が与えられたときに、
 * Xoroshiro のシードを計算するクラス。 {@code seed} と Xoroshiro constant に1回だけ
 * 遷移行列を作用して得られる内部状態を {@code x0}, {@code x1} とするとき、 {@code x0} は
 * {@code x1} の値により完全に決まる。具体的には {@code x0} は {@code x1} にアフィン変換
 * を施したものであり、この変換は8ビットごとの直和への作用に分解できる。これを利用して {@code
 * x0 + x1 == random} を満たすような組 {@code x0}, {@code x1} を探す。
 */
public class SpawnerSeedCalculatorSequencial implements SpawnerSeedCalculator {
    /**
     * {@code x0} の8ビットおきのスライスを切り出すためのマスク。第 {@code i} 番目のマスクは
     * 下から第 {@code i + 3 + 8 * n} 番目のビットが1である。
     */
    long[] x0SliceMasks = new long[8];

    /**
     * {@code x1} の8ビットおきのスライスを切り出すためのマスク。第 {@code i} 番目のマスクは
     * 下から第 {@code i + 8 * n} 番目のビットが1である。
     */
    long[] x1SliceMasks = new long[8];

    /**
     * {@code x0} のコンポーネントを切り出すためのマスク。第 {@code i} 番目のマスクは
     * 第 {@code 0} 番目から 第 {@code i} 番目までの {@code x0SliceMasks} の論理和である。
     */
    long[] x0CompMasks = new long[8];

    /**
     * {@code x1} のコンポーネントを切り出すためのマスク。第 {@code i} 番目のマスクは
     * 第 {@code 0} 番目から 第 {@code i} 番目までの {@code x1SliceMasks} の論理和である。
     */
    long[] x1CompMasks = new long[8];

    /**
     * 番号付けられた {@code x0} の8ビットおきのスライス。
     */
    long[][] x0Slices = new long[8][1 << 8];

    /**
     * 番号付けられた {@code x1} の8ビットおきのスライス。
     */
    long[][] x1Slices = new long[8][1 << 8];

    public SpawnerSeedCalculatorSequencial() {
        long[] e = new long[8];
        for (int i = 0; i < e.length; i++) {
            e[i] = 1L << (i * 8);
        }
        long[] every8s = new long[256];
        for (int i = 0; i < every8s.length; i++) {
            for (int j = 0; j < e.length; j++) {
                if (((i >>> j) & 1) == 1) {
                    every8s[i] ^= e[j];
                }
            }
        }

        long f;
        f = 0x01010101_01010101L;
        for (int i = 0; i < 8; i++) {
            x1SliceMasks[i] = f;
            x0SliceMasks[i] = Long.rotateLeft(f, 3);
            f = f << 1;
        }

        f = 0x01010101_01010101L;
        for (int i = 0; i < 8; i++) {
            x1CompMasks[i] = f;
            x0CompMasks[i] = Long.rotateLeft(f, 3);
            f = f | (f << 1);
        }

        for (int i = 0; i < 8; i++) {
            for (int k = 0; k < 256; k++) {
                long x1Comp = Long.rotateLeft(every8s[k], i);
                long x0Comp = SpawnerSeedCalculatorSequencial.x0(x1Comp) & x0SliceMasks[i];
                x0Slices[i][k] = x0Comp;
                x1Slices[i][k] = x1Comp;
            }
        }
    }

    /**
     * 2番目に {@code random} を返すような Xoroshiro のシードの集合を返す。
     * @param random
     * @return
     */
    public List<Long> seeds(long random) {
        List<Long> seeds = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                for (int k = 0; k < 256; k++) {
                    long x0Comp2 = x0Slices[0][i] | x0Slices[1][j] | x0Slices[2][k];
                    long x1Comp2 = x1Slices[0][i] | x1Slices[1][j] | x1Slices[2][k];
                    List<Long> possibleX1SliceList = possibleX1SliceListAt3(x0Comp2,
                            x1Comp2, random);
                    for (long x1Slice3 : possibleX1SliceList) {
                        long x0Slice3 = x0(x1Slice3) & x0SliceMasks[3];
                        long x0Comp3 = x0Comp2 | x0Slice3, x1Comp3 = x1Comp2 | x1Slice3;
                        long x1Slice4 = (random - x0Comp3 - x1Comp3)
                                & (0x01010101_01010101L << 4);
                        long x0Slice4 = x0(x1Slice4) & x0SliceMasks[4];
                        long x0Comp4 = x0Comp3 | x0Slice4, x1Comp4 = x1Comp3 | x1Slice4;
                        long x1Slice567 = (random - x0Comp4 - x1Comp4)
                                & (0x01010101_01010101L * 0b11100000);
                        long x0Slice567 = x0(x1Slice567) & (0x01010101_01010101L * 0b00000111);
                        long x0 = x0Comp4 | x0Slice567, x1 = x1Comp4 | x1Slice567;
                        if (x0 + x1 == random) {
                            long seed = seed(x1);
                            seeds.add(seed);
                        }
                    }
                }
            }
        }
        return seeds;
    }

    /**
     * {@code x0} と {@code x1} の第2コンポーネントと 64 ビット乱数値が与えられたとき、
     * {@code x1} の第3スライスの候補のリストを返す。
     * @param x0Comp
     * @param x1Comp
     * @param random
     * @return
     */
    public static List<Long> possibleX1SliceListAt3(long x0Comp, long x1Comp, long random) {
        int at = 3;
        long a = (random - x0Comp - x1Comp) & (0x01010101_01010101L << at);
        long b = (random - x0Comp - x1Comp - 0x01010101_01010101L * 0b11000000)
                & (0x01010101_01010101L << at);
        long diff = a ^ b;
        int[] notEq = new int[8];
        for (int i = 0; i < notEq.length; i++) {
            notEq[i] = (int) (diff >>> (i * 8 + at)) & 1;
        }
        List<Long> results = new ArrayList<>();
        long[] part = new long[8];
        for (int i0 = 0; i0 <= notEq[0]; i0++) {
            part[0] = i0 == 1 ? (1L << at) : 0L;
            for (int i1 = 0; i1 <= notEq[1]; i1++) {
                part[1] = part[0] | (i1 == 1 ? (1L << (at + 8)) : 0L);
                for (int i2 = 0; i2 <= notEq[2]; i2++) {
                    part[2] = part[1] | (i2 == 1 ? (1L << (at + 16)) : 0L);
                    for (int i3 = 0; i3 <= notEq[3]; i3++) {
                        part[3] = part[2] | (i3 == 1 ? (1L << (at + 24)) : 0L);
                        for (int i4 = 0; i4 <= notEq[4]; i4++) {
                            part[4] = part[3] | (i4 == 1 ? (1L << (at + 32)) : 0L);
                            for (int i5 = 0; i5 <= notEq[5]; i5++) {
                                part[5] = part[4] | (i5 == 1 ? (1L << (at + 40)) : 0L);
                                for (int i6 = 0; i6 <= notEq[6]; i6++) {
                                    part[6] = part[5] | (i6 == 1 ? (1L << (at + 48)) : 0L);
                                    for (int i7 = 0; i7 <= notEq[7]; i7++) {
                                        part[7] = part[6] | (i7 == 1 ? (1L << (at + 56)) : 0L);
                                        results.add(a ^ part[7]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    /**
     * $L^{24}\mathrm{const} = x_0  +  \left(1 + U^{16}\right)L^{27}x_1 + L^{51}x_1$
     * @param x1
     * @return
     */
    public static long x0(long x1) {
        x1 = Long.rotateLeft(x1, 27);
        long x0 = (Long.rotateLeft(Xoroshiro.XOROSHIRO_CONST, 24))
                ^ x1 ^ (x1 << 16) ^ Long.rotateLeft(x1, 24);
        return x0;
    }

    /**
     * $x_1 = L^{37}(\mathrm{seed} + \mathrm{const})$
     * @param x1
     * @return
     */
    public static long seed(long x1) {
        return Long.rotateRight(x1, 37) ^ Xoroshiro.XOROSHIRO_CONST;
    }
}
