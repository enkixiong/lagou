import com.lagou.edu.SpringConfig;
import com.lagou.edu.dao.AccountDao;
import com.lagou.edu.service.TransferService;
import engine.ioc.MyApplicationContext;
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
        MyApplicationContext applicationContext = new MyApplicationContext();
        applicationContext.parse(SpringConfig.class);
        applicationContext.getBean(TransferService.class).transfer("6029621011000","6029621011001",100);
    }
}
