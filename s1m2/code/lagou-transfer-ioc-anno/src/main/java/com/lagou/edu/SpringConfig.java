package com.lagou.edu;

import com.alibaba.druid.pool.DruidDataSource;
import engine.ioc.annotation.*;

import javax.sql.DataSource;

@Configuration
@ComponentScan({"com.lagou.edu"})
@Component
@PropertySource({"jdbc.properties"})
public class SpringConfig {

    @Value("${jdbc.driver}")
    private String driverClassName;
    @Value("${jdbc.url}")
    private String url;
    @Value("${jdbc.username}")
    private String username;
    @Value("${jdbc.password}")
    private String password;


    @Bean("dataSource")
    public DataSource createDataSource() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName(driverClassName);
        druidDataSource.setUrl(url);
        druidDataSource.setUsername(username);
        druidDataSource.setPassword(password);
        return druidDataSource;
    }




}
