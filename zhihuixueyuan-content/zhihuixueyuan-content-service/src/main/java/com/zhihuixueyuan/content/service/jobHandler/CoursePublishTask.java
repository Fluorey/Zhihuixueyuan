package com.zhihuixueyuan.content.service.jobHandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.zhihuixueyuan.base.exception.ZHXYException;
import com.zhihuixueyuan.base.model.CourseIndex;
import com.zhihuixueyuan.content.feignClient.SearchServiceClient;
import com.zhihuixueyuan.content.mapper.CoursePublishMapper;
import com.zhihuixueyuan.content.model.dto.CoursePreviewDto;
import com.zhihuixueyuan.content.model.po.CoursePublish;
import com.zhihuixueyuan.content.service.CoursePublishService;
import com.zhihuixueyuan.messagesdk.model.po.MqMessage;
import com.zhihuixueyuan.messagesdk.service.MessageProcessAbstract;
import com.zhihuixueyuan.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {
    @Autowired
    SearchServiceClient searchServiceClient;
    @Autowired
    CoursePublishService coursePublishService;
    @Autowired
    CoursePublishMapper coursePublishMapper;
    @XxlJob("coursePublishHandler")
    public void coursePublishJobHandler(){
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        process(shardIndex,shardTotal,"course_publish",30,60);
    }

    //课程发布任务处理
    @Override
    public boolean execute(MqMessage mqMessage) {
        //获取消息相关的业务信息
        String businessKey1 = mqMessage.getBusinessKey1();
        long courseId = Integer.parseInt(businessKey1);
        //课程静态化
        generateCourseHtml(mqMessage,courseId);
        //课程索引
        saveCourseIndex(mqMessage,courseId);
        //课程缓存
        //saveCourseCache(mqMessage,courseId);
        return true;
    }


    //生成课程静态化页面并上传至文件系统
    public void generateCourseHtml(MqMessage mqMessage,long courseId){

        log.debug("开始进行课程静态化,课程id:{}",courseId);
        //消息id
        Long id = mqMessage.getId();
        //消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int stageOne = mqMessageService.getStageOne(id);
        if(stageOne >0){
            log.debug("课程静态化已处理直接返回，课程id:{}",courseId);
            return ;
        }
        //进行课程静态化


        //保存第一阶段状态
        mqMessageService.completedStageOne(id);

    }

    //将课程信息缓存至redis
    public void saveCourseCache(MqMessage mqMessage,long courseId){
        log.debug("将课程信息缓存至redis,课程id:{}",courseId);
        //消息id
        Long id = mqMessage.getId();
        //消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int stageTwo = mqMessageService.getStageTwo(id);
        if(stageTwo >0){
            log.debug("课程缓存化已处理直接返回，课程id:{}",courseId);
            return ;
        }
        //进行课程缓存

        //保存第二阶段状态
        mqMessageService.completedStageTwo(id);


    }
    //保存课程索引信息
    public void saveCourseIndex(MqMessage mqMessage,long courseId){
        log.debug("保存课程索引信息,课程id:{}",courseId);
        //消息id
        Long id = mqMessage.getId();
        //消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int stageThree = mqMessageService.getStageThree(id);
        if(stageThree >0){
            log.debug("课程索引已保存，课程id:{}",courseId);
            return ;
        }
        //保存课程索引
        //先查询课程信息
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish,courseIndex);
        //远程调用
        Boolean add = searchServiceClient.add(courseIndex);
        if(!add){
            ZHXYException.cast("远程调用添加课程搜索服务失败！");
        }

        //保存第三阶段状态
        mqMessageService.completedStageThree(id);

    }
}
