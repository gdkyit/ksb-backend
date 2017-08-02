package com.gdky.restful.security;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gdky.restful.entity.ResponseMessage;

@ControllerAdvice
public class GlobalExceptionHandler {
    private Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 系统异常处理，比如：404,500
     * @param req
     * @param resp
     * @param e
     * @return
     * @throws Exception
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseMessage defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        logger.error("", e);
        String message = e.getMessage();
        if (e instanceof org.springframework.web.servlet.NoHandlerFoundException) {
        	String url = ((org.springframework.web.servlet.NoHandlerFoundException) e).getRequestURL();
             return ResponseMessage.error("404", "访问路径:"+url+" 不存在！");
        } else {
            return ResponseMessage.error("500", message);
        }
    }
}