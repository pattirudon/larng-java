package jp.co.pattirudon.larng.calculator.fixed;

import java.util.function.IntSupplier;

import jp.co.pattirudon.larng.matrices.BinaryMatrix;
import jp.co.pattirudon.larng.matrices.DoubleIndexedLongRowMatrix;
import jp.co.pattirudon.larng.matrices.LongRowMatrix;
import jp.co.pattirudon.larng.random.Xoroshiro;

public class FixedSeedCalculationFromDuplexIV {
    /**
     * PID生成最大数
     */
    public int numShinyRolls;

    /**
     * 右からシード0を掛けると表と裏の個体値を表すビット列を得る行列。
     * サイズは 60 行 64 列。
     */
    public final LongRowMatrix ivMat;

    /**
     * 左から表と裏の個体値を掛けるとシード0を得る行列。
     * サイズは 60 行 64 列。
     * {@code ivMat * seedMat.transposed() * ivMat == ivMat} が成り立つ。
     */
    public final DoubleIndexedLongRowMatrix seedMat;

    /**
     * {@code x0 = 0}, {@code x1 = Xoroshiro.XOROSHIRO_CONST} で初期化される
     * Xoroshiro が生成する表と裏の個体値を保持する配列。
     */
    public int[] ivConst;

    /**
     * {@code ivMat} の右カーネル。
     */
    public final long[] nullspace;

    public FixedSeedCalculationFromDuplexIV(int numShinyRolls) {
        this.numShinyRolls = numShinyRolls;
        ivMat = ivMatrix(); // (60, 64)
        nullspace = ivMat.nullspace();
        BinaryMatrix g = ivMat.generalizedInverse(); // (64, 60)
        LongRowMatrix gt = g.transposed().longRowMatrix(); // (60, 64)
        seedMat = DoubleIndexedLongRowMatrix.getInstance(12, 5, gt); // (60, 64)
        ivConst = ivConstant();
    }

    public long[] seeds(int... ivOmoteUra) {
        int[] ivDiff = new int[12];
        for (int i = 0; i < ivDiff.length; i++) {
            ivDiff[i] = ivConst[i] ^ ivOmoteUra[i];
        }
        long seed = seedMat.multiplyLeft(ivDiff);
        long[] seeds = new long[nullspace.length];
        for (int i = 0; i < seeds.length; i++) {
            seeds[i] = seed ^ nullspace[i];
        }
        return seeds;
    }

    public LongRowMatrix ivMatrix() {
        int n = 12;
        byte[][] mat = new byte[64][5 * n];
        for (int i = 0; i < 64; i++) {
            long seed = 1L << i;
            IntSupplier f = new IvSupplier(seed, 0L);
            for (int j = 0; j < n; j++) {
                int iv = f.getAsInt();
                for (int k = 0; k < 5; k++) {
                    mat[i][j * 5 + k] = (byte) ((iv >>> k) & 1);
                }
            }
        }
        boolean copy = false;
        BinaryMatrix b = BinaryMatrix.getInstance(64, 5 * n, mat, copy);
        return b.transposed().longRowMatrix();
    }

    public int[] ivConstant () {
        int[] ivConst = new int[12];
        IntSupplier f = new IvSupplier(0L, Xoroshiro.XOROSHIRO_CONST);
        for (int i = 0; i < ivConst.length; i++) {
            ivConst[i] = f.getAsInt();
        }
        return ivConst;
    }

    protected class IvSupplier implements IntSupplier {
        private final Xoroshiro random;
        private int i = 0;

        protected IvSupplier(long x0, long x1) {
            random = new Xoroshiro(x0, x1);
            random.nextInt(); // ec
            random.nextInt(); // tid
            for (int j = 0; j < numShinyRolls; j++) {
                random.nextInt(); // pid
            }
        }

        @Override
        public int getAsInt() {
            int iv;
            if ((i & 1) == 0)
                iv = (int) random.x0 & 0x1f;
            else {
                iv = (int) random.x1 & 0x1f;
                random.nextInt();
            }
            i++;
            return iv;
        }
    }
}
