package com.gdky.restful.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gdky.restful.dao.UserLogDao;
import com.gdky.restful.entity.User;
import com.gdky.restful.utils.Common;

@Transactional
@Service
public class UserLogService {
	
	@Resource
	private UserLogDao userLogDao;
	
	public void addLog(User user, String ip, String action) {
		String time = Common.getCurrentTime2MysqlDateTime();
		Number id = userLogDao.addLog(user,ip,time,action);
	}
}
