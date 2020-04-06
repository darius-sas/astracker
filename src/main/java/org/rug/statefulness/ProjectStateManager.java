package org.rug.statefulness;

import org.rug.data.project.IProject;
import org.rug.data.project.IVersion;

import java.io.*;
import java.nio.file.Paths;

/**
 * This class serializes the state of a project as a file in a given directory.
 * Methods for recovering the state of a project are also made available.
 * The serialization only saves the state of the latest version analysed.
 */
public class ProjectStateManager {

    private final File lastVersion;

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
        if (!dir.exists()){
            dir.mkdirs();
        }
        if (!dir.isDirectory()){
            dir.delete();
            throw new IllegalArgumentException("Project state directory must not be a file.");
        }
        this.lastVersion = Paths.get(dir.getAbsolutePath(), "version.seo").toFile();
    }

    /**
     * Convenience method that saves the state of the project by retrieving the last version and invoking {@link #saveState(IVersion)}.
     * @param project the project to save.
     * @throws IOException if the serialization fails
     */
    public void saveState(IProject project) throws IOException {
       saveState(project.versions().last());
    }

    /**
     * Save the state of a project by saving the state of the information identfying the latest version of the system.
     * @param lastVersion the latest version.
     * @throws IOException if the serialization fails.
     */
    public void saveState(IVersion lastVersion) throws IOException {
        try(var oos = new ObjectOutputStream(new FileOutputStream(this.lastVersion))) {
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
        try(var ois = new ObjectInputStream(new FileInputStream(this.lastVersion))) {
            lastVersionString = (String) ois.readObject();
            lastVersionposition = (long) ois.readObject();
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
}
