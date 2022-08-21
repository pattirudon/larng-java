package jp.co.pattirudon.larng.matrices;

import java.util.Arrays;

public class LongRowMatrix {
    public final int rows;
    public final long[] mat;
    public final int columns = 64;

    protected LongRowMatrix(long[] mat) {
        this.rows = mat.length;
        this.mat = mat;
    }

    public static LongRowMatrix getInstance(long[] mat, boolean copy) {
        if (copy) {
            return new LongRowMatrix(Arrays.copyOf(mat, mat.length));
        } else {
            return new LongRowMatrix(mat);
        }
    }

    public static LongRowMatrix getInstance(LongRowMatrix m, boolean copy) {
        return getInstance(m.mat, copy);
    }

    public static LongRowMatrix ones() {
        long[] _mat = new long[64];
        for (int i = 0; i < _mat.length; i++) {
            _mat[i] = 1L << i;
        }
        return LongRowMatrix.getInstance(_mat, false);
    }

    public LongRowMatrix add(LongRowMatrix f) {
        if (this.rows != f.rows) {
            throw new DimensionMismatchException(
                    "The number of rows of this matrix must equal to the number of rows of another matrix.");
        }
        long[] _mat = new long[this.rows];
        for (int i = 0; i < this.rows; i++) {
            _mat[i] = this.mat[i] ^ f.mat[i];
        }
        return LongRowMatrix.getInstance(_mat, false);
    }

    public byte[] multiplyRight(long vector) {
        byte[] result = new byte[rows];
        for (int i = 0; i < rows; i++) {
            long v = mat[i] & vector;
            byte e = popCount(v);
            result[i] = e;
        }
        return result;
    }

    public LongRowMatrix multiplyRight(LongRowMatrix f) {
        return this.binary().multiplyRight(f);
    }

    public long multiplyLeft(byte[] vector) {
        long result = 0L;
        for (int i = 0; i < rows; i++) {
            if (vector[i] == (byte) 1) {
                result ^= mat[i];
            }
        }
        return result;
    }

    public BinaryMatrix transposed() {
        return binary().transposed();
    }

    public BinaryMatrix generalizedInverse(){
        return binary().generalizedInverse();
    }

    public void swapRows(int i, int j) {
        long ri = mat[i];
        long rj = mat[j];
        mat[i] = rj;
        mat[j] = ri;
    }

    public void addRows(int src, int dst) {
        mat[dst] ^= mat[src];
    }

    public long[] nullbasis() {
        BinaryMatrix g = generalizedInverse();
        LongRowMatrix h = g.multiplyRight(this).add(LongRowMatrix.ones());
        BinaryMatrix t = h.transposed();
        BinaryMatrix.Echelon e = t.echelon();
        return Arrays.copyOf(e.echelon.longRowMatrix().mat, e.rank);
    }

    public long[] nullspace() {
        long[] basis = nullbasis();
        long[] space = new long[1 << basis.length];
        for (int k = 0; k < (1 << basis.length); k++) {
            long v = 0;
            for (int l = 0; l < basis.length; l++) {
                if (((k >>> l) & 1) == 1) {
                    v ^= basis[l];
                }
            }
            space[k] = v;
        }
        return space;
    }

    private BinaryMatrix binary() {
        byte[][] _mat = new byte[this.rows][this.columns];
        for (int i = 0; i < this.rows; i++) {
            long r = this.mat[i];
            for (int j = 0; j < this.columns; j++) {
                _mat[i][j] = (byte) (r & 1);
                r >>>= 1;
            }
        }
        return BinaryMatrix.getInstance(this.rows, this.columns, _mat, false);
    }

    /**
     * unsigned long を2進数表記したときの bit 1 の数を modulo 2 で返す．
     * @param x
     * @return {@code x} の2進数表記の bit 1 の数を 2 で割った余り．
     */
    public static byte popCount(long x) {
        x = x ^ (x >>> 32);
        x = x ^ (x >>> 16);
        x = x ^ (x >>> 8);
        x = x ^ (x >>> 4);
        x = x ^ (x >>> 2);
        x = x ^ (x >>> 1);
        return (byte) (x & 1);
    }

}
