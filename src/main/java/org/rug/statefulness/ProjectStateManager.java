package org.rug.statefulness;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rug.data.project.IProject;
import org.rug.data.project.IVersion;

import java.io.*;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * This class serializes the state of a project as a file in a given directory.
 * Methods for recovering the state of a project are also made available.
 * The serialization only saves the state of the latest version analysed.
 */
public class ProjectStateManager {

    private final static Logger logger = LogManager.getLogger(ProjectStateManager.class);

    private File dir;
    private File savedStateFile;
    private boolean wasAnalysedBefore = false;

    /**
     * Initialize the state manager with a directory to use for saving the states.
     * @param dir the directory where serialized projects will be saved and loaded from.
     */
    public ProjectStateManager(String dir){
        this(new File(dir));
    }

    /**
     * Initialize the state manager with a directory to use for saving the states.
     * @param dir the directory where serialized projects will be saved and loaded from.
     */
    public ProjectStateManager(File dir){
        this.dir = dir;
        if (!dir.exists()){
            // Directory doesn't exist, meaning that the analysis was not performed before
            // create the directory and the versioning file.
            try {
                dir.mkdirs();
                File file = new File(dir.toString() + "/version.seo");
                file.createNewFile();
                this.savedStateFile = file;
                this.wasAnalysedBefore = false;
                logger.info("The project was not analysed before, creating versioning file: " + file.getPath());
            } catch (IOException e) {
                logger.error("Could not create the versioning file: " + savedStateFile.getPath());
                e.printStackTrace();
            }

        } else {
            this.wasAnalysedBefore = true;
            this.savedStateFile = Paths.get(dir.getAbsolutePath(), "version.seo").toFile();
            logger.info("The project was analysed before, loading from versioning file: " + savedStateFile.getPath());
        }
    }

    /**
     * Convenience method that saves the state of the project by retrieving the last version and invoking {@link #saveState(IVersion)}.
     * @param project the project to save.
     * @throws IOException if the serialization fails
     */
    public void saveState(IProject project) throws IOException {
        Optional<IVersion> firstVersion = project.versions().stream().findFirst();
        if (firstVersion.isPresent()) {
            saveState(firstVersion.get());
        }
    }

    /**
     * Save the state of a project by saving the state of the information identfying the latest version of the system.
     * @param lastVersion the latest version.
     * @throws IOException if the serialization fails.
     */
    public void saveState(IVersion lastVersion) throws IOException {
        try(var oos = new ObjectOutputStream(new FileOutputStream(this.savedStateFile))) {
            oos.writeObject(lastVersion.getVersionString()); // alternatively we can only serialize the versionString.
            oos.writeObject(lastVersion.getVersionIndex());
        }
    }

    /**
     * Load the state of a previously serialized project instance.
     * @param instance the project instance to configure for analysis from the latest version serialized.
     * @throws IOException if deserialization fails.
     * @throws ClassNotFoundException if deserialization fails.
     */
    public void loadState(IProject instance) throws IOException, ClassNotFoundException {
        String lastVersionString;
        long lastVersionposition;
        try(var ois = new ObjectInputStream(new FileInputStream(this.savedStateFile))) {
            lastVersionString = (String) ois.readObject();
            lastVersionposition = (long) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Could not load the state from the ProjectStateManager");
            return;
        }
        if (instance.getVersionedSystem().containsKey(lastVersionString)){
            instance.setVersionedSystem(instance.getVersionedSystem().tailMap(lastVersionString));
            for(var v : instance.getVersionedSystem().values()){
                v.setVersionIndex(lastVersionposition++);
            }
        }else {
            throw new IllegalStateException("Cannot load state for current project: last version string is not contained in the starting project.");
        }
    }

    public File getSavedStateFile() {
        return savedStateFile;
    }

    public File getDir() {
        return dir;
    }

    public boolean wasAnalysedBefore() {
        return wasAnalysedBefore;
    }
}
