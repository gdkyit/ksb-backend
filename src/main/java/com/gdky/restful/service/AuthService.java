package com.gdky.restful.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.gdky.restful.dao.AuthDao;
import com.gdky.restful.entity.Role;
import com.gdky.restful.entity.User;

@Service
public class AuthService {
	
	@Resource
	private AuthDao authDao;
	
	public User getUser(String userName){
		return  authDao.getUser(userName);
	}

	public List<Role> getRolesByUser(String userName) {
		return authDao.getRolesByUser(userName);
	}
	

}
