package gov.hygs.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.gdky.restful.dao.BaseJdbcDao;

import gov.hygs.entity.UserGroup;
@Repository
public class UserGroupDao extends BaseJdbcDao {

	public List<Map<String,Object>> getUserGroup(Integer userId){
		String sql ="select gt.*,ug.is_default,ug.read_mark from user_group as ug,grouptable as gt where gt.id_ = ug.group_id and (now()<= gt.effective_date or gt.effective_date is null) and ug.USER_ID= ?";
		return this.jdbcTemplate.queryForList(sql, new Object[]{userId});
	}
	
	public List<Map<String,Object>> getGroups(){
		String sql ="select * from grouptable gt where (now()<= gt.effective_date or gt.effective_date is null)";
		return this.jdbcTemplate.queryForList(sql);
	}
	public void insertUserGroup(UserGroup userGroup){
		String sql = "insert into user_group(user_id,group_id,is_default,read_mark) values(?,?,?,?)";
		this.jdbcTemplate.update(sql, new Object[]{
				userGroup.getUserId(),userGroup.getGroupId(),userGroup.getIsDefault(),userGroup.getReadMark()
			});
	}
	public void clearUserGroup(Integer userId){
		String sql ="delete from user_group where user_id =?";
		this.jdbcTemplate.update(sql, new Object[]{
				userId
		});
	}

	public void clearUserDefaultGroup(Integer userId) {
		// TODO Auto-generated method stub
		String sql ="update user_group set is_default = 'N' where user_id =?";
		this.jdbcTemplate.update(sql, new Object[]{
				userId
		});
	}

	public void updateUserDefaultGroup(UserGroup userGroup) {
		// TODO Auto-generated method stub
		String sql ="update user_group set is_default = 'Y' where user_id =? and group_id = ? ";
		this.jdbcTemplate.update(sql, new Object[]{
				userGroup.getUserId(),userGroup.getGroupId()
		});
	}

	public void updateUpload(Integer userId, String image) {
		// TODO Auto-generated method stub
		String sql ="update user set photo = ? where id_ = ? ";
		this.jdbcTemplate.update(sql, new Object[]{image,userId});
	}
	
}
