package org.rug.simpletests.web;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.rug.web.credentials.Credentials;
import org.rug.web.helpers.RemoteProjectFetcher;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RemoteProjectFetcherTest {

    private static final Path clonedReposDirectory = Paths.get("./test-data/cloned-projects");
    private RemoteProjectFetcher remoteProjectFetcher;

    @BeforeAll
    void init(){
        this.remoteProjectFetcher = new RemoteProjectFetcher(clonedReposDirectory);
    }

    @Test
    void testGetProjectPath() {
        var expectedPath = Paths.get(clonedReposDirectory.toAbsolutePath().toString(), "pyne");
        Credentials credentials = Credentials.noCredentials();
        assertEquals(
                expectedPath,
                remoteProjectFetcher.fetchProject("https://github.com/darius-sas/pyne.git", credentials)
        );
        assertEquals(
                expectedPath,
                remoteProjectFetcher.fetchProject("pyne", credentials)
        );
        assertNotEquals(
                expectedPath,
                remoteProjectFetcher.fetchProject("github.com/darius-sas/pyne.git", credentials)
        );
    }

    @Test
    void testCheckIfAlreadyCloned() {
        assertTrue(remoteProjectFetcher.checkIfAlreadyCloned(new File(clonedReposDirectory.toString()+"/pyne")));
        assertFalse(remoteProjectFetcher.checkIfAlreadyCloned(new File(clonedReposDirectory.toString()+"/random")));
    }

    @Test
    void testGetProjectName() {
        assertEquals("pyne" , remoteProjectFetcher.getProjectName("https://github.com/darius-sas/pyne.git"));
        assertEquals("pyne", remoteProjectFetcher.getProjectName("pyne"));
    }

    @Test
    void testIsValidGitLink() {
        assertTrue(remoteProjectFetcher.isValidGitLink("https://github.com/darius-sas/pyne.git"));
        assertFalse(remoteProjectFetcher.isValidGitLink("github.com/darius-sas/pyne.git"));
        assertFalse(remoteProjectFetcher.isValidGitLink("Invalidlink"));
        assertFalse(remoteProjectFetcher.isValidGitLink(""));
    }
}