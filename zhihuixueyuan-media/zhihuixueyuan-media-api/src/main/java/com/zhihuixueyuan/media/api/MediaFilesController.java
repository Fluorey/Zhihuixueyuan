package com.zhihuixueyuan.media.api;

import com.zhihuixueyuan.base.model.PageParams;
import com.zhihuixueyuan.base.model.PageResult;
import com.zhihuixueyuan.media.model.dto.QueryMediaParamsDto;
import com.zhihuixueyuan.media.model.dto.UploadFileParamsDto;
import com.zhihuixueyuan.media.model.dto.UploadFileResultDto;
import com.zhihuixueyuan.media.model.po.MediaFiles;
import com.zhihuixueyuan.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @description 媒资文件管理接口
 */
 @Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
 @RestController
public class MediaFilesController {


  @Autowired
  MediaFileService mediaFileService;


    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto){
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiels(companyId,pageParams,queryMediaParamsDto);

    }

    /***
     * 上传文件
     * @param filedata 上传的文件
     * @return 返回文件信息
     */
    @ApiOperation("上传文件")
    @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile filedata) throws IOException {

        //todo:机构名先写死
        long companyId=1232141425L;

        //解析文件
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(filedata.getOriginalFilename());
        uploadFileParamsDto.setFileType("001001");
        uploadFileParamsDto.setFileSize(filedata.getSize());
        //todo:引入身份验证后获取用户名
        //uploadFileParamsDto.setUsername();
        //todo:后续还要设定标签、备注
        //uploadFileParamsDto.setTags();
        //uploadFileParamsDto.setRemark();

        //把文件临时存储在服务器本地
        File temp = File.createTempFile(filedata.getOriginalFilename() + "_minio", "temp");
        filedata.transferTo(temp);
        //文件路径
        String absolutePath = temp.getAbsolutePath();

        UploadFileResultDto uploadFileResultDto = mediaFileService.uploadFile(companyId, uploadFileParamsDto, absolutePath);

        //删除临时文件
        temp.delete();
        return uploadFileResultDto;
    }

}
