package org.rug.web.helpers;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper class that will be used to clone locally a remote Git repository.
 */
public class RemoteProjectFetcher {

    private final static Logger logger = LoggerFactory.getLogger(RemoteProjectFetcher.class);
    private final Path destination;

    public RemoteProjectFetcher(Path destination){
        this.destination = destination;
    }

    /**
     * Helper class that will take a path as an argument, and will return a Path object
     * which holds the location of the project to be analysed.
     *
     * @param linkOrName
     * @return
     * @throws IOException
     * @throws GitAPIException
     */
    public Path getProjectPath(String linkOrName) throws IOException, GitAPIException {
        if (this.isValidGitLink(linkOrName)) {
            var name = getProjectName(linkOrName);
            var path = Paths.get(destination.toAbsolutePath().toString(), name);
            if (!this.checkIfAlreadyCloned(path.toFile())) {
                // The project is not present locally, clone it from the remote repository.
                var git = Git.cloneRepository()
                    .setURI(linkOrName)
                    .setDirectory(path.toFile()).call();
                git.close();
            }
            // The project has been cloned before, update the branch to the latest version.
            var git = Git.open(path.toFile());
            var result = git.pull().setRemote("origin").setRemoteBranchName("master").call();
            if (!result.isSuccessful()) {
                logger.warn(String.format(
                        "Was not able to pull the latest changes from the repository %s",
                        name
                ));
            }
            return path;
        } else {
            return Paths.get(destination.toAbsolutePath().toString(), linkOrName);
        }
    }

    /**
     * Will check if a folder with the same name exists a the given location.
     *
     * @param file The folder location
     * @return boolean
     */
    public boolean checkIfAlreadyCloned(File file) {
        var list = file.list();
        return file.exists() && file.isDirectory() && list != null && list.length > 0;
    }

    /**
     * Will return the name of a project extracted from the path given as input.
     * - If the path is a Git link, it will look between the last "/" and "." symbols.
     * - If the path is just a name, it will return the same string.
     *
     * @param linkOrName
     * @return
     */
    public String getProjectName(String linkOrName){
        var slashIndex = linkOrName.lastIndexOf("/");
        var dotIndex = linkOrName.lastIndexOf(".");
        return slashIndex == -1 || dotIndex == -1 ? linkOrName : linkOrName.substring(slashIndex +1, dotIndex);
    }

    public boolean isValidGitLink(String link) {
        return link.matches("^http(s)?:.*\\.git$");
    }

}
