package jp.co.pattirudon.larng.matrices;

import java.util.Arrays;

public class IntRowMatrix {
    public final int rows;
    public final int[] mat;
    public final int columns = 32;

    protected IntRowMatrix(int[] mat) {
        this.rows = mat.length;
        this.mat = mat;
    }

    public static IntRowMatrix getInstance(int[] mat, boolean copy) {
        if (copy) {
            return new IntRowMatrix(Arrays.copyOf(mat, mat.length));
        } else {
            return new IntRowMatrix(mat);
        }
    }

    public static IntRowMatrix getInstance(IntRowMatrix m, boolean copy) {
        return getInstance(m.mat, copy);
    }

    public static IntRowMatrix ones() {
        int[] _mat = new int[32];
        for (int i = 0; i < _mat.length; i++) {
            _mat[i] = 1 << i;
        }
        return IntRowMatrix.getInstance(_mat, false);
    }

    public IntRowMatrix add(IntRowMatrix f) {
        if (this.rows != f.rows) {
            throw new DimensionMismatchException(
                    "The number of rows of this matrix must equal to the number of rows of another matrix.");
        }
        int[] _mat = new int[this.rows];
        for (int i = 0; i < this.rows; i++) {
            _mat[i] = this.mat[i] ^ f.mat[i];
        }
        return IntRowMatrix.getInstance(_mat, false);
    }

    public byte[] multiplyRight(int vector) {
        byte[] result = new byte[rows];
        for (int i = 0; i < rows; i++) {
            int v = mat[i] & vector;
            byte e = popCount(v);
            result[i] = e;
        }
        return result;
    }

    public IntRowMatrix multiplyRight(IntRowMatrix f) {
        return this.binary().multiplyRight(f);
    }

    public int multiplyLeft(byte[] vector) {
        int result = 0;
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
        int ri = mat[i];
        int rj = mat[j];
        mat[i] = rj;
        mat[j] = ri;
    }

    public void addRows(int src, int dst) {
        mat[dst] ^= mat[src];
    }

    public int[] nullbasis() {
        BinaryMatrix g = generalizedInverse();
        IntRowMatrix h = g.multiplyRight(this).add(IntRowMatrix.ones());
        BinaryMatrix t = h.transposed();
        BinaryMatrix.Echelon e = t.echelon();
        return Arrays.copyOf(e.echelon.intRowMatrix().mat, e.rank);
    }

    public int[] nullspace() {
        int[] basis = nullbasis();
        int[] space = new int[1 << basis.length];
        for (int k = 0; k < (1 << basis.length); k++) {
            int v = 0;
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
            int r = this.mat[i];
            for (int j = 0; j < this.columns; j++) {
                _mat[i][j] = (byte) (r & 1);
                r >>>= 1;
            }
        }
        return BinaryMatrix.getInstance(this.rows, this.columns, _mat, false);
    }

    /**
     * unsigned int を2進数表記したときの bit 1 の数を modulo 2 で返す．
     * @param x
     * @return {@code x} の2進数表記の bit 1 の数を 2 で割った余り．
     */
    public static byte popCount(int x) {
        x = x ^ (x >>> 16);
        x = x ^ (x >>> 8);
        x = x ^ (x >>> 4);
        x = x ^ (x >>> 2);
        x = x ^ (x >>> 1);
        return (byte) (x & 1);
    }

}
