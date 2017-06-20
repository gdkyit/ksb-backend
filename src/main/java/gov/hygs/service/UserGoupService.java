package gov.hygs.service;

import java.util.List;
import java.util.Map;

import gov.hygs.dao.UserGroupDao;
import gov.hygs.entity.UserGroup;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

@Component
public class UserGoupService {
	@Resource
	private UserGroupDao userGroupDao;
	
	public List<Map<String,Object>> getUserGroup(Integer userId){
		return this.userGroupDao.getUserGroup(userId);
	}
	
	public List<Map<String,Object>> getGroups(){
		return this.userGroupDao.getGroups();
	}
	
	public void insertUserGroup(List<UserGroup> userGroups,Integer userId){
		this.userGroupDao.clearUserGroup(userId);
		for(UserGroup userGroup:userGroups){
			userGroup.setUserId(userId);
			this.userGroupDao.insertUserGroup(userGroup);
		}
	}
	
	
	public void updateUserDefaultGroup(UserGroup userGroup, Integer userId) {
		// TODO Auto-generated method stub
		this.userGroupDao.clearUserDefaultGroup(userId);
		userGroup.setUserId(userId);
		this.userGroupDao.updateUserDefaultGroup(userGroup);

	}
	
}
