package org.rug.api;

import com.beust.jcommander.JCommander;
import org.rug.Analysis;
import org.rug.api.helpers.ArgumentMapper;
import org.rug.api.helpers.RemoteProjectFetcher;
import org.rug.args.Args;
import org.rug.persistence.PersistenceHub;

import java.io.IOException;

public class ASTrackerRunner {

    private ArgumentMapper mapper;

    public ASTrackerRunner(ArgumentMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Runs the analysis tool with the given parameters. Returns a String
     * informing the user of the progress or errors that may have occurred.
     * The Analysis can be found in the output folder.
     *
     * @return String
     */
    public String run() throws IOException {
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

        //Step1: clone the repository locally if not yet exists
        if (args.isRemoteGitProject()) {
            var fetcher = new RemoteProjectFetcher();
            args = fetcher.fetchProject(args);

            if (args.getGitLink() == null) {
                return "Something went wrong when cloning the project: " + args.getGitLink();
            }
        }

        //Step2: perform the analysis
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
            return "Unexpected errors have occurred while running runner " + errorRunnerName;
        }

        PersistenceHub.closeAll();
        return "Writing to output directory.";
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
