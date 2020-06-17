package engine.ioc;

import java.lang.reflect.Method;
import java.util.List;

public class BeanDefinition {

    private String id;

    private Class<?> type;

    private Object bean;

    private Object factory;
    
    private Method factoryMethod;

    /**
     * 实际上不应该定义在这里; 而是由BeanPostProcessor处理Bean
     *
     * {@link engine.ioc.annotation.Transactional}
     */
    private boolean transactional;

    /**
     * 提前构造依赖关系
     *
     * {@link engine.ioc.annotation.Autowired}
     */
    private List<BeanDefinition> refList;

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

    public List<BeanDefinition> getRefList() {
        return refList;
    }

    public void setRefList(List<BeanDefinition> refList) {
        this.refList = refList;
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
