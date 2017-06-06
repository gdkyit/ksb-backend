package gov.hygs.entity;

import java.util.Date;

public class UserEntity {
	private int userId;
	private String name;
	private String password;
	private String phone;
	private Date birthday;
	private Date rzday;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public Date getRzday() {
		return rzday;
	}

	public void setRzday(Date rzday) {
		this.rzday = rzday;
	}

}
