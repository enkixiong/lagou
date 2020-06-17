package engine.ioc.annotation;

import org.springframework.beans.factory.FactoryBean;

public class FactoryBeanAdapter<T> implements FactoryBean<T> {



    @Override
    public T getObject() throws Exception {
        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
