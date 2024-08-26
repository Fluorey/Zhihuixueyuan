package com.zhihuixueyuan.base.exception;

/***
 * 智慧学苑项目异常类
 */
public class ZHXYException extends RuntimeException{
    private String errMessage;

    public ZHXYException() {
        super();
    }

    public ZHXYException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public static void cast(CommonError commonError){
        throw new ZHXYException(commonError.getErrMessage());
    }
    public static void cast(String errMessage){
        throw new ZHXYException(errMessage);
    }
}
