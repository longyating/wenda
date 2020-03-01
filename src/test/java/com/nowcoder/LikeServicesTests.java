package com.nowcoder;

import com.nowcoder.service.LikeService;
import org.junit.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WendaApplication.class)
public class LikeServicesTests {
    @Autowired
    LikeService likeService;

    @Before  //如果有数据要初始化，就放在这里
    public void setUp(){
        System.out.println("setUp");
    }

    @After  //清理工作，删掉测试产生的数据
    public void tearDown(){
        System.out.println("tearDown");
    }

    //@BeforeClass 和 @AfterClass 只会执行一次，所以如果有重复性的before after 放在这里
    @BeforeClass //如果所有的测试用例都要的初始化数据
    public static void beforeClass(){
        System.out.println("beforeClass");
    }

    @AfterClass
    public static void afterClass(){
        System.out.println("afterClass");
    }


    @Test  //test 注解
    public void testLike(){
        System.out.println("testLike");
        likeService.like(123,1,1);
        Assert.assertEquals(1,likeService.getLikeStatus(123,1,1));
    }

    @Test
    public void testXXX(){
        System.out.println("testXXX");
    }

    //异常测试，即本来应该得到某个异常的却没有得到，就会报错
    @Test(expected = IllegalArgumentException.class)
    public void testException(){
        System.out.println("testException");
        throw new IllegalArgumentException("异常发生了"); //如果没这一句异常就会报错
    }

}
