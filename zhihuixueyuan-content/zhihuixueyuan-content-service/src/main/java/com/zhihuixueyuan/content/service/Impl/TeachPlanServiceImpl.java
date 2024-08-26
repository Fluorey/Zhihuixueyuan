package com.zhihuixueyuan.content.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhihuixueyuan.base.exception.ZHXYException;
import com.zhihuixueyuan.content.mapper.TeachplanMapper;
import com.zhihuixueyuan.content.mapper.TeachplanMediaMapper;
import com.zhihuixueyuan.content.model.dto.BindTeachPlanMediaDto;
import com.zhihuixueyuan.content.model.dto.CourseCategoryTreeDto;
import com.zhihuixueyuan.content.model.dto.SaveTeachPlanDto;
import com.zhihuixueyuan.content.model.dto.TeachplanDto;
import com.zhihuixueyuan.content.model.po.CourseCategory;
import com.zhihuixueyuan.content.model.po.Teachplan;
import com.zhihuixueyuan.content.model.po.TeachplanMedia;
import com.zhihuixueyuan.content.service.TeachPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Wrapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/***
 * 课程计划接口的实现类
 */
@Service
public class TeachPlanServiceImpl implements TeachPlanService {
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;
    @Override
    /***
     * 根据课程id查询其教学计划
     * @param courseId 课程id
     * @return 返回查出来的课程计划
     */
    public List<TeachplanDto> queryTeachPlanTreeNodes(long courseId) {
        //从数据库获取所有课程计划节点
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTeachPlanTreeNodes(courseId);
        if(teachplanDtos.isEmpty())
            ZHXYException.cast("不存在该课程！");

        //进行封装处理
        //定义返回结果，它保存所有grade为1的课程计划节点
        List<TeachplanDto> queryRes=new ArrayList<>();
        //在map中以分类id为key，以分类节点本身为value进行保存
        Map<Long, TeachplanDto> mp = teachplanDtos.stream().collect(Collectors.toMap(TeachplanDto::getId, item -> item));
        //遍历teachplanDtos，维护每个节点的子节点
        teachplanDtos.forEach(item->{
            if(item.getGrade()==1)
                queryRes.add(item);
            TeachplanDto parent = mp.get(item.getParentid());
            if(parent!=null){
                if(parent.getTeachPlanTreeNodes()==null){
                    parent.setTeachPlanTreeNodes(new ArrayList<TeachplanDto>());
                }
                parent.getTeachPlanTreeNodes().add(item);
            }
        });

        return queryRes;
    }

    /***
     * 新增或修改课程计划
     * @param saveTeachPlanDto 待新增或修改后的课程计划
     */
    @Transactional
    @Override
    public void saveTeachPlan(SaveTeachPlanDto saveTeachPlanDto) {
        //取出课程计划id
        Long id = saveTeachPlanDto.getId();
        //新增或修改课程计划
        if(id==null){
            //表示此时为新增课程计划
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachPlanDto,teachplan);
            //获取和该课程计划同级的课程计划的数量
            int count = getTeachplanCount(saveTeachPlanDto.getCourseId(), saveTeachPlanDto.getParentid());
            teachplan.setOrderby(count+1);
            //获取时间
            teachplan.setCreateDate(LocalDateTime.now());
            teachplan.setChangeDate(LocalDateTime.now());

            teachplanMapper.insert(teachplan);
        }else{
            //表示此时为修改课程计划
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(saveTeachPlanDto,teachplan);
            //获取时间
            teachplan.setChangeDate(LocalDateTime.now());

            teachplanMapper.updateById(teachplan);
        }
    }

    /***
     * 课程计划与媒资信息进行绑定的接口
     * @param bindTeachPlanMediaDto 所传参数包含课程计划id、媒资id与媒资文件名
     */
    @Override
    public TeachplanMedia associateMedia(BindTeachPlanMediaDto bindTeachPlanMediaDto) {
        //教学计划id
        Long teachplanId = bindTeachPlanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if(teachplan==null){
            ZHXYException.cast("教学计划不存在！");
        }
        Integer grade = teachplan.getGrade();
        if(grade!=2){
            ZHXYException.cast("只允许在第二级教学计划添加媒体资源");
        }
        LambdaQueryWrapper<TeachplanMedia> wrapper = new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, teachplanId);
        //删除现有的绑定记录
        int delete = teachplanMediaMapper.delete(wrapper);

        //添加新的媒资信息
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(bindTeachPlanMediaDto,teachplanMedia);
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        //todo:登录认证完成后还需要给teachplanMedia赋予createPeople和changePeople值
        int insert=teachplanMediaMapper.insert(teachplanMedia);
        if(insert<=0){
            ZHXYException.cast("添加视频失败！请重试");
        }
        return teachplanMedia;
    }

    /***
     * 获取和某个课程计划同级的节点有多少（包括它本身）
     * @param courseId 课程id
     * @param parentId 父节点id
     * @return 返回数量
     */
    private int getTeachplanCount(long courseId,long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId).eq(Teachplan::getParentid,parentId);
        return teachplanMapper.selectCount(queryWrapper);
    }
}
