package com.lagou.test;

import com.lagou.dao.IUserDao;
import com.lagou.io.Resources;
import com.lagou.pojo.User;
import com.lagou.sqlSession.SqlSession;
import com.lagou.sqlSession.SqlSessionFactory;
import com.lagou.sqlSession.SqlSessionFactoryBuilder;
import org.dom4j.DocumentException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyVetoException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class IPersistenceTest {

    private SqlSessionFactory sqlSessionFactory;

    @Test
    public void test() throws Exception {
        before();

        //调用
        User user = new User();
        user.setId(1);
        user.setUsername("张三");
      /*  User user2 = sqlSession.selectOne("user.selectOne", user);

        System.out.println(user2);*/

       /* List<User> users = sqlSession.selectList("user.selectList");
        for (User user1 : users) {
            System.out.println(user1);
        }*/

        SqlSession sqlSession = sqlSessionFactory.openSession();
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        List<User> all = userDao.findAll();
        for (User user1 : all) {
            System.out.println(user1);
        }

    }

    @Test
    public void testAdd() throws Exception {

        //调用
        User user = new User();
        user.setId(5);
        user.setUsername("张三");

        SqlSession sqlSession = sqlSessionFactory.openSession();
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);
        System.out.println("新增数量:"+userDao.insert(user));

        User persistUser = userDao.findByCondition(user);
        Assert.assertNotNull(persistUser);
        Assert.assertEquals(persistUser.getUsername(), "张三");
        user.setUsername("zhangsan");
        System.out.println("更新数量:"+userDao.updateById(user));
        persistUser = userDao.findByCondition(user);
        Assert.assertEquals(persistUser.getUsername(), "zhangsan");

        System.out.println("删除数量:"+userDao.deleteById(user));
        List<User> userList = userDao.findAll();
        for (User persistUser2 : userList) {
            Assert.assertFalse(Objects.equals(persistUser2.getId(),5));
        }

    }

    @Before
    public void before() throws DocumentException, PropertyVetoException {
        InputStream resourceAsSteam = Resources.getResourceAsSteam("sqlMapConfig.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsSteam);
    }


}
