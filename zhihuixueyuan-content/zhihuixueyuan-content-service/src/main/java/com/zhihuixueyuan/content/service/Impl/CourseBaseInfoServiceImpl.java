package com.zhihuixueyuan.content.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhihuixueyuan.base.exception.ZHXYException;
import com.zhihuixueyuan.base.model.PageParams;
import com.zhihuixueyuan.base.model.PageResult;
import com.zhihuixueyuan.content.mapper.CourseBaseMapper;
import com.zhihuixueyuan.content.mapper.CourseCategoryMapper;
import com.zhihuixueyuan.content.mapper.CourseMarketMapper;
import com.zhihuixueyuan.content.model.dto.AddCourseDto;
import com.zhihuixueyuan.content.model.dto.CourseBaseInfoDto;
import com.zhihuixueyuan.content.model.dto.EditCourseDto;
import com.zhihuixueyuan.content.model.dto.QueryCourseParamsDto;
import com.zhihuixueyuan.content.model.po.CourseBase;
import com.zhihuixueyuan.content.model.po.CourseMarket;
import com.zhihuixueyuan.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/***
 * 课程信息管理接口的实现类
 */
@Service
@Slf4j
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    /***
     * 分页查询课程
     * @param pageParams 分页参数
     * @param queryCourseParamsDto 查询条件
     * @return 包装了课程信息的pageResult，包含了分页信息
     */
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {

        //封装查询参数
        LambdaQueryWrapper<CourseBase> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName());
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus());
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());

        //创建MP的分页参数对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //进行分页查询
        Page<CourseBase> pageRes = courseBaseMapper.selectPage(page, queryWrapper);
        //封装查询结果
        PageResult<CourseBase> courseBasePageResult = new PageResult<CourseBase>(pageRes.getRecords(), pageRes.getTotal(),pageParams.getPageNo(),pageRes.getSize());

        return courseBasePageResult;
    }

    /***
     * 新增课程
     * @param companyId 机构 id
     * @param addCourseDto 新增课程的信息
     * @return
     */
    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(long companyId, AddCourseDto addCourseDto) {
        //合法性校验
//        if (StringUtils.isBlank(addCourseDto.getName())) {
//            ZHXYException.cast("课程名称为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getMt())) {
//            ZHXYException.cast("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getSt())) {
//            ZHXYException.cast("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getGrade())) {
//            ZHXYException.cast("课程等级为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
//            ZHXYException.cast("教育模式为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getUsers())) {
//            ZHXYException.cast("适应人群为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getCharge())) {
//            ZHXYException.cast("收费规则为空");
//        }

        //向CourseBase表添加课程基本信息
        CourseBase courseBaseNew=new CourseBase();
        //把 addCourseDto 中的部分属性值赋给 courseBaseNew 中的部分属性
        BeanUtils.copyProperties(addCourseDto,courseBaseNew);
        //赋予初始的创建日期、修改日期、审核状态、发布状态
        courseBaseNew.setCreateDate(LocalDateTime.now());
        courseBaseNew.setChangeDate(LocalDateTime.now());
        courseBaseNew.setAuditStatus("202002");
        courseBaseNew.setStatus("203001");
        //赋予机构ID
        courseBaseNew.setCompanyId(companyId);
        // todo:后续还要赋予创建人、修改人、机构名称
        int insert = courseBaseMapper.insert(courseBaseNew);
        if(insert<=0){
            //添加失败，则抛出异常
            ZHXYException.cast("添加课程失败！请联系管理员");
        }


        //向CourseMarket表添加课程营销信息
        CourseMarket courseMarketNew = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto,courseMarketNew);
        //为courseMarketNew赋予id，改id来自courseBaseNew，代表课程id
        courseMarketNew.setId(courseBaseNew.getId());

        int save=saveCourseMarket(courseMarketNew);
        if(save<=0){
            //添加失败，则抛出异常
            ZHXYException.cast("添加课程失败！请联系管理员");
        }
        //查询新增课程的详细信息并返回
        return getCourseBaseInfo(courseBaseNew.getId());
    }

    /***
     * 保存课程的营销信息
     * @param courseMarketNew 课程营销信息
     * @return 返回值小于等于0则表示保存失败
     */
    private int saveCourseMarket(CourseMarket courseMarketNew){
        //收费规则
        String charge = courseMarketNew.getCharge();
        if(StringUtils.isBlank(charge)){
            ZHXYException.cast("收费规则没有选择");
        }
        //收费规则为收费
        if(charge.equals("201001")){
            if(courseMarketNew.getPrice() == null || courseMarketNew.getPrice().floatValue()<=0){
                ZHXYException.cast("课程为收费价格不能为空且必须大于0");
            }
        }
        //根据id从课程营销表查询
        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarketNew.getId());
        if(courseMarketObj == null){
            //表中不存在对应id的课程时，新增该课程的营销信息
            return courseMarketMapper.insert(courseMarketNew);
        }else{
            //表中已存在对应id的课程时，修改该课程的营销信息
            BeanUtils.copyProperties(courseMarketNew,courseMarketObj);
            return courseMarketMapper.updateById(courseMarketObj);
        }
    }

    /***
     * 根据课程ID查找课程详细信息（基本信息+营销信息）
     * @param courseId 课程ID
     * @return 返回课程详细信息
     */
    @Override
    public CourseBaseInfoDto getCourseBaseInfo(long courseId) {
        //先从课程基本信息表查找
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null)
            return null;
        //再从课程营销信息表中查找
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);


        //将两种信息拼接起来
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        //获取分类名称，并记录在courseBaseInfoDto中
        String mtName = courseCategoryMapper.queryName(courseBase.getMt());
        String stName = courseCategoryMapper.queryName(courseBase.getSt());
        courseBaseInfoDto.setStName(stName);
        courseBaseInfoDto.setMtName(mtName);
        return courseBaseInfoDto;
    }

    /***
     * 修改课程信息
     * @param companyId 机构id
     * @param editCourseDto 修改后的课程信息
     * @return 返回修改后的课程信息
     */
    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(long companyId, EditCourseDto editCourseDto) {
        //首先判断机构id是否一致
        CourseBase courseBase = courseBaseMapper.selectById(editCourseDto.getId());
        if(courseBase==null){
            //课程不存在，抛自定义异常
            ZHXYException.cast("课程不存在！");
        }
        if(companyId!=courseBase.getCompanyId()){
            //机构不一致，抛自定义异常
            ZHXYException.cast("本机构只能修改本机构的课程！");
        }

        //然后修改课程的基本信息
        //封装修改后的课程基本信息
        BeanUtils.copyProperties(editCourseDto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        //todo:还需要修改 “修改人” 字段，等登录校验完成后即可获取当前用户，然后调用courseBase.setChangePeople()来修改
        //courseBase.setChangePeople();
        //访问数据库进行修改
        int i = courseBaseMapper.updateById(courseBase);
        if(i<=0){
            ZHXYException.cast("修改课程失败！");
        }

        //最后修改营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto,courseMarket);
        int i1 = saveCourseMarket(courseMarket);
        if(i1<=0){
            ZHXYException.cast("修改课程失败！");
        }

        //获取修改后的课程详细信息
        return getCourseBaseInfo(editCourseDto.getId());
    }
}
