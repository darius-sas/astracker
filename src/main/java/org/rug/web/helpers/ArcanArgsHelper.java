package org.rug.web.helpers;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.rug.args.Args;
import org.rug.runners.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;

/**
 * Helper class that will generate the list of CLI arguments needed in order to run Arcan on a project.
 * Can be used for both Java and C projects.
 */
public class ArcanArgsHelper {

    protected final static Logger logger = LoggerFactory.getLogger(ToolRunner.class);

    /**
     * Returns the list of CLI arguments that are used when running Arcan on multiple versions of a Git project.
     * @param args
     * @return String
     */
    public static String getArguments(Args args) {
        if (args.shouldAnalyseSingleVersion()) {
            return getSingleVersionArguments(args);
        }
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
    public static String getSingleVersionArguments(Args args) {
        logger.debug("This is a single version project!");
        var path = Paths.get(args.getGitRepo().toString());
        var versionId = String.format("%s-%s-%s",
                "1",
                getCommitDateFromPath(path),
                getCommitHashFromPath(path).getName()
        );

        return String.format("%s -versionId %s -p %s -out %s -branch %s",
                args.isJavaProject() ? "-singleVersion" : "" , //flag needed for Java projects
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
}
