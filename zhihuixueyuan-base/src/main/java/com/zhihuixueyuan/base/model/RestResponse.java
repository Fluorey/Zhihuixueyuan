package com.zhihuixueyuan.base.model;

import lombok.Data;

@Data
public class RestResponse<T> {

    /***
     * 相应编码
     */
    private int code;
    /***
     * 相应提示信息
     */
    private String message;
    /***
     * 相应内容
     */
    private T result;

    public RestResponse(){
        this(0,"success");
    }
    public RestResponse(int code,String message){
        this.code=code;
        this.message=message;
    }
    public static <T> RestResponse<T> success(T result){
        RestResponse<T> tRestResponse = new RestResponse<>();
        tRestResponse.setResult(result);
        return tRestResponse;
    }
    public static <T> RestResponse<T> success(T result,String message){
        RestResponse<T> tRestResponse = new RestResponse<>();
        tRestResponse.setResult(result);
        tRestResponse.setMessage(message);
        return tRestResponse;
    }
    public static <T> RestResponse<T> validFail(String message){
        RestResponse<T> tRestResponse = new RestResponse<>();
        tRestResponse.setMessage(message);
        tRestResponse.setCode(-1);
        return tRestResponse;
    }
    public static <T> RestResponse<T> validFail(T result,String message){
        RestResponse<T> tRestResponse = new RestResponse<>();
        tRestResponse.setResult(result);
        tRestResponse.setMessage(message);
        tRestResponse.setCode(-1);
        return tRestResponse;
    }
}
