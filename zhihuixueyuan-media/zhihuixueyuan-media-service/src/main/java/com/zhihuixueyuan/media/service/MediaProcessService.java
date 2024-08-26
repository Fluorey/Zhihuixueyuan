package com.zhihuixueyuan.media.service;

import com.zhihuixueyuan.media.model.po.MediaProcess;

import java.util.List;

/**
 * @description 媒资文件处理业务方法
 */
public interface MediaProcessService {
    /***
     * @description 获取待处理任务
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count 获取记录数
            */
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);

    /**
     *  开启一个任务
     * @param id 任务id
     * @return true开启任务成功，false开启任务失败
     */
    public boolean startTask(long id);

    /**
     * @description 保存视频媒资处理任务的结果
     * @param taskId  任务id
     * @param status 任务状态
     * @param fileId  文件id
     * @param url 处理后的视频的url
     * @param errorMsg 错误信息
     * @return void
     */
    public void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);
}


