package com.zhihuixueyuan.media.api;

import com.zhihuixueyuan.base.model.RestResponse;
import com.zhihuixueyuan.media.model.dto.UploadFileParamsDto;
import com.zhihuixueyuan.media.model.dto.UploadFileResultDto;
import com.zhihuixueyuan.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/***
 * 上传视频
 */
@RestController
@Api(value = "视频文件上传接口", tags = "视频文件上传接口")
public class VideoFileController {
    @Autowired
    MediaFileService mediaFileService;

    @ApiOperation(value = "文件上传前检查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkfile(@RequestParam("fileMd5") String fileMd5) throws Exception {
        return mediaFileService.checkFile(fileMd5);
    }


    @ApiOperation(value = "分块文件上传前的检测")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5, @RequestParam("chunk") int chunk) throws Exception {
        return mediaFileService.checkChunk(fileMd5,chunk);
    }

    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadchunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunk) throws Exception {
        //把分块文件临时存储在服务器本地
        File temp = File.createTempFile(file.getOriginalFilename() + "_minio_"+chunk, ".temp");
        file.transferTo(temp);
        //文件路径
        String absolutePath = temp.getAbsolutePath();

        return mediaFileService.uploadChunk(fileMd5,chunk,absolutePath);
    }

    @ApiOperation(value = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergechunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal) throws Exception {
        Long companyID=1232141425L;
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFileType("001002");
        uploadFileParamsDto.setTags("课程视频");
        uploadFileParamsDto.setRemark("");
        uploadFileParamsDto.setFilename(fileName);
        return mediaFileService.mergechunks(companyID, fileMd5, chunkTotal, uploadFileParamsDto);

    }
}
