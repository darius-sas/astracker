package org.rug;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rug.args.Args;
import org.rug.data.project.*;
import org.rug.persistence.*;
import org.rug.runners.*;
import org.rug.tracker.ASmellTracker;
import org.rug.tracker.SimpleNameJaccardSimilarityLinker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * The class defines an analysis of a project. The class initializes objects of itself based on the arguments
 * provided by the command line.
 */
public class Analysis {

    private static final Logger logger = LogManager.getLogger(Analysis.class);

    private final Args args;
    private IProject project;
    private final List<ToolRunner> runners;
    private ASmellTracker aSmellTracker;

    /**
     * General constructor that can be used to analyse all the versions on a project.
     *
     * @param args
     * @throws IOException
     */
    public Analysis(Args args) throws IOException {
        this.args = args;
        this.runners = new ArrayList<>();
        this.project = buildProjectFromArgs(args);
        this.aSmellTracker = new ASmellTracker(
                new SimpleNameJaccardSimilarityLinker(),
                args.trackNonConsecutiveVersions
        );
        if (args.isGitProject()) {
            logger.info("Repo: " + args.getGitRepo().getAbsolutePath());
        }
        init();
    }

    /**
     * Initializes a constructor with an already instantiated {@link ASmellTracker}. This object
     * will be passed to the {@link TrackASRunner}, so that the analysis  can be resumed
     * from a given version instead of analysing the whole project.
     *
     * @param args
     * @param aSmellTracker
     * @throws IOException
     */
    public Analysis(Args args, ASmellTracker aSmellTracker, IProject project) throws IOException {
        this.args = args;
        this.runners = new ArrayList<>();
        this.aSmellTracker = aSmellTracker;
        this.project = project;
        init();
    }

    private void init() throws IOException{
        if (args.runArcan()) {
            runners.add(getArcanRunner());
        } else if (isGraphMLProject()){
            project.addGraphMLfiles(args.getHomeProjectDirectory());
        } else if (args.project.isJar) {
            project.addSourceDirectory(args.getHomeProjectDirectory());
            var outputDir = args.getArcanOutDir();
            project.forEach(version -> {
                Path outputDirVers = Paths.get(outputDir, version.getVersionString());
                if (outputDirVers.toFile().mkdirs() && version instanceof Version) {
                    var arcan = new ArcanRunner(args.getArcanJarFile(), (Version) version,
                            outputDirVers.toString(), project.isFolderOfFoldersOfSourcesProject(), false);
                    arcan.setHomeDir(args.getHomeProjectDirectory());
                    arcan.inheritOutput(args.showArcanOutput);
                    runners.add(arcan);
                }
            });
            args.adjustProjDirToArcanOutput();
            project.addGraphMLfiles(args.getHomeProjectDirectory());
        } else {
            throw new IllegalArgumentException("Cannot parse project files.");
        }

        if (args.runTracker()){
            runners.add(new TrackASRunner(project, aSmellTracker, args.shouldAnalyseSingleVersion()));

            if (args.similarityScores) {
                PersistenceHub.register(new SmellSimilarityDataGenerator(args.getSimilarityScoreFile()));
            }

            if (args.smellCharacteristics) {
                PersistenceHub.register(new SmellCharacteristicsGenerator(args.getSmellCharacteristicsFile(), project));
                PersistenceHub.register(new ComponentAffectedByGenerator(args.getAffectedComponentsFile()));
            }

            if (args.componentCharacteristics){
                PersistenceHub.register(new ComponentMetricGenerator(args.getComponentCharacteristicsFile()));
            }

            PersistenceHub.register(new CondensedGraphGenerator(args.getCondensedGraphFile()));
            PersistenceHub.register(new TrackGraphGenerator(args.getTrackGraphFileName()));
        }

        if (args.runProjectSizes()){
            runners.add(new ProjecSizeRunner(project));
            PersistenceHub.register(new ProjectSizeGenerator(args.getProjectSizesFile()));
        }

        if (args.runFanInFanOutCounter()){
            runners.add(new FanInFanOutCounterRunner(project));
            PersistenceHub.register(new EdgeCountGenerator(args.getFanInFanOutFile()));
        }
    }

    /**
     * Will decide which Arcan runner to add to the project depending on the arguments.
     * GitRunner - for Java projects
     * GitCRunner - for C projects
     *
     * @return ToolRunner
     */
    private ToolRunner getArcanRunner() {
        ToolRunner arcanRunner;

        if(args.isJavaProject()) {
            arcanRunner = GitArcanJavaRunner.newGitRunner(project, args);
        } else {
            // C project, will use the Arcan C analyzer
             arcanRunner = GitArcanCRunner.newGitRunner(project, args);
            }
        return arcanRunner;
    }

    /**
     * Static function to help with returning an IProject instance initialized
     * with the given name and directories
     *
     * @param args
     * @return
     * @throws IOException
     */
    public static IProject buildProjectFromArgs(Args args) throws IOException {
        AbstractProject.Type pType;
        if (args.project.isCPP) {
            pType = AbstractProject.Type.CPP;
        } else if (args.project.isC) {
            pType = AbstractProject.Type.C;
        } else {
            pType = AbstractProject.Type.JAVA;
        }

        IProject project;
        if (args.isGitProject()) {
            project = new GitProject(args.project.name, args.getGitRepo(), pType);
            project.addSourceDirectory(args.getGitRepo().getAbsolutePath());
        } else {
            project = new Project(args.project.name, pType);
        }
        return project;
    }

    public IProject getProject() { return project; }
    public List<ToolRunner> getRunners() {
        return runners;
    }
    public ASmellTracker getASmellTracker() { return aSmellTracker; }

    private boolean isGraphMLProject() throws IOException{
        try(var files = Files.walk(args.inputDirectory.toPath())){
            return files.anyMatch(path -> path.getFileName().toString().matches(".*\\.graphml"));
        }
    }

}
