package com.gdky.restful.dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.gdky.restful.entity.User;

@Repository
public class UserLogDao   {

	@Autowired
    protected JdbcTemplate jdbcTemplate;
	
	public Number addLog(User user, String ip,String time, String action) {
		String sql = "insert into fw_user_log (user_id,ACCESS_IP,ACCESS_TIME,ACTION) values(?,?,?,?)";
		return 1;//jdbcTemplate.update(sql, new Object[]{user.getId(),ip,time,action});	
	}

}
