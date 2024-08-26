package com.zhihuixueyuan.media.api;

import com.zhihuixueyuan.base.model.RestResponse;
import com.zhihuixueyuan.media.model.po.MediaFiles;
import com.zhihuixueyuan.media.service.MediaFileService;
import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
@RestController
@RequestMapping("/open")
public class OpenController {
    @Autowired
    MediaFileService mediaFileService;

    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable  String mediaId){
        MediaFiles mediaFiles = mediaFileService.queryMediaFileById(mediaId);
        if(mediaFiles==null){
            return RestResponse.validFail("找不到视频！");
        }
        //取出视频地址
        String url = mediaFiles.getUrl();
        if(StringUtils.isEmpty(url)){
            return RestResponse.validFail("该视频正在处理中！");
        }
        return RestResponse.success(url);
    }
}
