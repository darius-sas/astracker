package org.rug.runners;

import org.rug.args.Args;
import org.rug.data.project.IProject;

import java.io.IOException;

abstract class AbstractGitArcanRunner extends ToolRunner {

    protected IProject project;
    protected Args args;
    /**
     * Initializes a tool with the given name and the given command.
     *
     * @param toolName The prefix toolName of the tool used in the properties file.
     * @param command  The command to execute.
     */
    protected AbstractGitArcanRunner(String toolName, IProject project, Args args, String command) {
        super(toolName, command);
        this.project = project;
        this.args = args;
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
