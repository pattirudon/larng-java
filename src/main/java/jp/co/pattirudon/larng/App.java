package jp.co.pattirudon.larng;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.FlatLightLaf;

import jp.co.pattirudon.larng.calculator.fixed.FixedSeedCalculator;
import jp.co.pattirudon.larng.calculator.fixed.FixedSeedPredicatorConfig;
import jp.co.pattirudon.larng.calculator.group.GroupSeedCalculator;
import jp.co.pattirudon.larng.calculator.group.GroupSeedPredicatorConfig;
import jp.co.pattirudon.larng.calculator.spawner.SpawnerSeedCalculatorParallel;
import jp.co.pattirudon.larng.gui.TabbedTopFrame;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(version = "1.0", description = "Determine Xoroshiro seeds", mixinStandardHelpOptions = true, sortOptions = false)
public class App {
    public static void main(String[] args) {
        new CommandLine(new App()).execute(args);
    }

    public static Logger getConsoleLogger() throws IOException {
        Logger logger = Logger.getLogger(App.class.getName());
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.CONFIG);
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new BareFormatter());
        consoleHandler.setLevel(Level.CONFIG);
        logger.addHandler(consoleHandler);
        return logger;
    }

    public static Logger getLogger(String fileName) throws IOException {
        Logger logger = getConsoleLogger();
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

    @Command(name = "find-fixed-seeds", description = "Search for 64-bit fixed seeds of the xoroshiro. "
            + "Parameters of the generated Pokemon is required.")
    public void invokeFixedSeedCalculator(
            @Parameters(paramLabel = "PATH", description = "Path to a param json file.") Path paramFilePath)
            throws Exception {
        long start = System.currentTimeMillis();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
        InputStream is = Files.newInputStream(paramFilePath);
        FixedSeedPredicatorConfig config = mapper.readValue(is, FixedSeedPredicatorConfig.class);
        String resultFileName = "fixed_seeds_" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
                + ".json";
        String resultFileDir = "fixedseeds";
        Files.createDirectories(Path.of(resultFileDir));
        Path resultFile = Path.of(resultFileDir, resultFileName);
        Logger logger = getConsoleLogger();
        new FixedSeedCalculator().solve(config, logger, resultFile);
        long end = System.currentTimeMillis();
        logger.config(String.format("Finish. [%d ms]", end - start));
    }

    @Command(name = "find-spawner-seeds", description = "Search for 64-bit spawner seeds of the xoroshiro. "
            + "A list of fixed seeds is required.")
    public void invokeSpawnerSeedCalculator(
            @Option(names = { "-l", "--list" }, paramLabel = "LIST", description = "Path to a list of fixed seeds") /**/
            Path fixedSeedFilePath)
            throws Exception {
        long start = System.currentTimeMillis();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
        InputStream is = Files.newInputStream(fixedSeedFilePath);
        RandomList list = mapper.readValue(is, RandomList.class);
        String resultFileName = "spawner_seeds_" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
                + ".json";
        String resultFileDir = "spawnerseeds";
        Files.createDirectories(Path.of(resultFileDir));
        Path resultFile = Path.of(resultFileDir, resultFileName);
        Logger logger = getConsoleLogger();
        new SpawnerSeedCalculatorParallel().solve(list, logger, resultFile);
        long end = System.currentTimeMillis();
        logger.config(String.format("Finish. [%d ms]", end - start));
    }

    @Command(name = "find-group-seeds", description = "Search for 64-bit group seeds of the xoroshiro. "
            + "Parameters of the secondary generated Pokemon is required.")
    public void invokeGroupSeedCalculator(
            @Option(names = { "-l",
                    "--list" }, paramLabel = "LIST", description = "Path to a list of spawner seeds") /**/
            Path spawnerSeedFilePath,
            @Parameters(paramLabel = "PATH", description = "Path to a param json file.") Path paramFilePath)
            throws Exception {
        long start = System.currentTimeMillis();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
        InputStream is = Files.newInputStream(spawnerSeedFilePath);
        RandomList list = mapper.readValue(is, RandomList.class);
        InputStream is1 = Files.newInputStream(paramFilePath);
        GroupSeedPredicatorConfig filter = mapper.readValue(is1, GroupSeedPredicatorConfig.class);
        String logFileName = "result_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
                + ".log";
        Logger logger = getLogger(logFileName);
        new GroupSeedCalculator().solve(list, filter, logger);
        long end = System.currentTimeMillis();
        logger.config(String.format("Finish. [%d ms]", end - start));
    }

    @Command(name = "gui", description = "Launch a graphical interface.")
    public void invokeGUI() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    FlatLightLaf.setup();
                    TabbedTopFrame frame = new TabbedTopFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
