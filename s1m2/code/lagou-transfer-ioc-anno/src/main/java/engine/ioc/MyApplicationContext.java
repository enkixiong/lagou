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

    public void parse(Class<?> config) throws Exception {
        // 获取配置类
        ComponentScan componentScan = config.getAnnotation(ComponentScan.class);
        String[] basePackages = componentScan.value();
        // 将RootBean加入
        BeanDefinition rootBean = createBeanDefinition(config);
        bdList.add(rootBean);
        // 将FactoryBean加入; 这些应该是框架层内置的FactoryBean
        bdList.add(createBeanDefinition(ProxyFactory.class));
        for (String basePackage : basePackages) {
            getResources(basePackage);
        }
        // 获取资源文件:init environment
        PropertySource propertySource = config.getAnnotation(PropertySource.class);
        for (String url : propertySource.value()) {
            Properties properties = new Properties();
            InputStream is = config.getClassLoader().getResourceAsStream(url);
            properties.load(is);
            properties.forEach((k,v) -> {
                environment.put((String) k,v);
            });
        }
        // 先初始化 @Configuration
        createBean(rootBean);
        initIoc();
    }

    /**
     * 加载文件,用TypeFilter处理,获得class对象
     */
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

    /**
     * 获取本程序处理的注解 @Component
     */
    public List<TypeFilter> getIncludeTypeFilterList() {
        List<TypeFilter> typeFilterList = new ArrayList<>();
        typeFilterList.add(new AnnotationTypeFilter(Component.class));
        return typeFilterList;
    }

    /**
     * 创建 BeanDefinition @Component注解
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

    /**
     * 创建 BeanDefinition FactoryBean & @Bean注解
     */
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

    /**
     * 处理类型匹配的问题
     */
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
     * 处理循环依赖的问题
     *
     * 将对象由1级缓存进行 postProcessor 处理之后,加入二级缓存(提前暴露)
     */
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

    /**
     * 主要是用来创建代理类; 对Bean进行增强的操作
     */
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
     * 处理Configuration注解 & Bean注解
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

    // 创建Bean
    public void createBean(BeanDefinition beanDefinition) throws Exception {

        // 如果Bean已经创建，则不再创建
        if (beanDefinition.getBean() != null || level3Cache.containsKey(beanDefinition.getId())) {
            return;
        }
        Object obj;
        if (beanDefinition.getFactoryMethod() != null) {
            // 处理@Bean时的依赖
            Object[] parameters = resolveAutowireParameters(beanDefinition.getFactoryMethod().getGenericParameterTypes());
            obj = beanDefinition.getFactoryMethod().invoke(beanDefinition.getFactory(),parameters);
        }else{
            // 处理构造函数的依赖
            Class<?> clazz = beanDefinition.getType();
            Constructor<?> constructor = clazz.getConstructors()[0];
            Object[] parameters = resolveAutowireParameters(constructor.getGenericParameterTypes());
            obj = constructor.newInstance(parameters);
        }
        beanDefinition.setBean(obj);
        // 加入一级缓存
        level1Cache.put(beanDefinition.getId(),beanDefinition);

        // 对配置类进行加载处理
        postProcessorWithConfiguration(beanDefinition);
        // populate bean : 设置属性&占位符 @Value & @Autowire
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
                // 处理Bean依赖
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

    /**
     * 获取依赖
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

    public Object[] resolveAutowireParameters(Type[] genericParameterTypes) throws Exception {
//        Type[] genericParameterTypes = constructor.getGenericParameterTypes();
        Object[] parameters = new Object[genericParameterTypes.length];
        for (int i = 0; i < genericParameterTypes.length; i++) {
            Object ref = resolveRef(Class.forName(genericParameterTypes[i].getTypeName()));
            parameters[i] = ref;
        }
        return parameters;
    }

    /**
     * Ioc容器启动后
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType){
        for (BeanDefinition beanDefinition : level3Cache.values()) {
            if (isTypeMatch(beanDefinition.getType(), requiredType)){
                return (T)beanDefinition.getBean();
            }
        }
        return null;
    }

    /**
     * Ioc容器启动前
     */
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
