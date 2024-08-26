package com.zhihuixueyuan.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.zhihuixueyuan.base.exception.ZHXYException;
import com.zhihuixueyuan.base.model.PageParams;
import com.zhihuixueyuan.base.model.PageResult;
import com.zhihuixueyuan.base.model.RestResponse;
import com.zhihuixueyuan.media.mapper.MediaFilesMapper;
import com.zhihuixueyuan.media.mapper.MediaProcessMapper;
import com.zhihuixueyuan.media.model.dto.QueryMediaParamsDto;
import com.zhihuixueyuan.media.model.dto.UploadFileParamsDto;
import com.zhihuixueyuan.media.model.dto.UploadFileResultDto;
import com.zhihuixueyuan.media.model.po.MediaFiles;
import com.zhihuixueyuan.media.model.po.MediaProcess;
import com.zhihuixueyuan.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 媒资管理接口
 */
 @Service
 @Slf4j
public class MediaFileServiceImpl implements MediaFileService {

   @Autowired
   MediaFilesMapper mediaFilesMapper;
   @Autowired
    MediaProcessMapper mediaProcessMapper;
   @Autowired
  MinioClient minioClient;
   @Autowired
   MediaFileService mediaFileService;
   //普通文件桶
   @Value("${minio.bucket.files}")
   private String bucket_Files;
   //视频文件桶
   @Value("${minio.bucket.videofiles}")
   private String bucket_video;

  /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.zhihuixueyuan.base.model.PageResult<com.zhihuixueyuan.media.model.po.MediaFiles>
  */
  @Override
  public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

   //构建查询条件对象
   LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

   //分页对象
   Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
   // 查询数据内容获得结果
   Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
   // 获取数据列表
   List<MediaFiles> list = pageResult.getRecords();
   // 获取数据总数
   long total = pageResult.getTotal();
   // 构建结果集
   PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
   return mediaListResult;

  }

  /***
  * 上传文件
  * @param companyId 机构ID
  * @param uploadFileParamsDto 上传文件的参数信息
  * @param localFilePath 文件的本地路径
  * @return 返回UploadFileResultDto对象
  */
  @Override
  public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {

   File file = new File(localFilePath);
   if (!file.exists()) {
      ZHXYException.cast("文件不存在");
   }
   //第一步：整理文件信息
   //文件名称
   String fileName=uploadFileParamsDto.getFilename();
   //文件扩展名
   String extension = fileName.substring(fileName.lastIndexOf("."));
   //获取文件的mimeType
   String mimeType = getMimeType(extension);
   //获取文件的Md5值
   String md5 = getMd5(file);
   //获取存在minio上的文件的目录
   String defaultFolderPath = getDefaultFolderPath();
   //生成文件的ObjectName
   String objectName=defaultFolderPath+md5+extension;

   //第二步：将文件信息保存至数据库，并保存至minio
   MediaFiles mediaFiles = mediaFileService.addMediaFilesToDB(uploadFileParamsDto, companyId, md5, bucket_Files, objectName);
   boolean b = addMediaFilesToMinIO(localFilePath, mimeType, bucket_Files, objectName);

   //第三步：返回数据
      UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
      BeanUtils.copyProperties(mediaFiles,uploadFileResultDto);
      return uploadFileResultDto;
  }

 /***
  * 获取文件的mimeType
  * @param extension 文件扩展名
  * @return 返回mImeType
  */
 private String getMimeType(String extension){
     if(extension==null)
       extension = "";
     //根据扩展名取出mimeType
     ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
     //通用mimeType，字节流
     String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
     if(extensionMatch!=null){
       mimeType = extensionMatch.getMimeType();
     }
     return mimeType;
  }

 /***
  * 获取文件的md5值
  * @param file 文件
  * @return 返回md5值
  */
  private String getMd5(File file){
     try {
        FileInputStream fileInputStream = new FileInputStream(file);
        String md5Hex = DigestUtils.md5Hex(fileInputStream);
        return md5Hex;
     } catch (IOException e) {
       e.printStackTrace();
       return null;
     }
  }

 /***
  * 根据当前时间获取文件的路径
  * @return 返回以年月日组成的路径
  */
  private String getDefaultFolderPath(){
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String folder = sdf.format(new Date()).replace("-", "/")+"/";
    return folder;
  }

 /***
  * 将文件上传至minio
  * @param localFilePath 文件本地路径
  * @param mimeType 文件的mimeTyepe
  * @param bucket 桶
  * @param objectName 文件上传后的名称
  * @return 上传成功则返回true
  */
 @Override
  public boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket, String objectName){
      try {
           UploadObjectArgs testbucket = UploadObjectArgs.builder()
                   .bucket(bucket)
                   .object(objectName)
                   .filename(localFilePath)
                   .contentType(mimeType)
                   .build();
           minioClient.uploadObject(testbucket);
           log.debug("上传文件到minio成功,bucket:{},objectName:{}",bucket,objectName);
           System.out.println("上传成功");
           return true;
     } catch (Exception e) {
          e.printStackTrace();
          log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}",bucket,objectName,e.getMessage(),e);
          ZHXYException.cast("上传文件到文件系统失败");
     }
     return false;
  }

    /***
     * 根据媒资文件的id查找文件
     * @param mediaId 文件id
     * @return 返回查询结果
     */
    @Override
    public MediaFiles queryMediaFileById(String mediaId) {
        return mediaFilesMapper.selectById(mediaId);
    }

    /***
     * 将上传的文件的信息保存至数据库
     * @param uploadFileParamsDto 上传的文件的参数
     * @param companyId 机构id
     * @param md5 文件的md5值
     * @param bucket 桶
     * @param objectName 文件的对象名
     * @return 返回上传成功的文件信息，封装在MediaFiles对象中
     *
     */
    @Transactional
    @Override
    public MediaFiles addMediaFilesToDB(UploadFileParamsDto uploadFileParamsDto, Long companyId,
                                        String md5, String bucket, String objectName){
        //先从数据库查数据
        MediaFiles mediaFile = mediaFilesMapper.selectById(md5);
        if(mediaFile!=null)
            ZHXYException.cast("文件已存在！上传失败！");
        mediaFile = new MediaFiles();
        //拷贝基本信息
        BeanUtils.copyProperties(uploadFileParamsDto,mediaFile);
        mediaFile.setFileId(md5);
        mediaFile.setId(md5);
        mediaFile.setCompanyId(companyId);
        mediaFile.setUrl("/" + bucket + "/" + objectName);
        mediaFile.setBucket(bucket);
        mediaFile.setFilePath(objectName);
        mediaFile.setCreateDate(LocalDateTime.now());
        mediaFile.setAuditStatus("002003");
        mediaFile.setStatus("1");
        //插入数据库
        int insert = mediaFilesMapper.insert(mediaFile);
        if(insert<=0){
            log.error("保存文件信息到数据库失败,{}",mediaFile.toString());
            ZHXYException.cast("保存文件信息失败");
        }
        log.debug("保存文件信息到数据库成功,{}",mediaFile.toString());

        //若该文件为视频，则记录至media_process表，表示待转码处理
        addWaitingTask(mediaFile);

        return mediaFile;
    }

    /**
     * @description 检查文件是否存在
     * @param fileMd5 文件的md5
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        //先检查数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles!=null){
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(mediaFiles.getBucket())
                    .object(mediaFiles.getFilePath())
                    .build();
            try {
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if(inputStream!=null)
                    return RestResponse.success(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResponse.success(false);
    }

    /**
     * @description 检查分块是否存在
     * @param fileMd5  文件的md5
     * @param chunkIndex  分块序号
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        //得到分块文件目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunkIndex;

        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_video)
                .object(chunkFilePath)
                .build();
        try {
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            if(inputStream!=null)
                return RestResponse.success(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.success(false);
    }

    /**
     * @description 上传分块
     * @param fileMd5  文件md5
     * @param chunk  分块序号
     * @param localFilePath  分块本地路径
     */
    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localFilePath) {
        //获取分块文件的对象名
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5)+chunk;
        //获取分块MineType
        String mimeType = getMimeType(null);
        //将分块上传至minio
        boolean b = addMediaFilesToMinIO(localFilePath, mimeType, bucket_video, chunkFileFolderPath);
        if(b){
            return RestResponse.success(true);
        }
        return RestResponse.validFail(false,"上传分块失败");
    }

    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //分块所在目录
        String chunkFileFolderPath=getChunkFileFolderPath(fileMd5);
        //获取所有分块文件
        List<ComposeSource> sources= Stream.iterate(0, i->++i).limit(chunkTotal).map(i->ComposeSource.builder().bucket(bucket_video).object(chunkFileFolderPath+i).build()).collect(Collectors.toList());
        //原文件名称
        String filename = uploadFileParamsDto.getFilename();
        //扩展名
        String extension=filename.substring(filename.lastIndexOf("."));
        //合并后文件的objectName
        String objectName = getFilePathByMD5(fileMd5, extension);
        //指定合并信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_video)
                .object(objectName)
                .sources(sources)
                .build();
        //进行文件合并
        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件出错！,bucket:{},objectName:{}，错误信息:{}",bucket_video,objectName,e.getMessage());
            return RestResponse.validFail(false,"合并文件异常！");
        }

        //将文件信息入库
        MediaFiles mediaFiles = mediaFileService.addMediaFilesToDB(uploadFileParamsDto, companyId, fileMd5, bucket_video, objectName);
        if(mediaFiles==null){
            return RestResponse.validFail(false,"文件入库失败！");
        }

        //清理分块文件
        clearChunkFiles(chunkFileFolderPath,chunkTotal);
        return RestResponse.success(true);
    }

    /***
     * 从minio下载文件到本地
     * @param bucket 桶
     * @param objectName 对象名
     * @return 返回文件
     */
    @Override
    public File downloadFileFromMinio(String bucket, String objectName) {
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile=File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 添加待处理任务
     * @param mediaFiles 媒资文件信息
     */
    private void addWaitingTask(MediaFiles mediaFiles){
        //文件名称
        String filename = mediaFiles.getFilename();
        //文件扩展名
        String exension = filename.substring(filename.lastIndexOf("."));
        //文件mimeType
        String mimeType = getMimeType(exension);
        //如果是avi视频添加到视频待处理表
        if(mimeType.equals("video/x-msvideo")){
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles,mediaProcess);
            mediaProcess.setStatus("1");//未处理
            mediaProcess.setFailCount(0);//失败次数默认为0
            mediaProcess.setUrl(null);
            mediaProcessMapper.insert(mediaProcess);
        }
    }


    //得到分块文件的目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }
    //得到合并后文件的目录
    private String getFilePathByMD5(String fileMd5,String fileExt) {
        return fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }
    /**
     * 清除分块文件
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal 分块文件总数
     */
    private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal) {

        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());

            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket("video").objects(deleteObjects).build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            results.forEach(r -> {
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清除分块文件失败,objectname:{}", deleteError.objectName(), e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("清除分块文件失败,chunkFileFolderPath:{}", chunkFileFolderPath, e);
        }
    }

}
