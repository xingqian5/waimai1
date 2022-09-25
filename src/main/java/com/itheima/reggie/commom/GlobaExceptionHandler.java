package com.itheima.reggie.commom;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j



public class GlobaExceptionHandler {
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandle(SQLIntegrityConstraintViolationException ex){
        log.info("开始");
        log.error(ex.getMessage());
        if (ex.getMessage().contains("Duplicate entry")){
            String[] split=ex.getMessage().split(" ");
            String msg=split[2]+"已存在";
            return  R.error(msg);
        }
        return  R.error("失败了");

    }
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandle(CustomException ex){

        log.error(ex.getMessage());

        return  R.error(ex.getMessage());

    }}
