package jp.co.pattirudon.larng;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.pattirudon.larng.calculator.fixed.FixedSeedCalculator;
import jp.co.pattirudon.larng.calculator.fixed.FixedSeedPredicatorConfig;
import jp.co.pattirudon.larng.calculator.group.GroupSeedCalculator;
import jp.co.pattirudon.larng.calculator.group.GroupSeedPredicatorConfig;
import jp.co.pattirudon.larng.calculator.spawner.SpawnerSeedCalculatorParallel;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(version = "1.0", description = "Determine Xoroshiro seeds", mixinStandardHelpOptions = true, sortOptions = false)
public class App implements Callable<Integer> {
    @ArgGroup(heading = "Actions:\n", exclusive = true, multiplicity = "1")
    Actions actions;

    static class Actions {
        @Option(order = 0, names = {
                "find-fixed-seeds" }, required = true, description = "Search for 64-bit fixed seeds of the xoroshiro. "
                        + "Parameters of the generated Pokemon is required.")
        boolean fixed;
        @Option(order = 1, names = {
                "find-spawner-seeds" }, required = true, description = "Search for 64-bit spawner seeds of the xoroshiro. "
                        + "--fixed-seeds options is required.")
        boolean spawner;
        @Option(order = 2, names = {
                "find-group-seeds" }, required = true, description = "Search for 64-bit group seeds of the xoroshiro. "
                        + "--spawner-seeds options is required.")
        boolean group;
    }

    @Option(names = { "-p", "--param" }, paramLabel = "PATH", description = "Path to a param json file.")
    Path paramFilePath;

    @Option(names = { "-f", "--fixed-seeds" }, paramLabel = "LIST", description = "Path to a list of fixed seeds")
    Path fixedSeedFilePath;

    @Option(names = { "-s", "--spawner-seeds" }, paramLabel = "LIST", description = "Path to a list of spawner seeds")
    Path spawnerSeedFilePath;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        String logFileName = "result_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
                + ".log";
        Logger logger = getLogger(logFileName);
        long start = System.currentTimeMillis();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
        if (actions.fixed) {
            InputStream is = Files.newInputStream(paramFilePath);
            FixedSeedPredicatorConfig config = mapper.readValue(is, FixedSeedPredicatorConfig.class);
            String resultFileName = "fixed_seeds_" + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
                    + ".json";
            String resultFileDir = "fixedseeds";
            Files.createDirectories(Path.of(resultFileDir));
            Path resultFile = Path.of(resultFileDir, resultFileName);
            new FixedSeedCalculator().solve(config, logger, resultFile);
        } else if (actions.spawner) {
            InputStream is = Files.newInputStream(fixedSeedFilePath);
            RandomList list = mapper.readValue(is, RandomList.class);
            String resultFileName = "spawner_seeds_" + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
                    + ".json";
            String resultFileDir = "spawnerseeds";
            Files.createDirectories(Path.of(resultFileDir));
            Path resultFile = Path.of(resultFileDir, resultFileName);
            new SpawnerSeedCalculatorParallel().solve(list, logger, resultFile);
        } else if (actions.group) {
            InputStream is = Files.newInputStream(spawnerSeedFilePath);
            RandomList list = mapper.readValue(is, RandomList.class);
            InputStream is1 = Files.newInputStream(paramFilePath);
            GroupSeedPredicatorConfig filter = mapper.readValue(is1, GroupSeedPredicatorConfig.class);
            new GroupSeedCalculator().solve(list, filter, logger);
        }
        long end = System.currentTimeMillis();
        logger.config(String.format("Finish. [%d ms]", end - start));
        return 0;
    }

    public static Logger getLogger(String fileName) throws IOException {
        Logger logger = Logger.getLogger(App.class.getName());
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.CONFIG);
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new BareFormatter());
        consoleHandler.setLevel(Level.CONFIG);
        logger.addHandler(consoleHandler);
        String dirName = "log";
        Files.createDirectories(Path.of(dirName));
        boolean append = true;
        Handler fileHandler = new FileHandler(Path.of(dirName, fileName).toString(), append);
        fileHandler.setFormatter(new BareFormatter());
        fileHandler.setLevel(Level.INFO);
        logger.addHandler(fileHandler);
        return logger;
    }

    final static class BareFormatter extends Formatter {
        @Override
        public synchronized String format(LogRecord aRecord) {
            final StringBuffer message = new StringBuffer();
            message.append(formatMessage(aRecord));
            message.append(String.format("%n"));
            return message.toString();
        }
    }
}
