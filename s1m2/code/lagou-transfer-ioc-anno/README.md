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

- 自定义 `ContextLoaderListener`

    `在Servlet中,Init时,设置transferService;
    `
        
    
- 自定义 `BeanDefinition`

- 自定义`ApplicationContext` `MyApplicationContext`

- 自定义Bean`创建`过程 `Spring的原有过程以及Bean的含义，Copy实现一遍`

- 自定义`循环依赖`解析过程

- 自定义`依赖注入`

- 自定义AOP代理 `事务控制`





