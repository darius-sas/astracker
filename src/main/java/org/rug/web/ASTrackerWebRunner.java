package org.rug.web;

import com.beust.jcommander.JCommander;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rug.Analysis;
import org.rug.args.Args;
import org.rug.data.project.IProject;
import org.rug.persistence.PersistenceHub;
import org.rug.statefulness.ASmellTrackerStateManager;
import org.rug.statefulness.ProjectStateManager;
import org.rug.web.credentials.Credentials;
import org.rug.web.helpers.ArgumentMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.rug.Analysis.buildProjectFromArgs;
import static org.rug.web.WebAnalysisController.Result;

public class ASTrackerWebRunner {

    private final static Logger logger = LogManager.getLogger(ASTrackerWebRunner.class);

    private ArgumentMapper mapper;
    private static final Path arcanJavaJar = Paths.get("arcan/Arcan-1.4.0-SNAPSHOT/Arcan-1.4.0-SNAPSHOT.jar");
    private static final Path arcanCppJar  = Paths.get("arcan/Arcan-c-1.3.1-SNAPSHOT-jar-with-dependencies.jar");
    public static final Path outputDirectory = Paths.get("./output-folder");
    public static final Path statesDirectory = Paths.get("./states");
    public static final Path clonedReposDirectory = Paths.get("./cloned-projects");
    public static Path arcanOutput = Paths.get(outputDirectory.toAbsolutePath().toString(), "arcanOutput");
    public static Path trackASoutput = Paths.get(outputDirectory.toAbsolutePath().toString(), "trackASOutput");;

    public ASTrackerWebRunner(Map<String, String> requestParameter, Credentials credentials) {
        try {
            if (Files.notExists(outputDirectory)) {
                Files.createDirectory(outputDirectory);
                logger.info("Created directory {}", outputDirectory.toAbsolutePath().toString());
            }

            if (Files.notExists(trackASoutput)) {
                Files.createDirectory(trackASoutput);
                logger.info("Created directory {}", trackASoutput.toAbsolutePath().toString());
            }

            if (Files.notExists(arcanOutput)) {
                Files.createDirectory(arcanOutput);
                logger.info("Created directory {}", arcanOutput.toAbsolutePath().toString());
            }
            if (Files.notExists(clonedReposDirectory)) {
                Files.createDirectory(clonedReposDirectory);
                logger.info("Created directory {}", clonedReposDirectory.toAbsolutePath().toString());
            }
            if (Files.notExists(statesDirectory)) {
                Files.createDirectory(statesDirectory);
                logger.info("Created directory {}", statesDirectory.toAbsolutePath().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Could not create working directories: {}", e.getMessage());
        }

        this.mapper = new ArgumentMapper(arcanJavaJar, arcanCppJar, outputDirectory, clonedReposDirectory, requestParameter, credentials);
    }

    /**
     * Runs the analysis tool with the given parameters. Returns a String
     * informing the user of the progress or errors that may have occurred.
     * The Analysis can be found in the output folder.
     *
     * @return String
     */
    public Result run() throws Exception {
        PersistenceHub.clearAll();
        Args args = buildArgumentsList();
        Analysis analysis;

        var projectStatesDirectory = Paths.get(statesDirectory.toString(), getProjectName()).toString();
        var projectStateManager = new ProjectStateManager(projectStatesDirectory);
        var aSmellTrackerStateManager = new ASmellTrackerStateManager(projectStatesDirectory);

        if (projectStateManager.wasAnalysedBefore()) {

            IProject project = buildProjectFromArgs(args);
            project.addGraphMLfiles(arcanOutput.toString()+ '/'+ args.project.name);

            //Load the previous state of the project
            projectStateManager.loadState(project);
            logger.debug("The previous state has been loaded");
            if (project.getVersionedSystem().size() == 1) {
                logger.info("The project is already analysed. No need to perform the analysis again");
                return Result.SKIPPED;
            }

            // initialize an ASmellTracker object from the project state
            var lastVersionAnalysed = project.versions().first(); // the first version is the first non-analysed version
            var aSmellTracker = aSmellTrackerStateManager.loadState(project, lastVersionAnalysed);
            logger.info("The ASmellTrackerStateManager has been successfully loaded.");
            analysis = new Analysis(args, aSmellTracker, project);
            logger.debug("Analysis is ready to be resumed.");

        } else {
            // Run the analysis from the beginning.
            analysis = new Analysis(args);
        }

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

        projectStateManager.saveState(analysis.getProject());
        aSmellTrackerStateManager.saveState(analysis.getASmellTracker());
        logger.info("The state of the analysis has been saved.");

        PersistenceHub.closeAll();
        logger.info("Completed.");
        return Result.SUCCESS;
    }

    /**
     * Will instantiate an {@link Args} object using the arguments extracted from the request.
     * @return
     * @throws IOException
     */
    private Args buildArgumentsList() throws Exception {
        var mapping = this.mapper.getArgumentsMapping();
        if (mapping.length == 1) {
            throw new IllegalArgumentException("Request malformed due to illegal arguments provided.");
        }

        Args args = new Args();
        JCommander jc = JCommander.newBuilder()
                .addObject(args)
                .build();

        jc.setProgramName("java -jar astracker.jar");
        jc.parse(mapping);

        return args;
    }

    public String getProjectName(){
        return this.mapper.getProjectName();
    }
}
