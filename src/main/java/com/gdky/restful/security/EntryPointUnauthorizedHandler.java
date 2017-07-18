package com.gdky.restful.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdky.restful.entity.ResponseMessage;

@Component
public class EntryPointUnauthorizedHandler implements AuthenticationEntryPoint {

	private Logger logger = Logger.getLogger(getClass());
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
			throws IOException, ServletException {
		logger.error("401", e);
		// 返回一个401响应代替默认的重定向到登录页面
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		String message = "用户信息过期或失效,请重新登录！";
		if(e.getMessage().equals("账户或密码错误！")){
			message= e.getMessage();
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(response.getOutputStream(), ResponseMessage.error("401", message));
	}
}
