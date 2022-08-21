package jp.co.pattirudon.larng.calculator.spawner;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import jp.co.pattirudon.larng.RandomList;

public class SpawnerSeedCalculatorParallel implements SpawnerSeedCalculator {

    long[] x0SliceMasks = new long[8];
    long[] x1SliceMasks = new long[8];
    long[] x0CompMasks = new long[8];
    long[] x1CompMasks = new long[8];
    long[][] x0Slices = new long[8][1 << 8];
    long[][] x1Slices = new long[8][1 << 8];
    long[] results = new long[256 * 256 * 256];
    int[] numResults = new int[256 * 256 * 256];

    public SpawnerSeedCalculatorParallel() {
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
                long x0Comp = x0(x1Comp) & x0SliceMasks[i];
                x0Slices[i][k] = x0Comp;
                x1Slices[i][k] = x1Comp;
            }
        }
    }

    public void solve(RandomList config, Logger logger, Path resultFile) {
        solve(config.randoms, logger, resultFile);
    }

    public void solve(List<Long> randoms, Logger logger, Path resultFile) {
        try (OutputStream os = Files.newOutputStream(resultFile, StandardOpenOption.CREATE);
                JsonGenerator jsonGenerator = new JsonFactory().createGenerator(os, JsonEncoding.UTF8)) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName("randoms");
            jsonGenerator.writeStartArray();
            for (int i = 0; i < randoms.size(); i++) {
                System.out.printf("%d / %d\r", i, randoms.size());
                long random = randoms.get(i);
                String[] seeds = seeds(random).stream().map(n -> String.format("%016x", n)).sorted()
                        .toArray(String[]::new);
                logger.fine(String.format("%016x: %s", random, Arrays.toString(seeds)));
                for (int j = 0; j < seeds.length; j++) {
                    String seed = seeds[j];
                    jsonGenerator.writeString(seed);
                }
                jsonGenerator.flush();
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Long> seeds(long random) {
        Range range888 = Range.create3D(256, 256, 256);
        Kernel kernel = new SeedSolverKernel(x0SliceMasks, x1SliceMasks, x0CompMasks, x1CompMasks,
                x0Slices[0], x1Slices[0], x0Slices[1], x1Slices[1], x0Slices[2], x1Slices[2], results, numResults,
                random);
        kernel.execute(range888);
        List<Long> result = IntStream.range(0, results.length).parallel().filter(i -> (numResults[i] > 0))
                .sequential().mapToLong(i -> results[i]).boxed().collect(Collectors.toList());
        return result;
    }

    /**
     * $L^{24}\mathrm{const} = x_0  +  \left(1 + U^{16}\right)L^{27}x_1 + L^{51}x_1$
     * @param x1
     * @return
     */
    public static long x0(long x1) {
        x1 = Long.rotateLeft(x1, 27);
        long x0 = (Long.rotateLeft(0x82a2b175_229d6a5bL, 24))
                ^ x1 ^ (x1 << 16) ^ Long.rotateLeft(x1, 24);
        return x0;
    }

    public class SeedSolverKernel extends Kernel {

        long[] x0SliceMasks, x1SliceMasks;
        long[] x0CompMasks, x1CompMasks;
        long[] x0Slices_0, x1Slices_0;
        long[] x0Slices_1, x1Slices_1;
        long[] x0Slices_2, x1Slices_2;
        long[] results;
        int[] numResults;
        long XOROSHIRO_CONST = 0x82a2b175_229d6a5bL;
        long random;

        public SeedSolverKernel(long[] x0SliceMasks, long[] x1SliceMasks, long[] x0CompMasks, long[] x1CompMasks,
                long[] x0Slices_0, long[] x1Slices_0, long[] x0Slices_1, long[] x1Slices_1, long[] x0Slices_2,
                long[] x1Slices_2, long[] results, int[] numResults, long random) {
            this.x0SliceMasks = x0SliceMasks;
            this.x1SliceMasks = x1SliceMasks;
            this.x0CompMasks = x0CompMasks;
            this.x1CompMasks = x1CompMasks;
            this.x0Slices_0 = x0Slices_0;
            this.x1Slices_0 = x1Slices_0;
            this.x0Slices_1 = x0Slices_1;
            this.x1Slices_1 = x1Slices_1;
            this.x0Slices_2 = x0Slices_2;
            this.x1Slices_2 = x1Slices_2;
            this.results = results;
            this.numResults = numResults;
            this.random = random;
        }

        @Override
        public void run() {
            int i = getGlobalId(0);
            int j = getGlobalId(1);
            int k = getGlobalId(2);
            int q = (i * getGlobalSize(1) + j) * getGlobalSize(2) + k;
            results[q] = 0L;
            numResults[q] = 0;
            long x0Comp_2 = x0Slices_0[i] | x0Slices_1[j] | x0Slices_2[k];
            long x1Comp_2 = x1Slices_0[i] | x1Slices_1[j] | x1Slices_2[k];
            long remaining = random - x0Comp_2 - x1Comp_2;
            long a = remaining & 0x08080808_08080808L;
            long b = (remaining - 0xc0c0c0c0_c0c0c0c0L) & 0x08080808_08080808L;
            long diff = a ^ b;
            long notEq_0, notEq_1, notEq_2, notEq_3,
                    notEq_4, notEq_5, notEq_6, notEq_7;
            notEq_0 = (int) (diff >>> 3) & 1;
            notEq_1 = (int) (diff >>> 11) & 1;
            notEq_2 = (int) (diff >>> 19) & 1;
            notEq_3 = (int) (diff >>> 27) & 1;
            notEq_4 = (int) (diff >>> 35) & 1;
            notEq_5 = (int) (diff >>> 43) & 1;
            notEq_6 = (int) (diff >>> 51) & 1;
            notEq_7 = (int) (diff >>> 59) & 1;
            long part_0, part_1, part_2, part_3,
                    part_4, part_5, part_6, part_7;
            for (int i0 = 0; i0 <= notEq_0; i0++) {
                part_0 = i0 == 1 ? (1L << 3) : 0L;
                for (int i1 = 0; i1 <= notEq_1; i1++) {
                    part_1 = part_0 | (i1 == 1 ? (1L << 11) : 0L);
                    for (int i2 = 0; i2 <= notEq_2; i2++) {
                        part_2 = part_1 | (i2 == 1 ? (1L << 19) : 0L);
                        for (int i3 = 0; i3 <= notEq_3; i3++) {
                            part_3 = part_2 | (i3 == 1 ? (1L << 27) : 0L);
                            for (int i4 = 0; i4 <= notEq_4; i4++) {
                                part_4 = part_3 | (i4 == 1 ? (1L << 35) : 0L);
                                for (int i5 = 0; i5 <= notEq_5; i5++) {
                                    part_5 = part_4 | (i5 == 1 ? (1L << 43) : 0L);
                                    for (int i6 = 0; i6 <= notEq_6; i6++) {
                                        part_6 = part_5 | (i6 == 1 ? (1L << 51) : 0L);
                                        for (int i7 = 0; i7 <= notEq_7; i7++) {
                                            part_7 = part_6 | (i7 == 1 ? (1L << 59) : 0L);
                                            long x1Slice_3 = a ^ part_7;
                                            long x0Slice_3 = x0(x1Slice_3) & x0SliceMasks[3];
                                            long x0Comp_3 = x0Comp_2 | x0Slice_3;
                                            long x1Comp_3 = x1Comp_2 | x1Slice_3;
                                            long x1Slice_4 = (random - x0Comp_3 - x1Comp_3) & 0x10101010_10101010L;
                                            long x0Slice_4 = x0(x1Slice_4) & x0SliceMasks[4];
                                            long x0Comp_4 = x0Comp_3 | x0Slice_4;
                                            long x1Comp_4 = x1Comp_3 | x1Slice_4;
                                            long x1Slice_5_6_7 = (random - x0Comp_4 - x1Comp_4) & 0xe0e0e0e0_e0e0e0e0L;
                                            long x0Slice_5_6_7 = x0(x1Slice_5_6_7) & 0x07070707_07070707L;
                                            long x0 = x0Comp_4 | x0Slice_5_6_7;
                                            long x1 = x1Comp_4 | x1Slice_5_6_7;
                                            if (x0 + x1 == random) {
                                                long seed = seed(x1);
                                                results[q] = seed;
                                                numResults[q] = numResults[q] + 1;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        public long rotateLeft(long x, int k) {
            return (x << k) | (x >>> (64 - k));
        }

        /**
         * $L^{24}\mathrm{const} = x_0  +  \left(1 + U^{16}\right)L^{27}x_1 + L^{51}x_1$
         * @param x1
         * @return
         */
        public long x0(long x1) {
            x1 = rotateLeft(x1, 27);
            long x0 = (rotateLeft(XOROSHIRO_CONST, 24))
                    ^ x1 ^ (x1 << 16) ^ rotateLeft(x1, 24);
            return x0;
        }

        /**
         * $x_1 = L^{37}(\mathrm{seed} + \mathrm{const})$
         * @param x1
         * @return
         */
        public long seed(long x1) {
            return rotateLeft(x1, 27) ^ XOROSHIRO_CONST;
        }
    }
}
