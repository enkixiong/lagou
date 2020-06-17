package engine.ioc;

import java.lang.reflect.Method;
import java.util.List;

public class BeanDefinition {

    // Bean的名称
    private String id;

    // Bean的类型
    private Class<?> type;

    // Bean对象
    private Object bean;

    // @Bean的configuration类
    private Object factory;

    // @Bean的Method
    private Method factoryMethod;

    /**
     * 实际上不应该定义在这里; 而是由BeanPostProcessor处理Bean
     *
     * {@link engine.ioc.annotation.Transactional}
     */
    private boolean transactional;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public boolean isTransactional() {
        return transactional;
    }

    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }

    public Object getFactory() {
        return factory;
    }

    public void setFactory(Object factory) {
        this.factory = factory;
    }

    public Method getFactoryMethod() {
        return factoryMethod;
    }

    public void setFactoryMethod(Method factoryMethod) {
        this.factoryMethod = factoryMethod;
    }

    @Override
    public String toString() {
        return "BeanDefinition{" +
                "id='" + id + '\'' +
                ", type=" + type +
                '}';
    }
}
