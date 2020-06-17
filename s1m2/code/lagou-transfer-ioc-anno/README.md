### 自定义 IOC 容器

#### 1. 自定义注解

    @ComponentScan : 定义在哪里找Bean对象

    @Service 定义好的Bean
    
    @Component 定义好的组件
    
    @Bean&@Configuration 自定义Bean

    @Transactional 事务

    @Autowired : 依赖注入
    
    @Value : PlaceHolder的处理
    
    @PropertySource : 资源文件位置,用来生成Environment
    
    实现了Ioc容器的雏形
    
未实现的功能: 

    1. propertySource的 Ordered功能
    2. 真正的三级缓存
    
    
#### 2. 执行思路

    1. 解析启动类
    2. 解析@ComponentScan : 扫描class对象(TypeFilter:@Component)
    3. 创建BeanDefinition
    4. 解析@propertySource资源文件; 构造Environment;
    5. 创建启动类BeanDefinition;
    6. 容器处理RootBean; @Configuration时，需要处理@Bean标签
    7. 实例化所有的Bean
        7.1 实例化Bean
        7.2 将对象放入一级缓存
        7.3 @Value处理
        7.4 @Autowired注解处理, 在这里可能会出现循环依赖
        7.5 处理postProcessor(代理类等) getRealyRefrence(循环依赖的解决方案)
        7.6 将对象放入二级缓存 
        7.7 postProcessor
        7.8 将对象转移到三级缓存
    8. 容器启动完成

- 自定义 `ContextLoaderListener`

    `在Servlet中,Init时,设置transferService;
    `
        
    
- 自定义 `BeanDefinition`

- 自定义`ApplicationContext` `MyApplicationContext`

- 自定义Bean`创建`过程 `Spring的原有过程以及Bean的含义，Copy实现一遍`

- 自定义`循环依赖`解析过程

- 自定义`依赖注入`

- 自定义AOP代理 `事务控制`





