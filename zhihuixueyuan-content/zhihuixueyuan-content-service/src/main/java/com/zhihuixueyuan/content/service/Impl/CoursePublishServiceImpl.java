package com.zhihuixueyuan.content.service.Impl;

import com.alibaba.fastjson.JSON;
import com.zhihuixueyuan.base.exception.CommonError;
import com.zhihuixueyuan.base.exception.ZHXYException;
import com.zhihuixueyuan.content.mapper.CourseBaseMapper;
import com.zhihuixueyuan.content.mapper.CourseMarketMapper;
import com.zhihuixueyuan.content.mapper.CoursePublishMapper;
import com.zhihuixueyuan.content.mapper.CoursePublishPreMapper;
import com.zhihuixueyuan.content.model.dto.CourseBaseInfoDto;
import com.zhihuixueyuan.content.model.dto.CoursePreviewDto;
import com.zhihuixueyuan.content.model.dto.TeachplanDto;
import com.zhihuixueyuan.content.model.po.CourseBase;
import com.zhihuixueyuan.content.model.po.CourseMarket;
import com.zhihuixueyuan.content.model.po.CoursePublish;
import com.zhihuixueyuan.content.model.po.CoursePublishPre;
import com.zhihuixueyuan.content.service.CourseBaseInfoService;
import com.zhihuixueyuan.content.service.CoursePublishService;
import com.zhihuixueyuan.content.service.TeachPlanService;
import com.zhihuixueyuan.messagesdk.model.po.MqMessage;
import com.zhihuixueyuan.messagesdk.service.MqMessageService;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CoursePublishServiceImpl implements CoursePublishService {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;
    @Autowired
    TeachPlanService teachPlanService;
    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    CoursePublishMapper coursePublishMapper;
    @Autowired
    MqMessageService mqMessageService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;

    /***
     * 获取课程的预览信息（基本信息+营销信息+师资信息+教学计划）
     * @param courseId 课程id
     * @return
     */
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        if(courseBaseInfo==null)
            ZHXYException.cast("出错啦！课程并不存在");
        List<TeachplanDto> teachplanDtos = teachPlanService.queryTeachPlanTreeNodes(courseId);
        BeanUtils.copyProperties(teachplanDtos,coursePreviewDto);
        BeanUtils.copyProperties(courseBaseInfo,coursePreviewDto);
        return coursePreviewDto;
    }

    /***
     * 提交课程审核
     * @param courseId  课程id
     */
    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {
        //查询课程
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase==null) {
            ZHXYException.cast("未查到该课程！");
        }
        //检查机构是否一致
        if (!courseBase.getCompanyId().equals(companyId)) {
            ZHXYException.cast("只能提交本机构的课程！");
        }

        //检查当前提交状态，如果为“已提交”（202003），则不允许再次提交
        String auditStatus = courseBase.getAuditStatus();
        if("202003".equals(auditStatus)){
            ZHXYException.cast("当前课程的审核已提交，请先等待审核结束");
        }
        //查看该课程的必要信息是否完整（封面图、课程计划）
        if (StringUtils.isEmpty(courseBase.getPic())) {
            ZHXYException.cast("请添加课程封面图");
        }
        List<TeachplanDto> teachplans = teachPlanService.queryTeachPlanTreeNodes(courseId);
        if (teachplans==null||teachplans.size()==0) {
            ZHXYException.cast("请添加课程教学计划");
        }

        //向预发布表插入数据
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //获取课程基本信息加营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);

        //修改课程基本信息表中的审核状态字段
        String teachPlanJSON = JSON.toJSONString(teachplans);
        coursePublishPre.setTeachplan(teachPlanJSON);
        //修改审核状态
        coursePublishPre.setStatus("202003");
        //修改提交审核日期
        coursePublishPre.setCreateDate(LocalDateTime.now());

        //向课程审核表插入数据
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPreUpdate == null){
            //添加课程预发布记录
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本表的审核状态
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    /***
     * 课程发布接口
     * @param companyId 机构id
     * @param courseId 课程id
     */
    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {
        //获取预发布表中的数据
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre==null)
            ZHXYException.cast("请先提交课程审核，审核通过才可以发布");

        //验证机构
        if(!companyId.equals(coursePublishPre.getCompanyId()))
            ZHXYException.cast("不允许提交其它机构的课程。");
        //验证课程审核状态
        String status = coursePublishPre.getStatus();
        if(!"202004".equals(status))
            ZHXYException.cast("操作失败，课程审核通过方可发布。");
        //向课程发布表添加数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        CoursePublish coursePublished = coursePublishMapper.selectById(courseId);
        if(coursePublished==null)
            coursePublishMapper.insert(coursePublish);
        else
            coursePublishMapper.updateById(coursePublish);

        //向消息表添加数据
        saveCoursePublishMessage(courseId);

        //删除课程预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);

    }

    /***
     * 查询课程发布信息
     */
    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        return coursePublishMapper.selectById(courseId);
    }

    /***
     * 从缓存查询课程发布信息
     */
    @Override
    public CoursePublish getCoursePublishCache(Long courseId) {
        //查询缓存
        Object  jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
        if(jsonObj!=null){
            String jsonString = jsonObj.toString();
            if("null".equals(jsonString))
                return null;
            CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
            return coursePublish;
        } else {
            RLock lock = redissonClient.getLock("course:" + courseId);
            lock.lock();
            try{
                //从缓存中查询（第二重校验）
                jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
                if(jsonObj!=null){
                    String jsonString = jsonObj.toString();
                    if("null".equals(jsonString))
                        return null;
                    CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
                    return coursePublish;
                }
                //从数据库查询
                CoursePublish coursePublish = getCoursePublish(courseId);
                redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish),30, TimeUnit.SECONDS);
                return coursePublish;
            }finally {
                lock.unlock();
            }
        }
    }

    private void saveCoursePublishMessage(Long courseId){
        MqMessage coursePublish = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if(coursePublish==null)
            ZHXYException.cast(CommonError.UNKOWN_ERROR);
    }
}
