package org.rug.statefulness;

import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.data.project.IProject;
import org.rug.data.project.IVersion;
import org.rug.tracker.ASmellTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;

import static org.rug.tracker.ASmellTracker.TAIL;
import static org.rug.tracker.ASmellTracker.VERSION;

/**
 * Saves the state of the object that performs the tracking of smells from one version to the next.
 * This allows to recover an analysis from the last version analysed.
 */
public class ASmellTrackerStateManager {

    private final static Logger logger = LoggerFactory.getLogger(ASmellTrackerStateManager.class);

    private File condensedGraph;
    private File trackGraph;
    private File trackerFile;

    /**
     * Instantiate a state manager using the given directory to save the serialized tracker.
     * @param dir the directory to use. This directory must be specific to a certain project and
     *            existing files will be overwritten.
     */
    public ASmellTrackerStateManager(String dir){
        this(new File(dir));
    }

    /**
     * Instantiate a state manager using the given directory to save the serialized tracker.
     * @param dir the directory to use. This directory must be specific to a certain project and
     *            existing files will be overwritten.
     */
    public ASmellTrackerStateManager(File dir){
        if (!dir.exists()){
            dir.mkdirs();
        }
        if (!dir.isDirectory()){
            dir.delete();
            throw new IllegalArgumentException("Tracker state directory argument must be a directory.");
        }
        this.condensedGraph = Paths.get(dir.getAbsolutePath(), "condensed.graphml").toFile();
        this.trackGraph = Paths.get(dir.getAbsolutePath(), "track.graphml").toFile();
        this.trackerFile = Paths.get(dir.getAbsolutePath(), "tracker.seo").toFile();
        createFilesIfNotExisting();
    }

    /**
     * Save the state of the given tracker on file.
     * @param tracker the object to serialize.
     */
    public void saveState(ASmellTracker tracker) {
        try(var outStream = new ObjectOutputStream(new FileOutputStream(trackerFile))) {
            outStream.writeObject(tracker);
            tracker.getTrackGraph().traversal().V().properties(ASmellTracker.SMELL_OBJECT).drop().iterate();
            tracker.getTrackGraph().traversal().io(trackGraph.getAbsolutePath()).with(IO.writer, IO.graphml).write().iterate();
            tracker.getCondensedGraph().traversal().io(condensedGraph.getAbsolutePath()).with(IO.writer, IO.graphml).write().iterate();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Saving the state of the ASmellTracker failed.");
        }
    }

    /**
     * Instantiate a new the tracker with a recovered state ready to analyse the next version of the given project.
     * @param project the project.
     * @param lastVersionAnalysed the last version in the project that was analysed.
     * @return a new instance of AStracker that can analyse the remaining version in the given project.
     * @throws IOException if deserialization fails.
     * @throws ClassNotFoundException if deserialization fails.
     */
    public ASmellTracker loadState(IProject project, IVersion lastVersionAnalysed) throws IOException, ClassNotFoundException {
        ASmellTracker tracker;
        try(var inpStream = new ObjectInputStream(new FileInputStream(trackerFile))) {
           tracker = (ASmellTracker) inpStream.readObject();
           tracker.initializeCache();
           logger.debug("Tracker was loaded from file");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Could not load the tracker from file. Using new tracker instead.");
            return new ASmellTracker();
        }

        tracker.setCondensedGraph(TinkerGraph.open());
        tracker.getCondensedGraph().traversal().io(condensedGraph.getAbsolutePath()).with(IO.reader, IO.graphml).read().iterate();
        tracker.setTrackGraph(TinkerGraph.open());
        tracker.getTrackGraph().traversal().io(trackGraph.getAbsolutePath()).with(IO.reader, IO.graphml).read().iterate();

        tracker.setTail(tracker.getTrackGraph().traversal().V().hasLabel(TAIL).next());

        var uniqueSmellsMap = tracker.getUniqueSmellsMap();
        tracker.getCondensedGraph().traversal().V()
                .has(ASmellTracker.UNIQUE_SMELL_ID)
                .forEachRemaining( v -> uniqueSmellsMap.put(v.value(ASmellTracker.UNIQUE_SMELL_ID), v));

        var lastVersionSmellVertices = tracker.getTrackGraph().traversal().V().hasLabel(TAIL).out().has(VERSION, lastVersionAnalysed.getVersionString()).toSet();
        var lastVersionSmells = project.getArchitecturalSmellsIn(lastVersionAnalysed);

        assert lastVersionSmells.size() == lastVersionSmellVertices.size();

        for (var smell : lastVersionSmells){
            var smellVertex = lastVersionSmellVertices.stream()
                    .filter(v -> v.value(ASmellTracker.SMELL_ID).equals(smell.getId()))
                    .findFirst();
            if (smellVertex.isEmpty()){
                logger.error("Unable to find a match for smell with ID: {}", smell.getId());
                continue;
            }
            smellVertex.get().property(ASmellTracker.SMELL_OBJECT, smell);
        }

        return tracker;
    }

    /**
     * Helper class that will create the neccesary versioning files if they don't exist
     * already in order to save the state of the analysis.
     */
    private void createFilesIfNotExisting() {
        try {
            if (!this.condensedGraph.exists()) this.condensedGraph.createNewFile();
            if (!this.trackGraph.exists()) this.trackGraph.createNewFile();
            if (!this.trackerFile.exists()) this.trackerFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Could not create the versioning files for the ASmellTrackerStateManager");
        }
    }
}
