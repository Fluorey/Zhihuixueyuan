package com.zhihuixueyuan.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /***
     * 处理自定义异常
     * @return 返回异常处理信息
     */
    @ExceptionHandler(ZHXYException.class)
    //还需要返回一个对应的状态码
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customException(ZHXYException e){
        //打印异常信息
        log.info("系统异常 {}",e.getErrMessage(),e);

        return new RestErrorResponse(e.getErrMessage());

    }

    /***
     * JSR303框架对数据校验时返回的是MethodArgumentNotValidException异常，这里对该种异常进行处理
     * @return 返回异常处理信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse JSR303Exception(MethodArgumentNotValidException e){
        //打印异常信息
        log.info("系统异常 {}",e.getMessage(),e);

        //准备一个String集合，用于接受所有的错误信息
        List<String> errors=new ArrayList<>();

        //取出错误信息并放入errors
        BindingResult bindingResult = e.getBindingResult();
        bindingResult.getFieldErrors().forEach(error->errors.add(error.getDefaultMessage()));
        String errorMessage= StringUtils.join(errors,",");

        return new RestErrorResponse(errorMessage);

    }

    /***
     * 处理其他异常
     * @return 返回异常处理信息
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customException(Exception e){
        //打印异常信息
        log.info("系统异常 {}",e.getMessage(),e);

        if(e.getMessage().equals("不允许访问")){
            return new RestErrorResponse("没有操作此功能的权限");
        }

        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());

    }
}
