package com.zhihuixueyuan.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuixueyuan.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {
    /**
     * @description 根据分片参数获取待处理任务
     * @param shardTotal  分片总数
     * @param shardIndex  分片序号
     * @param count 任务数
     * @return java.util.List<com.zhihuixueyuan.media.model.po.MediaProcess>
     * @author Mr.M
     * @date 2022/9/14 8:54
     */
    @Select("select * from media_process t where t.id % #{shardTotal} = #{shardIndex} and (t.status = '1' or t.status = '3') and t.fail_count < 3 limit #{count}")
    List<MediaProcess> selectListByShardIndex(@Param("shardTotal") int shardTotal, @Param("shardIndex") int shardIndex, @Param("count") int count);

    /**
     * 开启一个任务（通过乐观锁的方式，来看是否成功将status字段改为了4，这里的4表示“处理中”）
     * @param id 任务id
     * @return 更新记录数，返回值为1时，表示成功获取乐观锁
     */
    @Update("update media_process m set m.status='4' where (m.status='1' or m.status='3') and m.fail_count<3 and m.id=#{id}")
    int startTask(@Param("id") long id);

}
