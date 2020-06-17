## 实现思路

### 框架层: 更改SimpleExecutor方法

    * 新增update方法
    * 参考query方法，设置参数
    * 执行 executeUpdate()方法,并返回int值,表示DB中影响的行数

### 框架层:修改DefaultSqlSession类

    * 新增doUpdate方法, 调用BaseExecutor中的 update 方法 
    * 在getMapper方法中,新增对 update/delete/insert 三个方法的代理，并且优先判断;
    * 判断成功后，调用 update 方法,并且提前返回  

### 框架层:修改XMLMapperBuilder类

    * 新增对 insert/update/delete 标签的解析

### 新增User表
`  create table user(
       id integer,
       username varchar(100)
   );
`

### 修改数据源

    * 修改配置文件,获取正确的数据源
    * mysql8 驱动: com.mysql.cj.jdbc.Driver 
    
### Mapper文件修改

    * 新增update/delete/insert 方法
    * 传入参数都是User对象
    * 返参都是 int 
    * 无 parameterType
    
### 修改IUserDao接口

    * 增加 insert/update/delete 方法
    * 参数与Mapper文件保持一致
    * 方法名与Mapper文件保持一致
    
### 新增测试方法 **testAdd**


### 疑问

    * 无事务控制
    * DataSource.getConnection() 无法指定是否开启事务；connection 也无法与当前线程进行绑定~
    * 如何实现跨线程的事务？
    * 如何实现跨线程事务的日志追踪？
    * c3p0 & druid 的优劣? 参数配置? 一个错误的参数，会导致哪些影响？
    * 实际场景中, Mybatis 1对多,多对多 在pojo层维护映射的场景多吗？ 当前使用Mybatis的最佳实践是怎样的? 


