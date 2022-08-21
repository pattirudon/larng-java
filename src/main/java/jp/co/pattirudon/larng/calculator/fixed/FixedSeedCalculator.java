package jp.co.pattirudon.larng.calculator.fixed;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class FixedSeedCalculator {
    public void solve(FixedSeedPredicatorConfig config, Logger logger, Path resultFile) {

        FixedSeedCalculationFromDuplexIV calculator = new FixedSeedCalculationFromDuplexIV(config.numShinyRolls);
        FixedSeedPredicator predicator = new FixedSeedPredicator(config.numShinyRolls,
                config.ability.outwardAbility, config.ability.numAbilities,
                config.nature, config.gender.outwardGender, config.gender.genderRatio,
                config.getWeightMap());

        int[] ivs = new int[] { config.ivs.h, config.ivs.a, config.ivs.b, config.ivs.c, config.ivs.d, config.ivs.s };
        int[][] ivsOmote = new int[6][32];
        int[][] ivsUra = new int[6][32];
        for (int i = 0; i < ivsOmote.length; i++) {
            int copyI = i;
            ivsOmote[i] = new int[32];
            ivsUra[i] = new int[32];
            Arrays.setAll(ivsOmote[i], j -> j);
            Arrays.setAll(ivsUra[i], j -> (ivs[copyI] - j) & 0x1f);
        }

        Range range555 = com.aparapi.device.JavaDevice.THREAD_POOL.createRange3D(32, 32, 32);
        CDSBruteForceKernel kernel = new CDSBruteForceKernel(ivsOmote, ivsUra, calculator, predicator);

        try (OutputStream os = Files.newOutputStream(resultFile, StandardOpenOption.CREATE);
                JsonGenerator jsonGenerator = new JsonFactory().createGenerator(os, JsonEncoding.UTF8)) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName("randoms");
            jsonGenerator.writeStartArray();
            for (int i = 0; i < 32; i++) {
                kernel.setI(i);
                for (int j = 0; j < 32; j++) {
                    kernel.setJ(j);
                    for (int k = 0; k < 32; k++) {
                        System.out.printf("%d / %d\r", (i * 32 + j) * 32 + k, 32 * 32 * 32);
                        kernel.setK(k);
                        kernel.getSeeds().clear();
                        kernel.execute(range555);
                        for (int s = 0; s < kernel.getSeeds().size(); s++) {
                            long seed = kernel.getSeeds().get(s);
                            jsonGenerator.writeString(String.format("%016x", seed));
                        }
                    }
                    jsonGenerator.flush();
                }
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public class CDSBruteForceKernel extends Kernel {
        int i, j, k;
        int[][] ivsOmote, ivsUra;
        FixedSeedCalculationFromDuplexIV calculator;
        FixedSeedPredicator predicator;
        List<Long> seeds = new CopyOnWriteArrayList<>();

        public CDSBruteForceKernel(int[][] ivsOmote, int[][] ivsUra, FixedSeedCalculationFromDuplexIV calculator,
                FixedSeedPredicator predicator) {
            this.ivsOmote = ivsOmote;
            this.ivsUra = ivsUra;
            this.calculator = calculator;
            this.predicator = predicator;
        }

        @Override
        public void run() {
            int l = getGlobalId(0);
            int m = getGlobalId(1);
            int n = getGlobalId(2);
            long[] seeds = calculator.seeds(ivsOmote[0][i], ivsUra[0][i], ivsOmote[1][j], ivsUra[1][j],
                    ivsOmote[2][k], ivsUra[2][k], ivsOmote[3][l], ivsUra[3][l], ivsOmote[4][m], ivsUra[4][m],
                    ivsOmote[5][n], ivsUra[5][n]);
            for (int i = 0; i < seeds.length; i++) {
                if (predicator.test(seeds[i]))
                    this.seeds.add(seeds[i]);
            }
        }

        public void setI(int i) {
            this.i = i;
        }

        public void setJ(int j) {
            this.j = j;
        }

        public void setK(int k) {
            this.k = k;
        }

        public List<Long> getSeeds() {
            return this.seeds;
        }

    }
}
