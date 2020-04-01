package org.rug.statefulness;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rug.data.project.IProject;
import org.rug.data.project.IVersion;
import org.rug.web.ASTrackerWebRunner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ProjectStateManager {

    private final static Logger logger = LogManager.getLogger(ASTrackerWebRunner.class);

    private File dir;
    private File savedStateFile;
    private boolean wasAnalysedBefore;


    public ProjectStateManager(String dir){
        this(new File(dir));
    }

    public ProjectStateManager(File dir){
        this.dir = dir;
        if (!dir.exists()){
            // Directory didnt exist so the analysis was not performed before.
            try {
                dir.mkdirs();
                File file = new File(dir.toString() + "/version.seo");
                file.createNewFile();
                this.savedStateFile = file;
                this.wasAnalysedBefore = false;
            } catch (IOException e) {
                logger.error("Could not create the versioning file");
                e.printStackTrace();
            }

        } else {
            this.wasAnalysedBefore = true;
            this.savedStateFile = Paths.get(dir.getAbsolutePath(), "version.seo").toFile();
        }
    }

    public void saveState(IProject project) throws IOException {
       saveState(project.versions().last());
    }

    public void saveState(IVersion lastVersion) throws IOException {
        try(var oos = new ObjectOutputStream(new FileOutputStream(this.savedStateFile))) {
            oos.writeObject(lastVersion.getVersionString()); // alternatively we can only serialize the versionString.
            oos.writeObject(lastVersion.getVersionIndex());
        }
    }

    public void loadState(IProject instance) throws IOException, ClassNotFoundException {
        String lastVersionString;
        long lastVersionposition;
        try(var ois = new ObjectInputStream(new FileInputStream(this.savedStateFile))) {
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

    public File getSavedStateFile() {
        return savedStateFile;
    }

    public File getDir() {
        return dir;
    }
}
