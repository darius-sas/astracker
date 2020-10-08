package org.rug.simpletests.args;

import com.beust.jcommander.JCommander;
import org.junit.jupiter.api.Test;
import org.rug.args.Args;

import static org.junit.jupiter.api.Assertions.*;

class ArgsTest {
    @Test
    void shouldFindAllOptions() {
        Args args = new Args();
        String[] argsArr = "-o test -p project -i test -rA test -dRT -jP -rS -rF -sAO -pS -pC -pCC -tNCS 1 -v -sv -branch master -startDate 123 -nDays 3".split(" ");
        JCommander jc = JCommander.newBuilder().addObject(args).build();
        jc.setProgramName("hello");
        jc.parse(argsArr);
        args.setGitRepo(null);
        assertEquals("test", args.outputDir.toString());
        assertEquals("project", args.project.name);
        assertEquals("test", args.inputDirectory.toString());
        assertEquals("test", args.runArcan);
        assertTrue(args.runArcan());
        assertFalse(args.runTracker());
        assertTrue(args.isJavaProject());
        assertTrue(args.runProjectSizes());
        assertTrue(args.runFanInFanOutCounter());
        assertTrue(args.showArcanOutput);
        assertTrue(args.smellCharacteristics);
        assertTrue(args.similarityScores);
        assertTrue(args.componentCharacteristics);
        assertFalse(args.isGitProject());
        assertTrue(args.shouldAnalyseSingleVersion());
        assertNull(args.getGitRepo());
        assertNotNull(args.getArcanJarFile());
        assertFalse(args.getSimilarityScoreFile().isEmpty());
        assertFalse(args.getSmellCharacteristicsFile().isEmpty());
        assertFalse(args.getAffectedComponentsFile().isEmpty());
        assertFalse(args.getCondensedGraphFile().isEmpty());
        assertFalse(args.getTrackGraphFileName().isEmpty());
        assertFalse(args.getProjectSizesFile().isEmpty());
        assertFalse(args.getFanInFanOutFile().isEmpty());
        assertFalse(args.getHomeProjectDirectory().isEmpty());
        assertEquals("master", args.branch);
        assertEquals(3, args.nDays);
        assertEquals("123", args.startDate);
        assertThrows(IllegalArgumentException.class, args::adjustProjDirToArcanOutput);
    }
}