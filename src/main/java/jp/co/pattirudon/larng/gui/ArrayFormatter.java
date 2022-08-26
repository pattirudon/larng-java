package jp.co.pattirudon.larng.gui;

public class ArrayFormatter {
    public static String toString(int[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0;; i++) {
            sb.append(array[i]);
            if (i == array.length - 1)
                break;
            else
                sb.append(", ");
        }
        return sb.toString();
    }
}
