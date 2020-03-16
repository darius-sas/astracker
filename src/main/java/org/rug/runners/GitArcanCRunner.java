package org.rug.runners;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.rug.args.Args;
import org.rug.data.project.IProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;

public class GitArcanCRunner extends ToolRunner {

    IProject project;
    Args args;
    /**
     * Initializes a tool with the given name and the given command.
     *
     * @param toolName The prefix toolName of the tool used in the properties file.
     * @param command  The command to execute.
     */
    private GitArcanCRunner(String toolName, IProject project, Args args, String command) {
        super(toolName, command);
        this.project = project;
        this.args = args;
    }

    /**
     * Arcan runner for C projects. Can analyse all the versions in the current branch, or a single version.
     * @param project
     * @param args
     * @return
     */
    public static GitArcanCRunner newGitRunner(IProject project, Args args){
        String arcanArgs = null;
        if (args.shouldAnalyseSingleVersion()) {
            arcanArgs = getSingleVersionArcanArgs(args);
        } else {
            arcanArgs = getArcanArgs(args);
        }

        var arcanCommand = "java -jar " + args.getArcanJarFile();
        GitArcanCRunner arcan = new GitArcanCRunner("arcanC", project, args, arcanCommand);
        arcan.setArgs(arcanArgs.split(" "));
        arcan.inheritOutput(args.showArcanOutput);
        return arcan;
    }

    /**
     * Returns the list of CLI arguments that are used when running Arcan on multiple versions of a Git project.
     * @param args
     * @return String
     */
    private static String getArcanArgs(Args args) {
        System.out.println("This is a Git C project! Adding Arcan C Runner");
        return String.format("-git -p %s -out %s -branch %s -startDate %s -nWeeks %d",
                args.getGitRepo().getAbsolutePath(),
                args.getArcanOutDir(),
                "master",
                "1-1-1",
                2);
    }

    /**
     * Returns the list of CLI arguments that are used when running Arcan on a single version of a Git project.
     * @param args
     * @return String
     */
    private static String getSingleVersionArcanArgs(Args args) {
        System.out.println("This is a single version C project! Adding Arcan C Runner");
        var path = Paths.get(args.getGitRepo().toString());
        var versionId = String.format("%s-%s-%s",
            "1",
            getCommitDateFromPath(path),
            getCommitHashFromPath(path).getName()
        );

        return String.format("-versionId %s -p %s -out %s -branch %s",
            versionId,
            args.getGitRepo().getAbsolutePath(),
            args.getArcanOutDir(),
            "master"
        );
    }

    /**
     * Used to return the date of the current commit, formatted according to ASTracker.
     *
     * @param path
     * @return String
     * @throws IOException
     */
    private static String getCommitDateFromPath(Path path) {
        RevCommit commit = null;

        try {
            Git git = Git.init().setDirectory(new File(path.toString())).call();
            var lastCommitId = getCommitHashFromPath(path);
            var revWalk = new RevWalk(git.getRepository());
            commit = revWalk.parseCommit(lastCommitId);

        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(commit.getAuthorIdent().getWhen());

        return String.format("%s_%s_%s" ,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.YEAR)
        );
    }
    /**
     * Used to return the hash of the currently checkout commit.
     *
     * @param path
     * @return String
     * @throws IOException
     */
    private static AnyObjectId getCommitHashFromPath(Path path) {
        Git git = null;
        AnyObjectId lastCommitId = null;
        try {
            git = Git.init().setDirectory(new File(path.toString())).call();
            lastCommitId = git.getRepository().resolve(Constants.HEAD);

        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
        }
        return (lastCommitId !=null ) ? lastCommitId : null;
    }

    @Override
    protected void preProcess() {

    }

    @Override
    protected void postProcess(Process p) throws IOException {
        args.adjustProjDirToArcanOutput();
        project.addGraphMLfiles(args.getHomeProjectDirectory());
    }
}
