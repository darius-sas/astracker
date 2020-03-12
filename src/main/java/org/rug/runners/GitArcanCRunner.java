package org.rug.runners;

import org.rug.args.Args;
import org.rug.data.project.IProject;

import java.io.IOException;

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

    @Override
    protected void preProcess() {

    }

    @Override
    protected void postProcess(Process p) throws IOException {
        args.adjustProjDirToArcanOutput();
        project.addGraphMLfiles(args.getHomeProjectDirectory());
    }
}
