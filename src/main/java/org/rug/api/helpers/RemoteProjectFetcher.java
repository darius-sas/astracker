package org.rug.api.helpers;

import org.eclipse.jgit.api.Git;
import org.rug.args.Args;
import java.io.File;
import java.io.IOException;

/**
 * Helper class that will be used to clone locally a remote Git repository.
 */
public class RemoteProjectFetcher {

    /**
     * Checks locally if the project has already been cloned and adds the path to the location.
     * If not, it will clone the project from Git first. TODO: Might wanna make it prettier
     *
     * @param args The arguments of the request. Will contain the link to the remote GitHub repository
     *             (should end in .git).
     * @return args The same object will be returned, but now enriched with the location of the local
     *              repository (in args.project.gitRepo)
     */
    public Args fetchProject(Args args) {
        try {
            if (this.isValidGitLink(args.getGitLink())) {
                var directory = String.format("./cloned-projects/%s", args.project.name);
                var file = new File(directory);

                if (this.checkIfAlreadyCloned(file)) {
                    // the project was already cloned - no need to clone it again
                    // update the path and return the arguments
                    args.setGitRepo(file);
                    return args;
                }

                try (Git git = Git.cloneRepository()
                        .setURI(args.getGitLink())
                        .setDirectory(file)
                        .call()) {

                    args.setGitRepo(file);

                } catch (Exception e) {
                    e.printStackTrace();
                    return args;
                }
            } else {
                System.out.println("The provided link was not a valid Git link!");
                return args;
            }

        } catch (IOException e) {
            System.out.println("The link is not a valid Git link!");
            e.printStackTrace();
        }

        return args;
    }

    /**
     * Will check if a folder with the same name exists a the given location.
     *
     * @param file The folder location
     * @return boolean
     */
    public boolean checkIfAlreadyCloned(File file) {
        //TODO Check this
        if (file.exists() && file.isDirectory() && file.list() == null) {
            System.out.println("The project was already cloned - no need to do it again");
            return true;
        }

        return false;
    }

    /**
     * Used to check whether a link is a valid GitHub link.
     * @param link
     * @return bool
     * @throws IOException
     */
    private boolean isValidGitLink(String link) throws IOException {
        //TODO: could add another check to see if the project name is the same as the git one :+1:
        return link.matches(".*\\.git");
    }
}
