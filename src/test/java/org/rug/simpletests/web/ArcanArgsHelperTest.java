package org.rug.simpletests.web;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.rug.args.Args;
import org.rug.web.helpers.ArcanArgsHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ArcanArgsHelperTest {

    private static final Path inputDir = Paths.get("./test-data/git-projects");
    private static final Path outputDir = Paths.get("./test-data/output");
    private static final Path arcanOutput = Paths.get("./test-data/output/arcanOutput/pyne");
    private ArcanArgsHelper arcanArgsHelper;
    private Args args;

    @BeforeAll
    void init(){
        this.arcanArgsHelper = new ArcanArgsHelper();
        this.args = new Args();
        this.args.project.name = "pyne";
        this.args.setGitRepo(Paths.get(String.valueOf(inputDir), "pyne").toFile());
        this.args.outputDir = outputDir.toFile();
    }

    @Test
    void testGetArguments() {
        var expectedString = String.format(
                "-git -p %s -out %s -branch master -startDate 1-1-1 -nWeeks 2",
                Paths.get(String.valueOf(inputDir), "pyne").toAbsolutePath(),
                arcanOutput.toAbsolutePath()
        );

        assertEquals(expectedString, arcanArgsHelper.getArguments(args));
    }

    @Test
    void testGetSingleVersionArguments() throws IOException, GitAPIException {
        Git.open(Paths.get(String.valueOf(inputDir), "pyne").toAbsolutePath().toFile())
                .checkout()
                .setName("072567a0f448c891053d2c418596c200827a895f")
                .call();
        var versionId = String.format(
                "1-%s-%s",
                "27_10_2019",
                "072567a0f448c891053d2c418596c200827a895f"
        );
        this.args.singleVersion = true;

        var expectedString = String.format(
                " -versionId %s -p %s -out %s -branch master",
                versionId,
                Paths.get(String.valueOf(inputDir), "pyne").toAbsolutePath(),
                arcanOutput.toAbsolutePath()
        );

        assertEquals(
                expectedString,
                arcanArgsHelper.getSingleVersionArguments(this.args)
        );
    }
}