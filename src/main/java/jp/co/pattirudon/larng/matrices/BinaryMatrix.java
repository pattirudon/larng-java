package jp.co.pattirudon.larng.matrices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BinaryMatrix {
    public final int rows;
    public final int columns;
    public final byte[][] mat;

    private BinaryMatrix(int rows, int columns, byte[][] mat) {
        this.rows = rows;
        this.columns = columns;
        this.mat = mat;
    }

    public static BinaryMatrix getInstance(int rows, int columns, byte[][] mat, boolean copy) {
        if (copy) {
            byte[][] _mat = new byte[rows][];
            for (int i = 0; i < rows; i++) {
                _mat[i] = Arrays.copyOf(mat[i], columns);
            }
            return new BinaryMatrix(rows, columns, _mat);
        } else {
            return new BinaryMatrix(rows, columns, mat);
        }
    }

    public static BinaryMatrix ones(int n) {
        byte[][] mat = new byte[n][n];
        for (int i = 0; i < n; i++) {
            mat[i][i] = (byte) 1;
        }
        BinaryMatrix o = new BinaryMatrix(n, n, mat);
        return o;
    }

    public IntRowMatrix multiplyRight(IntRowMatrix f) {
        if (this.columns != f.rows) {
            throw new DimensionMismatchException(
                    "The number of columns of this matrix must equal to the number of rows of another matrix.");
        }
        int[] _mat = new int[this.rows];
        for (int i = 0; i < this.rows; i++) {
            _mat[i] = f.multiplyLeft(this.mat[i]);
        }
        return IntRowMatrix.getInstance(_mat, false);
    }

    public LongRowMatrix multiplyRight(LongRowMatrix f) {
        if (this.columns != f.rows) {
            throw new DimensionMismatchException(
                    "The number of columns of this matrix must equal to the number of rows of another matrix.");
        }
        long[] _mat = new long[this.rows];
        for (int i = 0; i < this.rows; i++) {
            _mat[i] = f.multiplyLeft(this.mat[i]);
        }
        return LongRowMatrix.getInstance(_mat, false);
    }

    public BinaryMatrix transposed() {
        byte[][] t = new byte[columns][rows];
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                t[i][j] = mat[j][i];
            }
        }
        return new BinaryMatrix(columns, rows, t);
    }

    public void swapRows(int i, int j) {
        byte[] ri = mat[i];
        byte[] rj = mat[j];
        mat[i] = rj;
        mat[j] = ri;
    }

    public void addRows(int src, int dst) {
        for (int j = 0; j < columns; j++) {
            mat[dst][j] ^= mat[src][j];
        }
    }

    public IntRowMatrix intRowMatrix() {
        if (columns == 32) {
            int[] r = new int[rows];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    r[i] |= (mat[i][j] & 1) << j;
                }
            }
            return IntRowMatrix.getInstance(r, false);
        } else {
            throw new DimensionMismatchException("Cannot convert to an IntMatrix. The number of columns must be 32.");
        }
    }

    public LongRowMatrix longRowMatrix() {
        if (columns == 64) {
            long[] r = new long[rows];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    r[i] |= (mat[i][j] & 1L) << j;
                }
            }
            return LongRowMatrix.getInstance(r, false);
        } else {
            throw new DimensionMismatchException("Cannot convert to an LongMatrix. The number of columns must be 64.");
        }
    }

    public BinaryMatrix resized(int newRows, int newColumns) {
        byte[][] newMat = new byte[newRows][];
        for (int i = 0; i < this.rows && i < newRows; i++) {
            newMat[i] = Arrays.copyOf(this.mat[i], newColumns);
        }
        for (int i = this.rows; i < newRows; i++) {
            newMat[i] = new byte[newColumns];
        }
        return new BinaryMatrix(newRows, newColumns, newMat);
    }

    public BinaryMatrix generalizedInverse() {
        Echelon e = echelon();
        BinaryMatrix p = e.transform;
        int rank = e.rank;
        List<Integer> pivots = e.pivots;
        BinaryMatrix permp = p.resized(this.columns, this.rows);
        for (int i = rank - 1; i >= 0; i--) {
            int columnIndex = pivots.get(i);
            permp.swapRows(i, columnIndex);
        }
        return permp;
    }

    /**
     * この行列の行簡約階段形を返す。{@code this} に変更は加えない
     * https://ja.wikipedia.org/wiki/行階段形#行簡約階段形
     * @return 
     */
    public Echelon echelon() {
        BinaryMatrix e = BinaryMatrix.getInstance(rows, columns, mat, true);
        BinaryMatrix p = BinaryMatrix.ones(rows);
        int rank = 0;
        List<Integer> pivots = new ArrayList<>();
        for (int j = 0; j < columns; j++) {
            for (int i = rank; i < e.rows; i++) {
                if (e.mat[i][j] != 0) {
                    /* erase other rows */
                    for (int k = 0; k < e.rows; k++) {
                        if ((k != i) && e.mat[k][j] != 0) {
                            e.addRows(i, k);
                            p.addRows(i, k);
                        }
                    }
                    e.swapRows(i, rank);
                    p.swapRows(i, rank);
                    pivots.add(j);
                    rank++;
                    break;
                }
            }
        }
        return new Echelon(e, p, rank, pivots);
    }

    public static class Echelon {
        public final BinaryMatrix echelon;
        public final BinaryMatrix transform;
        public final int rank;
        public final List<Integer> pivots;

        Echelon(BinaryMatrix f, BinaryMatrix p, int rank, List<Integer> pivots) {
            this.echelon = f;
            this.transform = p;
            this.rank = rank;
            this.pivots = pivots;
        }
    }
}
