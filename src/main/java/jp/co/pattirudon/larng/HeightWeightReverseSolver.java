package jp.co.pattirudon.larng;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.TreeMap;
import java.util.TreeSet;

public class HeightWeightReverseSolver {
    public static TreeMap<Integer, TreeSet<Integer>> weightMap(float height, float mediHeight, float weight,
            float mediWeight) {
        TreeMap<Integer, TreeSet<Integer>> result = new TreeMap<>();
        for (Integer heightScalar : solveForHeight(height, mediHeight)) {
            result.put(heightScalar, solveForWeight(heightScalar, weight, mediWeight));
        }
        return result;
    }

    public static TreeSet<Integer> solveForHeight(float height, float mediHeight) {
        TreeSet<Integer> results = new TreeSet<>();
        BigDecimal target = BigDecimal.valueOf(height).setScale(2, RoundingMode.HALF_UP);
        for (int s = 0; s < 256; s++) {
            float ratio = getRatio(s);
            float h = ratio * mediHeight;
            BigDecimal tempHeight = BigDecimal.valueOf(h).setScale(2, RoundingMode.HALF_UP);
            if (tempHeight.equals(target))
                results.add(s);
        }
        return results;
    }

    public static TreeSet<Integer> solveForWeight(int heightScalar, float weight, float mediWeight) {
        TreeSet<Integer> results = new TreeSet<>();
        BigDecimal target = BigDecimal.valueOf(weight).setScale(2, RoundingMode.HALF_UP);
        for (int s = 0; s < 256; s++) {
            float ratio = getRatio(heightScalar) * getRatio(s);
            float w = ratio * mediWeight;
            BigDecimal tempWeight = BigDecimal.valueOf(w).setScale(2, RoundingMode.HALF_UP);
            if (tempWeight.equals(target))
                results.add(s);
        }
        return results;
    }

    /**
     * https://github.com/kwsch/PKHeX/blob/c6aa13ddc6dde62bf0bd104af209c4e7de85e9fc/PKHeX.Core/PKM/PA8.cs
     * @param scalar
     * @return
     */
    private static float getRatio(int scalar) {
        float result = scalar / Float.intBitsToFloat(0x437F0000);
        result *= Float.intBitsToFloat(0x3ECCCCCE);
        result += Float.intBitsToFloat(0x3F4CCCCD);
        return result;
    }
}
