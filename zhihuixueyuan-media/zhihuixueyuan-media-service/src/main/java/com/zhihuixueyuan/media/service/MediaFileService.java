package com.zhihuixueyuan.media.service;

import com.zhihuixueyuan.base.model.PageParams;
import com.zhihuixueyuan.base.model.PageResult;
import com.zhihuixueyuan.base.model.RestResponse;
import com.zhihuixueyuan.media.model.dto.QueryMediaParamsDto;
import com.zhihuixueyuan.media.model.dto.UploadFileParamsDto;
import com.zhihuixueyuan.media.model.dto.UploadFileResultDto;
import com.zhihuixueyuan.media.model.po.MediaFiles;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.util.List;

/**
 * @description 媒资文件管理业务类
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.zhihuixueyuan.base.model.PageResult<com.zhihuixueyuan.media.model.po.MediaFiles>
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

 /***
  * 上传文件
  * @param companyId 机构ID
  * @param uploadFileParamsDto 上传文件的参数信息
  * @param localFilePath 文件的本地路径
  * @return 返回UploadFileResultDto对象
  */
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto,String localFilePath);

 /***
  * 把文件信息加入数据库
  * @param uploadFileParamsDto 文件信息
  * @param companyId 机构id
  * @param md5 文件的md5值
  * @param bucket 桶
  * @param objectName 文件的对象名
  * @return 返回文件信息
  */
 public MediaFiles addMediaFilesToDB(UploadFileParamsDto uploadFileParamsDto, Long companyId,
                                     String md5, String bucket, String objectName);

 /**
  * @description 检查文件是否存在
  * @param fileMd5 文件的md5
  */
 public RestResponse<Boolean> checkFile(String fileMd5);

 /**
  * @description 检查分块是否存在
  * @param fileMd5  文件的md5
  * @param chunkIndex  分块序号
  */
 public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

 /**
  * @description 上传分块
  * @param fileMd5  文件md5
  * @param chunk  分块序号
  * @param localFilePath  分块本地路径
  */
 public RestResponse uploadChunk(String fileMd5,int chunk,String localFilePath);

 /**
  * @description 合并分块
  * @param companyId  机构id
  * @param fileMd5  文件md5
  * @param chunkTotal 分块总和
  * @param uploadFileParamsDto 文件信息
  */
 public RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);

 /***
  * 从minio下载文件到本地
  * @param bucket 桶
  * @param objectName 对象名
  * @return 返回文件
  */
 public File downloadFileFromMinio(String bucket,String objectName);

 /***
  * 将文件上传至minio
  * @param localFilePath 文件本地路径
  * @param mimeType 文件的mimeTyepe
  * @param bucket 桶
  * @param objectName 文件上传后的名称
  * @return 上传成功则返回true
  */
 public boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket, String objectName);

 /***
  * 根据媒资文件的id查找文件
  * @param mediaId 文件id
  * @return 返回查询结果
  */
 public MediaFiles queryMediaFileById(String mediaId);
}
