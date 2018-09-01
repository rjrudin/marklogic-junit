package com.marklogic.junit.spring;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.ext.helper.DatabaseClientProvider;
import com.marklogic.client.ext.modulesloader.ModulesLoader;
import com.marklogic.client.ext.modulesloader.impl.DefaultModulesFinder;

/**
 * Used to automatically load new/modified modules before a test runs.
 */
public class ModulesLoaderTestExecutionListener extends AbstractTestExecutionListener {

    private static boolean initialized = false;
    private final static Logger logger = LoggerFactory.getLogger(ModulesLoaderTestExecutionListener.class);

    /**
     * This currently only runs once; the thought is that an application will have an a base test class that defines the
     * module loaders for all subclasses. Could easily modify this to instead keep track of which directories it has
     * loaded already.
     */
    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        if (!initialized) {
            List<ModulesPath> pathList = null;

            ModulesPaths paths = testContext.getTestClass().getAnnotation(ModulesPaths.class);
            if (paths != null) {
                pathList = Arrays.asList(paths.paths());
            } else {
                ModulesPath path = testContext.getTestClass().getAnnotation(ModulesPath.class);
                if (path != null) {
                    pathList = Arrays.asList(path);
                }
            }

            if (pathList != null) {
                ModulesLoader modulesLoader = getModulesLoader(testContext);

                DatabaseClientProvider p = testContext.getApplicationContext().getBean(DatabaseClientProvider.class);
                DatabaseClient client = p.getDatabaseClient();

                for (ModulesPath loader : pathList) {
                    String baseDir = loader.baseDir();
                    if (logger.isInfoEnabled()) {
                        logger.info(String.format("Loading modules, using base directory of %s", baseDir));
                    }
                    Set<Resource> loadedModules = modulesLoader.loadModules(baseDir, new DefaultModulesFinder(),
                            client);
                    if (loadedModules != null) {
                        testContext.getApplicationContext().publishEvent(new ModulesLoadedEvent(loadedModules));
                    }
                }
                initialized = true;
            }
        }
    }

    /**
     * Assumes that a ModulesLoader is present in the Spring container; can be overridden in a subclass to build one in
     * some other fashion
     * 
     * @param testContext
     * @return
     */
    protected ModulesLoader getModulesLoader(TestContext testContext) {
        return testContext.getApplicationContext().getBean(ModulesLoader.class);
    }
}
