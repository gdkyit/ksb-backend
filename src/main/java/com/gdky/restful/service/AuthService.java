package com.gdky.restful.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.gdky.restful.dao.AuthDao;
import com.gdky.restful.entity.Role;
import com.gdky.restful.entity.User;
import com.gdky.restful.security.CustomUserDetails;

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

	public void insertDlxx(String random,CustomUserDetails userDetails) {
		// TODO Auto-generated method stub
		authDao.insertDlxx(random,userDetails);

	}
	

}
