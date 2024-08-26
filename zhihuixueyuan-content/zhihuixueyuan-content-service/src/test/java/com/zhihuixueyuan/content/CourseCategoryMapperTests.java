package com.zhihuixueyuan.content;

import com.zhihuixueyuan.content.mapper.CourseCategoryMapper;
import com.zhihuixueyuan.content.model.dto.CourseCategoryTreeDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
public class CourseCategoryMapperTests {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Test
    public void testCourseCategoryMapper(){
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes("1-1");
        System.out.println("courseCategoryMapper = " + courseCategoryMapper);
    }
}
