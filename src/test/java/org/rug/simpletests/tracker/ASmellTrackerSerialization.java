package org.rug.simpletests.tracker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.rug.tracker.ASmellTracker;
import org.rug.tracker.SimpleNameJaccardSimilarityLinker;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.rug.simpletests.TestData.antlr;
import static org.rug.simpletests.TestData.pure;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ASmellTrackerSerialization{

    protected Map<String, Long> antlrOracle;
    protected Map<String, Long> pureOracle;

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
        oracle = new long[]{ 0, 997, 108, 1040, 109};
        i = 0;
        for (var v : pure){
            pureOracle.put(v.getVersionString(), oracle[i++]);
        }
    }

    @Test
    void testSerialization() throws IOException, ClassNotFoundException {
        ASmellTracker smellTracker = new ASmellTracker(new SimpleNameJaccardSimilarityLinker(), false);
        var v1 = antlr.getVersion("3.1");
        var v2 = antlr.getVersion("3.2");
        var v3 = antlr.getVersion("3.3");
        var v4 = antlr.getVersion("3.4");

        smellTracker.track(antlr.getArchitecturalSmellsIn(v1), v1);
        smellTracker.track(antlr.getArchitecturalSmellsIn(v2), v2);
        //assertEquals(antlrOracle.get(v2.getVersionString()), smellTracker.smellsLinked());

        var serFile = new File("astracker.seo");
        serFile.deleteOnExit();

        var outfs = new FileOutputStream(serFile);
        var objos = new ObjectOutputStream(outfs);

        objos.writeObject(smellTracker);
        objos.flush();
        objos.close();

        var infs = new FileInputStream(serFile);
        var objin = new ObjectInputStream(infs);
        ASmellTracker serializedSmellTracker = (ASmellTracker)objin.readObject();
        objin.close();
        assertNotNull(serializedSmellTracker);
    }
}
