package gov.hygs.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.gdky.restful.dao.BaseJdbcDao;

import gov.hygs.entity.UserGroup;
@Repository
public class UserGroupDao extends BaseJdbcDao {

	public List<Map<String,Object>> getUserGroup(Integer userId){
		String sql ="select gt.*,ug.is_default from user_group as ug,grouptable as gt where gt.id_ = ug.group_id and ug.USER_ID= ?";
		return this.jdbcTemplate.queryForList(sql, new Object[]{userId});
	}
	
	public List<Map<String,Object>> getGroups(){
		String sql ="select * from grouptable ";
		return this.jdbcTemplate.queryForList(sql);
	}
	public void insertUserGroup(UserGroup userGroup){
		String sql = "insert into user_group(user_id,group_id) values(?,?)";
		this.jdbcTemplate.update(sql, new Object[]{
				userGroup.getUserId(),userGroup.getGroupId()
			});
	}
	public void clearUserGroup(Integer userId){
		String sql ="delete from user_group where user_id =?";
		this.jdbcTemplate.update(sql, new Object[]{
				userId
		});
	}
	
}
