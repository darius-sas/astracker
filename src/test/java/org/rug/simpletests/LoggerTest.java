package org.rug.simpletests;

import org.apache.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;


public class LoggerTest {

    private static Logger logger = LogManager.getLogger(Logger.class);

    @Test
    void testLoggers(){
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.ALL);
        logger.error("Error message.");
        logger.debug("Debug message.");
        logger.info("Info message.");
        logger.warn("Warning message.");
        logger.fatal("Fatal message.");
    }
}
