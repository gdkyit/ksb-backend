package com.gdky.restful.dao;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gdky.restful.entity.Role;
import com.gdky.restful.entity.User;

@Repository
@Transactional
public class AuthDao   {

	@Autowired
    protected JdbcTemplate jdbcTemplate;
	
	public List<User> getUser(String userName) {
		String sql = "select * from fw_users where username = ?";
		List<User> ls = this.jdbcTemplate.query(sql, new Object[] { userName },
				new BeanPropertyRowMapper<User>(User.class));
		return ls;
	}

	public List<User> getUserByUname(String uname) {
		String sql = "select * from fw_users where uname = ?";
		List<User> ls = this.jdbcTemplate.query(sql, new Object[] { uname },
				new BeanPropertyRowMapper<User>(User.class));
		return ls;
	}

	public List<Role> getRolesByUser(String userName) {
		StringBuffer sb = new StringBuffer();
		sb.append(" select r.* from fw_users u, fw_user_role ur ,fw_role  r ");
		sb.append(" where u.ID = ur.USER_ID ");
		sb.append(" and ur.ROLE_ID = r.ID ");
		sb.append(" and u.USERNAME =? ");
		List<Role> ls = this.jdbcTemplate.query(sb.toString(),
				new Object[] { userName }, new BeanPropertyRowMapper<Role>(
						Role.class));
		return ls;
	}

	public List<Role> getRoles() {
		String sql = "select * from fw_role t order by t.id desc";
		List<Role> ls = this.jdbcTemplate.query(sql,
				new BeanPropertyRowMapper<Role>(Role.class));
		return ls;
	}

	

	public void delPrivileges(Integer roleId) {
		String sql = "delete from fw_role_menu where role_id = ?";
		this.jdbcTemplate.update(sql, new Object[] { roleId });

	}

	public Number insertPrivileges(Integer roleId, List<String> privileges) {
		int rs = 0;
		if (privileges.size() > 0) {
			List<Object[]> batchValue = new ArrayList<Object[]>();
			for (String str : privileges) {
				Object[] obj = new Object[] { roleId, str };
				batchValue.add(obj);
			}
			String sql = "insert into fw_role_menu (role_id,menu_id) values (?,?)";
			rs = this.jdbcTemplate.batchUpdate(sql, batchValue).length;
		}

		return rs;
	}

	

	public Number addRole(Map<String, Object> obj) {
		String name = (String) obj.get("name");
		String desc = (String) obj.get("description");
		String sql = "insert into fw_role (name,description) values(?,?)";
		return jdbcTemplate.update(sql, new Object[] { name, desc });

	}

	public Integer delRole(Integer roleId) {
		String sql = "delete from fw_role where id = ?";
		Integer rs = this.jdbcTemplate.update(sql, new Object[] { roleId });
		return rs;

	}

	public void updateRole(Role role) {
		String sql = "update fw_role set name=?,description=? where id=?";
		this.jdbcTemplate.update(
				sql,
				new Object[] { role.getName(), role.getDescription(),
						role.getId() });

	}

	

	

	public void addRoleUser(int role, Integer userId) {
		String sql = "insert into fw_user_role (user_id,role_id) values(?,?)";
		this.jdbcTemplate.update(sql, new Object[] { userId, role });

	}

	public void delRoleUser(Integer userId) {
		String sql = "delete from fw_user_role where user_id =?";
		this.jdbcTemplate.update(sql, new Object[] { userId });
	}

	public Integer delUsers(ArrayList<Object[]> batchValue) {
		String sql = "delete from fw_users where id = ?";
		int rs = this.jdbcTemplate.batchUpdate(sql, batchValue).length;
		return rs;

	}

	
	
	public void resetPass(Integer userId, String password) {
		StringBuffer sb = new StringBuffer();
		sb.append(" update fw_users ");
		sb.append(" set password=? ");
		sb.append(" where id = ? ");
		this.jdbcTemplate.update(
				sb.toString(),
				new Object[] {password,userId});
	}


	public void updatePass(String password1, Integer userId) {
		String sql = "update fw_users set password = ? where id = ? ";
		this.jdbcTemplate.update(sql,new Object[]{password1,userId});		
	}

	


}
