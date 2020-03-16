package org.rug.runners;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.rug.args.Args;
import org.rug.data.project.IProject;
import org.rug.web.helpers.ArcanArgsHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;

public class GitArcanCRunner extends AbstractGitArcanRunner {

    private GitArcanCRunner(String toolName, IProject project, Args args, String command) {
        super(toolName, project, args, command);
    }

    /**
     * Arcan runner for C projects. Can analyse all the versions in the current branch, or a single version.
     * @param project
     * @param args
     * @return
     */
    public static GitArcanCRunner newGitRunner(IProject project, Args args){
        logger.debug("This is a C project! Adding Arcan C Runner to the list of runners");
        var arcanCommand = "java -jar " + args.getArcanJarFile();
        GitArcanCRunner arcan = new GitArcanCRunner("arcanC", project, args, arcanCommand);
        arcan.setArgs(ArcanArgsHelper.getArguments(args).split(" "));
        arcan.inheritOutput(args.showArcanOutput);
        return arcan;
    }
}
