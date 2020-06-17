package engine.ioc;


import com.lagou.edu.SpringConfig;
import com.lagou.edu.dao.AccountDao;
import engine.aop.ProxyFactory;
import com.lagou.edu.service.TransferService;
import engine.ioc.annotation.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MyApplicationContext {

    private static final Log logger = LogFactory.getLog(MyApplicationContext.class);

    private final Map<String,Object> environment = new HashMap<>();

    private final List<BeanDefinition> bdList = new ArrayList<>();

    private final Map<String,BeanDefinition> level1Cache = new ConcurrentHashMap<>();

    private final Map<String,BeanDefinition> level2Cache = new ConcurrentHashMap<>();

    private final Map<String,BeanDefinition> level3Cache = new ConcurrentHashMap<>();

//    private final ProxyFactory proxyFactory = new ProxyFactory();

    public void parse(Class<?> config) throws Exception {
        ComponentScan componentScan = config.getAnnotation(ComponentScan.class);
        String[] basePackages = componentScan.value();
        BeanDefinition rootBean = createBeanDefinition(config);
        bdList.add(rootBean);
        bdList.add(createBeanDefinition(ProxyFactory.class));
        for (String basePackage : basePackages) {
            getResources(basePackage);
        }
        // init environment
        PropertySource propertySource = config.getAnnotation(PropertySource.class);
        for (String url : propertySource.value()) {
            Properties properties = new Properties();
            InputStream is = config.getClassLoader().getResourceAsStream(url);
            properties.load(is);
            properties.forEach((k,v) -> {
                environment.put((String) k,v);
            });
        }
        createBean(rootBean);
        initIoc();
    }

    public void getResources(String basePackage) throws Exception {
        SimpleMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
        basePackage = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                ClassUtils.convertClassNameToResourcePath(basePackage) + '/' + "**/*.class";
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();


        List<TypeFilter> typeFilterList = getIncludeTypeFilterList();
        for (Resource resource : resourcePatternResolver.getResources(basePackage)) {
            try {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
                String className = sbd.getMetadata().getClassName();
                Class<?> clazz = Class.forName(className);
                for (TypeFilter typeFilter : typeFilterList) {
                    if (typeFilter.match(metadataReader, metadataReaderFactory)) {
                        BeanDefinition bd = createBeanDefinition(clazz);
                        // 检测Bean定义重复
                        if (!containsBean(bd)) {
                            bdList.add(bd);
                        }
                        break;
                    }
                }
            } catch (Throwable e) {
                logger.error("创建Bean产生异常!",e);
            }
        }
    }

    public List<TypeFilter> getIncludeTypeFilterList() {
        List<TypeFilter> typeFilterList = new ArrayList<>();
        typeFilterList.add(new AnnotationTypeFilter(Component.class));
        return typeFilterList;
    }

    /**
     * 创建 BeanDefinition
     */
    private BeanDefinition createBeanDefinition(Class<?> clazz) {
        BeanDefinition beanDefinition = new BeanDefinition();
        Component component = clazz.getAnnotation(Component.class);
        String beanName = clazz.getName().substring(0, 1).toLowerCase() + clazz.getName().substring(1);
        if (component != null && !StringUtils.isEmpty(component.value())) {
            beanName = component.value();
        }
        beanDefinition.setType(clazz);
        beanDefinition.setId(beanName);
        return beanDefinition;
    }

    private BeanDefinition createBeanDefinition(Object obj, Method method){
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setId(method.getName());
        beanDefinition.setType(method.getReturnType());
        beanDefinition.setFactory(obj);
        beanDefinition.setFactoryMethod(method);
        return beanDefinition;
    }


    /**
     * 解决依赖
     */
    private Object resolveRef(Class<?> clazz) throws Exception {
        for (BeanDefinition beanDefinition : level3Cache.values()) {
            if (isTypeMatch(beanDefinition.getType(), clazz)) {
                return beanDefinition.getBean();
            }
        }
        for (BeanDefinition beanDefinition : level2Cache.values()) {
            if (isTypeMatch(beanDefinition.getType(), clazz)) {
                return beanDefinition;
            }
        }
        for (BeanDefinition beanDefinition : level1Cache.values()) {
            if (isTypeMatch(beanDefinition.getType(), clazz)) {
                createBean(beanDefinition);
                return beanDefinition.getBean();
            }
        }
        throw new RuntimeException("未找到依赖的Bean: " + clazz.getSimpleName());
    }

    public boolean isTypeMatch(Class<?> beanClazz, Class<?> interfaceClazz) {
        if (beanClazz == null){
            return false;
        }
        if (Object.class.equals(beanClazz)){
            return false;
        }
        if (beanClazz.equals(interfaceClazz)){
            return true;
        }
        for (Class<?> anInterface : beanClazz.getInterfaces()) {
            if (anInterface.equals(interfaceClazz)){
                return true;
            }
        }
        return isTypeMatch(beanClazz.getSuperclass(),interfaceClazz);
    }

    /**
     * 解决依赖
     */
    private BeanDefinition resolveInitRef(Class<?> clazz) throws Exception {
        for (BeanDefinition beanDefinition : level3Cache.values()) {
            if (isTypeMatch(beanDefinition.getType(), clazz)) {
                return beanDefinition;
            }
        }
        for (BeanDefinition beanDefinition : level2Cache.values()) {
            if (isTypeMatch(beanDefinition.getType(), clazz)) {
                return beanDefinition;
            }
        }
        for (BeanDefinition beanDefinition : level1Cache.values()) {
            if (isTypeMatch(beanDefinition.getType(), clazz)) {
                getEarlyReference(beanDefinition);
                return beanDefinition;
            }
        }
        for (BeanDefinition beanDefinition : bdList) {
            if (isTypeMatch(beanDefinition.getType(), clazz)){
                createBean(beanDefinition);
                return beanDefinition;
            }
        }
        throw new RuntimeException("未找到依赖的Bean: " + clazz.getSimpleName());
    }

    private void getEarlyReference(BeanDefinition beanDefinition){
        postProcessorAfterInit(beanDefinition);
    }

    // 实例化容器
    private void initIoc() throws Exception {

        // 创建Bean;  1) FactoryMethod @Bean 2)无参构造器 3)有参构造器
        for (BeanDefinition beanDefinition :bdList) {
            createBean(beanDefinition);
        }
        level1Cache.clear();
        level2Cache.clear();
    }

    private void postProcessorAfterInit(BeanDefinition beanDefinition) {
        Class<?> clazz = beanDefinition.getType();
        boolean isTransactional = false;
        for (Method method : clazz.getMethods()) {
            if (method.getAnnotation(Transactional.class) != null){
                isTransactional = true;
                break;
            }
        }
        beanDefinition.setTransactional(isTransactional);
        if (beanDefinition.isTransactional()) {
            beanDefinition.setBean(getBeanInner(ProxyFactory.class).getCglibProxy(beanDefinition.getBean()));
        }
        level2Cache.put(beanDefinition.getId(), beanDefinition);
    }


    /**
     * 处理Configuration标签
     * @param beanDefinition: Bean定义对象
     */
    private void postProcessorWithConfiguration(BeanDefinition beanDefinition){
        if (beanDefinition.getType().getAnnotation(Configuration.class) == null){
            return;
        }
        for (Method method : beanDefinition.getType().getMethods()) {
            if (method.getAnnotation(Bean.class) != null){
                bdList.add(createBeanDefinition(beanDefinition.getBean(), method));
            }
        }
    }


    public void createBean(BeanDefinition beanDefinition) throws Exception {
        if (beanDefinition.getBean() != null || level3Cache.containsKey(beanDefinition.getId())) {
            return;
        }
        Object obj;
        if (beanDefinition.getFactoryMethod() != null) {
            Object[] parameters = resolveAutowireParameters(beanDefinition.getFactoryMethod().getGenericParameterTypes());
            obj = beanDefinition.getFactoryMethod().invoke(beanDefinition.getFactory(),parameters);
        }else{
            Class<?> clazz = beanDefinition.getType();
            Constructor<?> constructor = clazz.getConstructors()[0];
            Object[] parameters = resolveAutowireParameters(constructor.getGenericParameterTypes());
            obj = constructor.newInstance(parameters);
        }
        beanDefinition.setBean(obj);
        level1Cache.put(beanDefinition.getId(),beanDefinition);

        // enable filter
        postProcessorWithConfiguration(beanDefinition);
        // populate bean
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {

            // @Value
            Value value = field.getAnnotation(Value.class);
            if (null != value && value.value().startsWith("${") && value.value().endsWith("}")){
                String key = value.value().replace("${","").replace("}","");
                Object fieldValue = environment.get(key);
                field.setAccessible(true);
                field.set(obj, fieldValue);
            }

            // @Autowire
            if (field.getAnnotation(Autowired.class) != null) {
                BeanDefinition refBeanDefinition = resolveInitRef(field.getType());
                if (refBeanDefinition.getBean() == null) {
                    createBean(refBeanDefinition);
                }
                field.setAccessible(true);
                field.set(obj, refBeanDefinition.getBean());
            }
        }
        if (!level2Cache.containsKey(beanDefinition.getId())) {
            getEarlyReference(beanDefinition);
            level2Cache.put(beanDefinition.getId(),beanDefinition);
        }
//        postProcessor(beanDefinition);
        level3Cache.put(beanDefinition.getId(),beanDefinition);
    }

    public Object[] resolveAutowireParameters(Type[] genericParameterTypes) throws Exception {
//        Type[] genericParameterTypes = constructor.getGenericParameterTypes();
        Object[] parameters = new Object[genericParameterTypes.length];
        for (int i = 0; i < genericParameterTypes.length; i++) {
            Object ref = resolveRef(Class.forName(genericParameterTypes[i].getTypeName()));
            parameters[i] = ref;
        }
        return parameters;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType){
        for (BeanDefinition beanDefinition : level3Cache.values()) {
            if (isTypeMatch(beanDefinition.getType(), requiredType)){
                return (T)beanDefinition.getBean();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T getBeanInner(Class<T> requiredType){
        try {
            return (T) resolveInitRef(requiredType).getBean();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private boolean containsBean(BeanDefinition bd){
        for (BeanDefinition beanDefinition : bdList) {
            if (beanDefinition.getId().equals(bd.getId())){
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        MyApplicationContext applicationContext = new MyApplicationContext();
        applicationContext.parse(SpringConfig.class);
        System.out.println(applicationContext.getBean(AccountDao.class).getClass());
        System.out.println(applicationContext.getBean(TransferService.class).getClass());
    }
}
