package org.rug.simpletests.statefulness;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.rug.data.project.IProject;
import org.rug.simpletests.tracker.ASmellTrackerTest;
import org.rug.statefulness.ASmellTrackerStateManager;
import org.rug.tracker.ASmellTracker;
import org.rug.tracker.SimpleNameJaccardSimilarityLinker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.rug.simpletests.TestData.antlr;
import static org.rug.simpletests.TestData.pure;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ASmellTrackerStateManagerTest {

    protected Map<String, Long> antlrOracle;
    protected Map<String, Long> pureOracle;
//    private Map<String, Integer> antOracle;

    @BeforeAll
    void init(){
        antlrOracle = new HashMap<>();
        var oracle = new long[]{0, 3, 3, 4, 5, 3, 26, 61, 61, 26, 35, 0, 113, 34, 183, 23, 44,
                95, 125, 116, 151, 28};
        int i = 0;
        for(var v : antlr){
            antlrOracle.put(v.getVersionString(), oracle[i++]);
        }

        pureOracle = new HashMap<>();
        oracle = new long[]{ 0, 997, 108, 1039, 109};
        i = 0;
        for (var v : pure){
            pureOracle.put(v.getVersionString(), oracle[i++]);
        }
//        antOracle = new HashMap<>();
//        oracle = new int[]{0, 9, 29, 42, 55, 27, 97, 94, 94, 100, 53, 143, 151, 222, 234, 234, 210,
//                           205, 167, 181, 145, 348, 231};
//        for(var v : ant){
//            antlrOracle.put(v.getVersionString(), oracle[i++]);
//        }

    }

    @Test
    void testStatefulness() throws IOException, ClassNotFoundException {
        var tracker = new ASmellTracker(new SimpleNameJaccardSimilarityLinker(), false);

        var v1 = antlr.getVersionWith(1);
        var v2 = antlr.getVersionWith(2);
        var v3 = antlr.getVersionWith(3);
        var v4 = antlr.getVersionWith(4);

        var stateManager = new ASmellTrackerStateManager("test-data/output/states");

        tracker.track(antlr.getArchitecturalSmellsIn(v1), v1);
        tracker.track(antlr.getArchitecturalSmellsIn(v2), v2);
        assertEquals((long)antlrOracle.get(v2.getVersionString()), tracker.smellsLinked());

        stateManager.saveState(tracker);
        tracker = stateManager.loadState(antlr, v2);

        tracker.track(antlr.getArchitecturalSmellsIn(v3), v3);
        tracker.track(antlr.getArchitecturalSmellsIn(v4), v4);
        assertEquals((long)antlrOracle.get(v4.getVersionString()), tracker.smellsLinked());
    }

    @Test
    void testStatefulnessAntlr() throws IOException, ClassNotFoundException {
        testStatefulness(antlr, antlrOracle);
    }

    @Test
    void testStatefulnessPure() throws IOException, ClassNotFoundException {
        testStatefulness(pure, pureOracle);
    }

    void testStatefulness(IProject project, Map<String, Long> oracle) throws IOException, ClassNotFoundException {
        var tracker = new ASmellTracker(new SimpleNameJaccardSimilarityLinker(), false);
        int nVersions = (int)project.numberOfVersions();

        IntStream.range(1, nVersions/2).forEach(i -> {
            var version = project.getVersionWith(i);
            tracker.track(project.getArchitecturalSmellsIn(version), version);
            assertEquals((long)oracle.get(version.getVersionString()), tracker.smellsLinked());
        });

        var stateManager = new ASmellTrackerStateManager("test-data/output/states");
        stateManager.saveState(tracker);
        var recoveredTracker = stateManager.loadState(project, project.getVersionWith(nVersions/2 - 1));

        IntStream.range(nVersions/2, nVersions).forEach(i -> {
            var version = project.getVersionWith(i);
            recoveredTracker.track(project.getArchitecturalSmellsIn(version), version);
            assertEquals((long)oracle.get(version.getVersionString()), recoveredTracker.smellsLinked(),
                    String.format("Assertion error at i = %d version = %s", i, version.getVersionString()));
        });
    }

}
