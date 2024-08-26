package com.zhihuixueyuan.media.service.jobHandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.zhihuixueyuan.base.utils.Mp4VideoUtil;
import com.zhihuixueyuan.media.model.po.MediaProcess;
import com.zhihuixueyuan.media.service.MediaFileService;
import com.zhihuixueyuan.media.service.MediaProcessService;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.*;

/**
 * 视频任务处理类

 */
@Component
@Slf4j
public class VideoTask {
    @Autowired
    MediaProcessService mediaProcessService;
    @Autowired
    MediaFileService mediaFileService;
    @Value("${videoprocess.ffmpegpath}")
    //ffmpeg的安装位置
    String ffmpeg_path;


    /**
     * 2、分片任务
     */
    @XxlJob("videoJobHandler")
    public void shardingJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        //获取CPU核心数
        int processors = Runtime.getRuntime().availableProcessors();

        //查询任务
        List<MediaProcess> mediaProcessList = mediaProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
        int size = mediaProcessList.size();
        if(size<=0){
            log.debug("取到的任务数量：{}",size);
            return;
        }

        //创建一个线程池
        ExecutorService mediaProcessTasks = Executors.newFixedThreadPool(size);
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(task->{
            //将任务添加至线程池
            mediaProcessTasks.execute(()->{
                try {
                    //任务id
                    Long taskId = task.getId();
                    //文件id
                    String fileId = task.getFileId();
                    //objectName和桶
                    String objectName = task.getFilePath();
                    String bucket = task.getBucket();
                    //开启任务
                    boolean b = mediaProcessService.startTask(taskId);
                    if (!b) {
                        log.debug("抢占任务失败，任务id：{}", taskId);
                        return;
                    }
                    //执行视频转码
                    //从minio下载待处理的视频到本地
                    File file = mediaFileService.downloadFileFromMinio(bucket, objectName);
                    if (file == null) {
                        log.debug("下载文件失败！，任务id:{},bucket:{},objectName:{}", taskId, bucket, objectName);
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件异常");
                        return;
                    }
                    //源视频的路径
                    String video_path = file.getAbsolutePath();
                    //转换后mp4文件的名称
                    String mp4_name = fileId + ".mp4";
                    //转换后mp4文件的路径
                    //先创建临时文件用于保存转后文件
                    File tempFile = null;
                    try {
                        tempFile = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.debug("创建临时文件异常,{}", e.getMessage());
                        //保存任务处理失败的结果
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件异常");
                        return;
                    }
                    String mp4_path = tempFile.getAbsolutePath();
                    //创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4_path);
                    //开始视频转换，成功将返回success
                    String result = videoUtil.generateMp4();
                    //删除从minio下载到本地的原视频临时文件
                    file.delete();
                    if (!"success".equals(result)) {
                        //转码失败
                        log.debug("转码失败，原因：{}，bucket:{}，objectName:{}", result, bucket, objectName);
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, result);
                    }

                    //把转码后的视频上传至minio
                    String newObjectName = getFilePathByMD5(fileId, ".mp4");
                    boolean b1 = mediaFileService.addMediaFilesToMinIO(tempFile.getAbsolutePath(), "video/mp4", bucket, newObjectName);
                    if (!b1) {
                        log.debug("处理后的视频上传至Minio失败,taskId：{}", taskId);
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "处理后的视频上传至Minio失败");
                    }
                    //mp4文件的url
                    String url = "/"+bucket+"/"+newObjectName;
                    //删除临时文件
                    tempFile.delete();


                    //保存处理结果
                    mediaProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, null);
                }finally {
                    countDownLatch.countDown();
                }
            });
        });
        countDownLatch.await(30, TimeUnit.MINUTES);
        mediaProcessTasks.shutdown();

    }
    private String getFilePathByMD5(String fileMd5,String fileExt) {
        return fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }


}
