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

import static org.rug.tracker.ASmellTracker.*;

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
    }

    /**
     * Save the state of the given tracker on file.
     * @param tracker the object to serialize.
     * @throws IOException if serialization fails.
     */
    public void saveState(ASmellTracker tracker) throws IOException {
        try(var outStream = new ObjectOutputStream(new FileOutputStream(trackerFile))) {
            outStream.writeObject(tracker);
            tracker.getTrackGraph().traversal().V().properties(ASmellTracker.SMELL_OBJECT).drop().iterate();
            tracker.getTrackGraph().traversal().io(trackGraph.getAbsolutePath()).with(IO.writer, IO.graphml).write().iterate();
            tracker.getTrackGraph().traversal().io(condensedGraph.getAbsolutePath()).with(IO.writer, IO.graphml).write().iterate();
        }
    }

    /**
     * Instantiate a new the tracker with a recovered state ready to analyse the next version of the given project.
     * @param project the project.
     * @param lastVersion the last version in the project.
     * @return a new instance of AStracker that can analyse the remaining version in the given project.
     * @throws IOException if deserialization fails.
     * @throws ClassNotFoundException if deserialization fails.
     */
    public ASmellTracker loadState(IProject project, IVersion lastVersion) throws IOException, ClassNotFoundException {
        ASmellTracker tracker;
        try(var inpStream = new ObjectInputStream(new FileInputStream(trackerFile))) {
           tracker = (ASmellTracker) inpStream.readObject();
        }
        tracker.setCondensedGraph(TinkerGraph.open());
        tracker.getCondensedGraph().traversal().io(condensedGraph.getAbsolutePath()).with(IO.reader, IO.graphml).read().iterate();

        tracker.setTrackGraph(TinkerGraph.open());
        tracker.getTrackGraph().traversal().io(trackGraph.getAbsolutePath()).with(IO.reader, IO.graphml).read().iterate();

        tracker.setTail(tracker.getTrackGraph().traversal().V().hasLabel(TAIL).next());

        var lastVersionSmellVertices = tracker.getTrackGraph().traversal().V().hasLabel(TAIL).out().has(VERSION, lastVersion.getVersionString()).toSet();
        var lastVersionSmells = project.getArchitecturalSmellsIn(lastVersion);

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

}
