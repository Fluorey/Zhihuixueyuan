package com.zhihuixueyuan.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhihuixueyuan.media.mapper.MediaFilesMapper;
import com.zhihuixueyuan.media.mapper.MediaProcessHistoryMapper;
import com.zhihuixueyuan.media.mapper.MediaProcessMapper;
import com.zhihuixueyuan.media.model.po.MediaFiles;
import com.zhihuixueyuan.media.model.po.MediaProcess;
import com.zhihuixueyuan.media.model.po.MediaProcessHistory;
import com.zhihuixueyuan.media.service.MediaFileService;
import com.zhihuixueyuan.media.service.MediaProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/***
 * 视频资源进行转码处理相关的接口
 */
@Service
@Slf4j
public class MediaProcessServiceImpl implements MediaProcessService {
    @Autowired
    MediaProcessMapper mediaProcessMapper;
    @Autowired
    MediaFilesMapper mediaFilesMapper;
    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;
    /***
     * @description 获取待处理任务
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count 获取记录数
     * @return java.util.List<com.zhihuixueyuan.media.model.po.MediaProcess>
     * @author Mr.M
     */
    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardTotal,shardIndex,count);
    }

    /**
     *  开启一个任务
     * @param id 任务id
     * @return true开启任务成功，false开启任务失败
     */
    @Override
    public boolean startTask(long id) {
        int i = mediaProcessMapper.startTask(id);
        return i<=0?false:true;
    }

    /**
     * @description 保存视频媒资处理任务的结果
     * @param taskId  任务id
     * @param status 任务状态
     * @param fileId  文件id
     * @param url 处理后的视频的url
     * @param errorMsg 错误信息
     * @return void
     */
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        //查出任务，如果不存在则直接返回
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if(mediaProcess == null){
            return ;
        }
        //处理失败，更新任务处理结果
        LambdaQueryWrapper<MediaProcess> queryWrapperById = new LambdaQueryWrapper<MediaProcess>().eq(MediaProcess::getId, taskId);
        //处理失败
        if(status.equals("3")){
            MediaProcess mediaProcess_u = new MediaProcess();
            mediaProcess_u.setStatus("3"); //设置状态编号（3表示处理失败）
            mediaProcess_u.setErrormsg(errorMsg); //设置失败信息
            mediaProcess_u.setFailCount(mediaProcess.getFailCount()+1); //失败次数自增
            mediaProcessMapper.update(mediaProcess_u,queryWrapperById);
            log.debug("更新任务处理状态为失败，任务信息:{}",mediaProcess_u);
            return ;
        }
        //任务处理成功
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if(mediaFiles!=null){
            //更新媒资文件中的访问url
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);
        }
        //处理成功，更新url和状态
        mediaProcess.setUrl(url);
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcessMapper.updateById(mediaProcess);

        //添加到历史记录
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);
        //删除mediaProcess
        mediaProcessMapper.deleteById(mediaProcess.getId());
    }


}
