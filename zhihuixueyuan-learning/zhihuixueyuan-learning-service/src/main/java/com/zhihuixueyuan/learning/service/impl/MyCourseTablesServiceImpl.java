package com.zhihuixueyuan.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhihuixueyuan.base.exception.ZHXYException;
import com.zhihuixueyuan.base.model.PageResult;
import com.zhihuixueyuan.content.model.po.CoursePublish;
import com.zhihuixueyuan.learning.feignclient.ContentServiceClient;
import com.zhihuixueyuan.learning.mapper.XcChooseCourseMapper;
import com.zhihuixueyuan.learning.mapper.XcCourseTablesMapper;
import com.zhihuixueyuan.learning.model.dto.MyCourseTableParams;
import com.zhihuixueyuan.learning.model.dto.XcChooseCourseDto;
import com.zhihuixueyuan.learning.model.dto.XcCourseTablesDto;
import com.zhihuixueyuan.learning.model.po.XcChooseCourse;
import com.zhihuixueyuan.learning.model.po.XcCourseTables;
import com.zhihuixueyuan.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
/***
 * 选课相关接口实现类
 */
public class MyCourseTablesServiceImpl implements MyCourseTablesService {
    @Autowired
    ContentServiceClient contentServiceClient;
    @Autowired
    XcChooseCourseMapper xcChooseCourseMapper;
    @Autowired
    XcCourseTablesMapper xcCourseTablesMapper;
    /***
     * 添加选课记录（并向课程表添加记录）
     */
    @Override
    @Transactional
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        //远程调用，从课程发布表查询课程
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if(coursepublish==null)
            ZHXYException.cast("课程不存在或服务器繁忙！");
        //获取该课程的收费标准
        String charge = coursepublish.getCharge();
        XcChooseCourse chooseCourse=null;
        if("201000".equals(charge)){
            //添加免费课程
            chooseCourse  = addFreeCoruse(userId, coursepublish);
            //添加到我的课程表
            XcCourseTables xcCourseTables = addCourseTabls(chooseCourse);
        }else{
            //添加收费课程
            chooseCourse  = addChargeCoruse(userId, coursepublish);
        }
        //获取学习资格
        XcCourseTablesDto xcCourseTablesDto = getLearningStatus(userId, courseId);

        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(chooseCourse,xcChooseCourseDto);
        //设置学习资格
        xcChooseCourseDto.setLearnStatus(xcCourseTablesDto.getLearnStatus());

        return xcChooseCourseDto;
    }

    /***
     * 获取学习资格
     */
    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        //先判断课程表是否有记录
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        if(xcCourseTables==null){
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }
        BeanUtils.copyProperties(xcCourseTables,xcCourseTablesDto);
        //是否过期,true过期，false未过期
        boolean isExpires = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        if(!isExpires){
            //正常学习
            xcCourseTablesDto.setLearnStatus("702001");
        }else{
            //已过期
            xcCourseTablesDto.setLearnStatus("702003");
        }
        return xcCourseTablesDto;
    }

    //添加免费课程,免费课程加入选课记录表、我的课程表
    public XcChooseCourse addFreeCoruse(String userId, CoursePublish coursepublish) {
        //查询选课记录表是否存在免费的且选课成功的订单
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getOrderType, "700001") //免费课程
                .eq(XcChooseCourse::getStatus, "701001"); //选课成功
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses != null && !xcChooseCourses.isEmpty()) {
            return xcChooseCourses.get(0);
        }
        //添加选课记录信息
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700001");//免费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus("701001");//选课成功

        xcChooseCourse.setValidDays(365);//免费课程默认365
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        xcChooseCourseMapper.insert(xcChooseCourse);

        return xcChooseCourse;
    }

    //添加收费课程
    public XcChooseCourse addChargeCoruse(String userId,CoursePublish coursepublish){
        //如果存在待支付交易记录直接返回
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getOrderType, "700002")  //收费课程
                .eq(XcChooseCourse::getStatus, "701002");  //待支付
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses != null && !xcChooseCourses.isEmpty()) {
            return xcChooseCourses.get(0);
        }
        //添加选课记录信息
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700001");//免费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus("701001");//选课成功

        xcChooseCourse.setValidDays(365);//免费课程默认365
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        xcChooseCourseMapper.insert(xcChooseCourse);

        return xcChooseCourse;
    }
    //添加到我的课程表
    public XcCourseTables addCourseTabls(XcChooseCourse xcChooseCourse){
        //选课记录完成且未过期才可以添加课程到课程表
        String status = xcChooseCourse.getStatus();
        if (!"701001".equals(status)){
            ZHXYException.cast("选课未成功，无法添加到课程表");
        }
        //判断课程表中是否已有该课程
        String userId = xcChooseCourse.getUserId();
        Long courseId = xcChooseCourse.getCourseId();
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        if(xcCourseTables!=null)
            return xcCourseTables;
        //没有的话则插入课程表
        XcCourseTables xcCourseTablesNew = new XcCourseTables();
        xcCourseTablesNew.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTablesNew.setUserId(xcChooseCourse.getUserId());
        xcCourseTablesNew.setCourseId(xcChooseCourse.getCourseId());
        xcCourseTablesNew.setCompanyId(xcChooseCourse.getCompanyId());
        xcCourseTablesNew.setCourseName(xcChooseCourse.getCourseName());
        xcCourseTablesNew.setCreateDate(LocalDateTime.now());
        xcCourseTablesNew.setValidtimeStart(xcChooseCourse.getValidtimeStart());
        xcCourseTablesNew.setValidtimeEnd(xcChooseCourse.getValidtimeEnd());
        xcCourseTablesNew.setCourseType(xcChooseCourse.getOrderType());
        xcCourseTablesMapper.insert(xcCourseTablesNew);

        return xcCourseTablesNew;
    }

    /**
     * @description 根据课程和用户查询我的课程表中某一门课程
     */
    public XcCourseTables getXcCourseTables(String userId,Long courseId){
        XcCourseTables xcCourseTables = xcCourseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId).eq(XcCourseTables::getCourseId, courseId));
        return xcCourseTables;

    }

    /**
     * 对于付费课更新选课记录，把支付状态改为已支付，同时在课程表添加课程
     * @param chooseCourseId
     * @return
     */
    @Transactional
    @Override
    public boolean saveChooseCourseSuccess(String chooseCourseId) {

        //根据choosecourseId查询选课记录
        XcChooseCourse xcChooseCourse = xcChooseCourseMapper.selectById(chooseCourseId);
        if(xcChooseCourse == null){
            log.debug("收到支付结果通知没有查询到关联的选课记录,choosecourseId:{}",chooseCourseId);
            return false;
        }
        String status = xcChooseCourse.getStatus();
        if("701001".equals(status)){
            //添加到课程表
            addCourseTabls(xcChooseCourse);
            return true;
        }
        //待支付状态才处理
        if ("701002".equals(status)) {
            //更新为选课成功
            xcChooseCourse.setStatus("701001");
            int update = xcChooseCourseMapper.updateById(xcChooseCourse);
            if(update>0){
                log.debug("收到支付结果通知处理成功,选课记录:{}",xcChooseCourse);
                //添加到课程表
                addCourseTabls(xcChooseCourse);
                return true;
            }else{
                log.debug("收到支付结果通知处理失败,选课记录:{}",xcChooseCourse);
                return false;
            }
        }

        return false;
    }

    @Override
    public PageResult<XcCourseTables> mycoursetables(MyCourseTableParams params) {
        //获取分页参数
        int pageNo = params.getPage();
        int pageSize = params.getSize();
        //封装分页查询条件
        LambdaQueryWrapper<XcCourseTables> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcCourseTables::getUserId,params.getUserId());
        //进行分页查询
        Page<XcCourseTables> page = new Page<>();
        Page<XcCourseTables> pageResult = xcCourseTablesMapper.selectPage(page, queryWrapper);
        //封装返回结果
        PageResult<XcCourseTables> courseTablesResult  = new PageResult<>(pageResult.getRecords(),pageResult.getTotal(),pageNo,pageSize);
        return courseTablesResult ;
    }


}
