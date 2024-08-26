package com.zhihuixueyuan.content.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/***
 * 请求视图页面（通过freemarker模板引擎）
 */
@Controller
public class FreemarkerController {

    @GetMapping("/testfreemarker")
    public ModelAndView test(){
        ModelAndView modelAndView = new ModelAndView();
        //设置模板名称
        modelAndView.setViewName("test");
        //设置模型数据
        modelAndView.addObject("username","杨昱睿");

        return modelAndView;
    }
}
