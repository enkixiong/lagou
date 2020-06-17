package engine.ioc.handler;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class AnnotationBeanFactory implements ResourceLoader {

    @Override
    public Resource getResource(String location) {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }
}
