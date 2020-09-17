package org.rug.runners;

import org.rug.args.Args;
import org.rug.data.project.IProject;
import org.rug.web.helpers.ArcanArgsHelper;

import java.io.IOException;

public class GitArcanJavaRunner extends AbstractGitArcanRunner {

    private GitArcanJavaRunner(String toolName, IProject project, Args args, String command) {
        super(toolName, project, args, command);
    }

    public static GitArcanJavaRunner newGitRunner(IProject project, Args args){
        logger.debug("This is a Git Java project! Adding Arcan Java Runner to the list of runners");
        var arcanCommand = "java -jar " + args.getArcanJarFile();
        GitArcanJavaRunner arcan = new GitArcanJavaRunner("arcan", project, args, arcanCommand);
        arcan.setArgs(ArcanArgsHelper.getArguments(args).split(" "));
        arcan.inheritOutput(args.showArcanOutput);
        return arcan;
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
