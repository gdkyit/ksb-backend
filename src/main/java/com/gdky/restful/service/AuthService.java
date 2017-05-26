package com.gdky.restful.service;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gdky.restful.dao.AuthDao;
import com.gdky.restful.entity.Role;
import com.gdky.restful.entity.User;

@Service
@Transactional
public class AuthService {

	@Resource
	private AuthDao authDao;

	public User getUser(String userName) {
		List<User> ls = authDao.getUser(userName);
		if (ls.size() != 1) {
			return null;
		}
		return ls.get(0);
	}

	public User getUserByUname(String uname) {
		List<User> ls = authDao.getUserByUname(uname);
		if (ls.size() != 1) {
			return null;
		}
		return ls.get(0);
	}

	public List<Role> getRolesByUser(String userName) {
		return authDao.getRolesByUser(userName);
	}

	public List<Role> getRoles() {
		return authDao.getRoles();
	}



	public void delPrivileges(Integer roleId) {
		authDao.delPrivileges(roleId);
	}


	public static List<String> unique(List<String> list) {
		// List_unique
		HashSet<String> set = new HashSet<String>(list);
		list.clear();
		list.addAll(set);
		return list;
	}

	public static List<String> removeZero(List<String> items) {
		List<String> list = new ArrayList<String>();
		for (String str : items) {
			if (!str.equals("000")) {
				str = str.replaceFirst("^0*", "");
				list.add(str);
			}
		}
		return list;
	}

	public Number addRole(Map<String, Object> obj) {
		return authDao.addRole(obj);
	}

	public void delRole(Integer roleId) {
		authDao.delPrivileges(roleId);
		authDao.delRole(roleId);
	}

	public void updateRole(Role role) {
		authDao.updateRole(role);
	}



	
	public void addRoleUser(int role, Integer userId) {
		authDao.addRoleUser(role,userId);
	}




	public void delRoleUser(Integer userId) {
		authDao.delRoleUser(userId);
	}

	
    public String getForWAddr(HttpServletRequest request) {  
        String ip = request.getHeader("X-FORWARDED-FOR");  
        if(ip == null ) {  
            ip = request.getRemoteAddr();  
        }  
        return ip;  
    } 
    public String getRealAddr(HttpServletRequest request) {  
        String ip = request.getHeader("X-Real-IP");  
        if(ip == null ) {  
            ip = request.getRemoteAddr();  
        }  
        return ip;  
    }  
}
