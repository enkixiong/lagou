### 自定义 IOC 容器

#### 1. 自定义注解

    @ComponentScan : 定义在哪里找Bean对象

    @Service 定义好的Bean
    
    @Component 定义好的组件
    
    @Bean&@Configuration 自定义Bean
    
    @EnableXXX Import

    @Transactional 事务

    @Qualified : 注入时ByName注入
    @Autowired : 依赖注入
    
    @Value : PlaceHolder的处理
    
#### 2. 执行思路

- 自定义 `ContextLoaderListener`
    
- 自定义 `BeanDefinition`

- 自定义`ApplicationContext` `MyApplicationContext implements ApplicationContext`

- 自定义Bean`创建`过程 `Spring的原有过程以及Bean的含义，Copy实现一遍`

- 自定义`循环依赖`解析过程

- 自定义`依赖注入`

- 自定义AOP代理 `事务控制`

##### 2.1 自定义 ContextLoaderListener

##### 2.2 自定义 BeanDefinition

##### 2.3 自定义 ApplicationContext

##### 2.4 自定义Bean创建过程

##### 2.5 自定义循环依赖解析过程 & 依赖注入

##### 2.6 自定义事务控制

##### 2.7 `FactoryBean` `@Configuration` `@Import` `selectImports` `@EnableXXX` `@Bean`

##### 2.8 `PlaceHolder`

#### 3. 




