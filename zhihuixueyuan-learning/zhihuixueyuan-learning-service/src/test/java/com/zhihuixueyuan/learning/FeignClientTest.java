package com.zhihuixueyuan.learning;

import com.zhihuixueyuan.content.model.po.CoursePublish;
import com.zhihuixueyuan.learning.feignclient.ContentServiceClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @description TODO
 */
@SpringBootTest
public class FeignClientTest {

    @Autowired
    ContentServiceClient contentServiceClient;


    @Test
    public void testContentServiceClient() {
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(18L);
        Assertions.assertNotNull(coursepublish);
    }
}