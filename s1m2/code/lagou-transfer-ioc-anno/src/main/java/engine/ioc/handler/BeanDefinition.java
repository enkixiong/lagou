package engine.ioc.handler;

import java.lang.reflect.Method;

public class BeanDefinition {


    private String name;

    private Class<?> type;

    private boolean singleton;

    private boolean lazy;

    private String factoryBeanName;

    private Class<?> factoryType;

    private Method factoryMethod;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public boolean isLazy() {
        return lazy;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public Class<?> getFactoryType() {
        return factoryType;
    }

    public void setFactoryType(Class<?> factoryType) {
        this.factoryType = factoryType;
    }

    public Method getFactoryMethod() {
        return factoryMethod;
    }

    public void setFactoryMethod(Method factoryMethod) {
        this.factoryMethod = factoryMethod;
    }
}
