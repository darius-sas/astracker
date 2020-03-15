package org.rug.web.helpers;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper class that will be used to clone locally a remote Git repository.
 */
public class RemoteProjectFetcher {

    private final Path destination;

    public RemoteProjectFetcher(Path destination){
        this.destination = destination;
    }

    public Path getProjectPath(String linkOrName) {
        if (this.isValidGitLink(linkOrName)) {
            var name = getProjectName(linkOrName);
            var file = Paths.get(destination.toAbsolutePath().toString(), name);
            if (!this.checkIfAlreadyCloned(file.toFile())) {
                try {
                    var git = Git.cloneRepository()
                        .setURI(linkOrName)
                        .setDirectory(file.toFile()).call();
                    git.close();
                } catch (GitAPIException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return file;
        }else {
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

    public String getProjectName(String linkOrName){
        var slashIndex = linkOrName.lastIndexOf("/");
        var dotIndex = linkOrName.lastIndexOf(".");
        return slashIndex == -1 || dotIndex == -1 ? linkOrName : linkOrName.substring(slashIndex+1, dotIndex);
    }

    public boolean isValidGitLink(String link) {
        return link.matches("^http(s)?:.*\\.git$");
    }

}
