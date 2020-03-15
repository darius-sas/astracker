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
     * Default Arcan for C projects. Will analyse only the current version in a branch.
     * CLI command :
     * java -jar arcan/Arcan-c-1.0.2-RELEASE-jar-with-dependencies.jar -versionId version -p cloned-projects/pure/ -out new-folder
     * @param project
     * @param args
     * @return
     */
    public static GitArcanCRunner newSingleVersionGitRunner(IProject project, Args args){
        //TODO: Refactor the two functions, only the args are different.
        var path = Paths.get(args.getGitRepo().toString());
        var versionId = String.format("%s-%s-%s",
                "1",
                getCommitDateFromPath(path),
                getCommitFromPath(path).getName()
        );

        var arcanArgs = String.format("-versionId %s -p %s -out %s -branch %s",
                versionId,
                args.getGitRepo().getAbsolutePath(),
                args.getArcanOutDir(),
                "master"
            );

        var arcanCommand = "java -jar " + args.getArcanJarFile();
        GitArcanCRunner arcan = new GitArcanCRunner("arcanC", project, args, arcanCommand);
        arcan.setArgs(arcanArgs.split(" "));
        arcan.inheritOutput(args.showArcanOutput);
        return arcan;
    }

    /**
     * Default Arcan for C projects. Will analyse all the versions in the current branch.
     * @param project
     * @param args
     * @return
     */
    public static GitArcanCRunner newGitRunner(IProject project, Args args){
        var arcanArgs = String.format("-git -p %s -out %s -branch %s -startDate %s -nWeeks %d",
                args.getGitRepo().getAbsolutePath(),
                args.getArcanOutDir(),
                "master",
                "1-1-1",
                2);

        var arcanCommand = "java -jar " + args.getArcanJarFile();
        GitArcanCRunner arcan = new GitArcanCRunner("arcanC", project, args, arcanCommand);
        arcan.setArgs(arcanArgs.split(" "));
        arcan.inheritOutput(args.showArcanOutput);
        return arcan;
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
            var lastCommitId = getCommitFromPath(path);
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
    private static AnyObjectId getCommitFromPath(Path path) {
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
