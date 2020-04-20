package org.rug.runners;

import org.rug.args.Args;
import org.rug.data.project.IProject;
import org.rug.web.helpers.ArcanArgsHelper;

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
