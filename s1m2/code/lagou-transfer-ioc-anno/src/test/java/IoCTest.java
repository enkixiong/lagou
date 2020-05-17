import com.lagou.edu.SpringConfig;
import com.lagou.edu.dao.AccountDao;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.sql.DataSource;

/**
 * @author 应癫
 */
public class IoCTest {


    @Test
    public void testIoC() throws Exception {

        // 通过读取classpath下的xml文件来启动容器（xml模式SE应用下推荐）
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfig.class);

        DataSource dataSource = (DataSource) applicationContext.getBean("dataSource");

        System.out.println(dataSource.getConnection());



    }
}
