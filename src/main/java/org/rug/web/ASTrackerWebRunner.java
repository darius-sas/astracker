package org.rug.web;

import com.beust.jcommander.JCommander;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rug.Analysis;
import org.rug.web.helpers.ArgumentMapper;
import org.rug.web.helpers.RemoteProjectFetcher;
import org.rug.args.Args;
import org.rug.persistence.PersistenceHub;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;

public class ASTrackerWebRunner {

    private final static Logger logger = LogManager.getLogger(ASTrackerWebRunner.class);

    private ArgumentMapper mapper;
    private static final Path arcanJavaJar = Paths.get("arcan/Arcan-1.4.0-SNAPSHOT/Arcan-1.4.0-SNAPSHOT.jar");
    private static final Path arcanCppJar  = Paths.get("arcan/Arcan-c-1.0.2-RELEASE-jar-with-dependencies.jar");
    private static final Path outputDirectory = Paths.get("./output-folder");
    private static final Path clonedReposDirectory = Paths.get("./cloned-projects");

    public ASTrackerWebRunner(Map<String, String> requestParameter) {
        try {
            Files.createDirectory(outputDirectory);
            Files.createDirectory(Paths.get(outputDirectory.toAbsolutePath().toString(), "trackASOutput"));
            Files.createDirectory(Paths.get(outputDirectory.toAbsolutePath().toString(), "arcanOutput"));
            Files.createDirectory(clonedReposDirectory);
        } catch (IOException e) {
            logger.error("Could not create working directories: {}", e.getMessage());
        }
        this.mapper = new ArgumentMapper(arcanJavaJar, arcanCppJar, outputDirectory, clonedReposDirectory, requestParameter);
    }

    /**
     * Runs the analysis tool with the given parameters. Returns a String
     * informing the user of the progress or errors that may have occurred.
     * The Analysis can be found in the output folder.
     *
     * @return String
     */
    public String run() throws Exception {
        PersistenceHub.clearAll();
        var mapping = this.mapper.getArgumentsMapping();
        if (mapping.length == 1) {
            return null;
        }

        Args args = new Args();
        JCommander jc = JCommander.newBuilder()
                .addObject(args)
                .build();

        jc.setProgramName("java -jar astracker.jar");
        jc.parse(mapping);

        if (args.help) {
            return this.getHelp();
        }

        Analysis analysis = new Analysis(args);

        boolean errorsOccurred = false;
        String errorRunnerName = "";

        for (var r : analysis.getRunners()) {
            int exitCode = r.run();
            errorsOccurred = exitCode != 0;
            if (errorsOccurred) {
                errorRunnerName = r.getToolName();
                break;
            }
        }
        if (errorsOccurred) {
            throw new Exception("Unexpected errors have occurred while running runner " + errorRunnerName);
        }

        PersistenceHub.closeAll();
        return "Completed successfully.";
    }

    public String getCLIArgs() {
        return this.mapper.toString();
    }

    /**
     * Will return the help menu as if using -help in the CLI
     *
     * @return String
     */
    public String getHelp() {
        Args args = new Args();
        JCommander jc = JCommander.newBuilder()
                .addObject(args)
                .build();

        StringBuilder var1 = new StringBuilder();
        jc.usage(var1);
        return var1.toString();
    }
}
