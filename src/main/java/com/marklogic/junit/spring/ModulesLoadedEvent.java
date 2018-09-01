package com.marklogic.junit.spring;

import java.io.File;
import java.util.Set;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.io.Resource;

/**
 * Spring event that is raised after ModulesLoaderTestExecutionListener is done loading modules. The intent is to give a
 * test class interested in this event a chance to react after any modules have been loaded.
 */
@SuppressWarnings("serial")
public class ModulesLoadedEvent extends ApplicationEvent {

    public ModulesLoadedEvent(Set<Resource> loadedModules) {
        super(loadedModules);
    }

    @SuppressWarnings("unchecked")
    public Set<Resource> getLoadedModules() {
        return (Set<Resource>) this.getSource();
    }
}
