package org.rug.simpletests.web.helpers;

import org.junit.jupiter.api.Test;
import org.rug.web.helpers.RemoteProjectFetcher;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class RemoteProjectFetcherTest {

    @Test
    void shouldParseLinksProperly() {
        RemoteProjectFetcher f = new RemoteProjectFetcher(Paths.get("test-data/"));
        assertTrue(f.isValidGitLink("https://github.com/digeo/TDConfession"));
        assertTrue(f.isValidGitLink("https://github.com/digeo/TDConfession.git"));
        assertEquals("TDConfession", f.getProjectName("https://github.com/digeo/TDConfession.git"));
        assertEquals("TDConfession", f.getProjectName("https://github.com/digeo/TDConfession"));
    }
}