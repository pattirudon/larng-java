package jp.co.pattirudon.larng.matrices;

public class DoubleIndexedLongRowMatrix {
    public final int rowsFirstLevel, rowsSecondLevel;
    public final long[][] mat;
    public final int columns = 64;

    protected DoubleIndexedLongRowMatrix(int rowsFirstLevel, int numBitsSecondLevel, long[][] mat) {
        this.rowsFirstLevel = rowsFirstLevel;
        this.rowsSecondLevel = 1 << numBitsSecondLevel;
        this.mat = mat;
    }

    public static DoubleIndexedLongRowMatrix getInstance(int rowsFirstLevel, int numBitsSecondLevel, LongRowMatrix mat) {
        int rowsSecondLevel = 1 << numBitsSecondLevel;
        if (rowsFirstLevel * numBitsSecondLevel != mat.rows)
            throw new IllegalArgumentException("rowsFirstLevel * numBitsSecondLevel "
                    + "must equal to the number of rows of given matrix");
        long[][] newMat = new long[rowsFirstLevel][rowsSecondLevel];
        for (int i1 = 0; i1 < rowsFirstLevel; i1++) {
            for (int i2 = 0; i2 < rowsSecondLevel; i2++) {
                byte[] rowVect = new byte[rowsFirstLevel * numBitsSecondLevel];
                for (int j = 0; j < numBitsSecondLevel; j++) {
                    rowVect[i1 * numBitsSecondLevel + j] = (byte) ((i2 >>> j) & 1);
                }
                newMat[i1][i2] = mat.multiplyLeft(rowVect);
            }
        }
        return new DoubleIndexedLongRowMatrix(rowsFirstLevel, numBitsSecondLevel, newMat);
    }

    public long multiplyLeft(int[] rowVect) {
        if (rowVect.length != rowsFirstLevel)
            throw new DimensionMismatchException();
        long result = 0L;
        for (int i = 0; i < rowsFirstLevel; i++) {
            result ^= mat[i][rowVect[i]];
        }
        return result;
    }

    public static int log2(int n) {
        int log = 0;
        while (n > 1) {
            log++;
            n /= 2;
        }
        return log;
    }
}