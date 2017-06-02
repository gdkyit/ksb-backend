package com.gdky.restful.entity;

import java.io.Serializable;
import java.util.Date;
public class User implements Serializable {

	/** serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** ID_. */
	private Integer id_;

	/** 登陆名称. */
	private String loginName;

	/** 用户名称. */
	private String userName;

	/** 手机号码. */
	private String phone;

	/** 入职时间. */
	private Date rzsj;

	/** 职位. */
	private String zw;

	/** 密码. */
	private String pwd;

	/** 头像. */
	private String photo;

	/** 所在科室. */
	private Integer deptid;

	/** 生日. */
	private Date birthday;

	
	private Integer accountEnabled;
	private Integer accountExpired;
	private Integer accountLocked;
	private Integer credentialsExpired;
	
	/**
	 * Constructor.
	 */
	public User() {
	}

	/**
	 * Set the ID_.
	 * 
	 * @param id
	 *            ID_
	 */
	public void setId_(Integer id_) {
		this.id_ = id_;
	}

	/**
	 * Get the ID_.
	 * 
	 * @return ID_
	 */
	public Integer getId_() {
		return this.id_;
	}

	/**
	 * Set the 登陆名称.
	 * 
	 * @param login_Name
	 *            登陆名称
	 */
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	/**
	 * Get the 登陆名称.
	 * 
	 * @return 登陆名称
	 */
	public String getLoginName() {
		return this.loginName;
	}

	/**
	 * Set the 用户名称.
	 * 
	 * @param user_Name
	 *            用户名称
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Get the 用户名称.
	 * 
	 * @return 用户名称
	 */
	public String getUserName() {
		return this.userName;
	}

	/**
	 * Set the 手机号码.
	 * 
	 * @param phone
	 *            手机号码
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * Get the 手机号码.
	 * 
	 * @return 手机号码
	 */
	public String getPhone() {
		return this.phone;
	}

	/**
	 * Set the 入职时间.
	 * 
	 * @param rzsj
	 *            入职时间
	 */
	public void setRzsj(Date rzsj) {
		this.rzsj = rzsj;
	}

	/**
	 * Get the 入职时间.
	 * 
	 * @return 入职时间
	 */
	public Date getRzsj() {
		return this.rzsj;
	}

	/**
	 * Set the 职位.
	 * 
	 * @param zw
	 *            职位
	 */
	public void setZw(String zw) {
		this.zw = zw;
	}

	/**
	 * Get the 职位.
	 * 
	 * @return 职位
	 */
	public String getZw() {
		return this.zw;
	}

	/**
	 * Set the 密码.
	 * 
	 * @param pwd
	 *            密码
	 */
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	/**
	 * Get the 密码.
	 * 
	 * @return 密码
	 */
	public String getPwd() {
		return this.pwd;
	}
    public String getPassword(){
    		return this.pwd;
    }
    public String getUsername(){
		return this.loginName;
}
	/**
	 * Set the 头像.
	 * 
	 * @param photo
	 *            头像
	 */
	public void setPhoto(String photo) {
		this.photo = photo;
	}

	/**
	 * Get the 头像.
	 * 
	 * @return 头像
	 */
	public String getPhoto() {
		return this.photo;
	}

	/**
	 * Set the 所在科室.
	 * 
	 * @param deptid
	 *            所在科室
	 */
	public void setDeptid(Integer deptid) {
		this.deptid = deptid;
	}

	/**
	 * Get the 所在科室.
	 * 
	 * @return 所在科室
	 */
	public Integer getDeptid() {
		return this.deptid;
	}

	/**
	 * Set the 生日.
	 * 
	 * @param birthday
	 *            生日
	 */
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	/**
	 * Get the 生日.
	 * 
	 * @return 生日
	 */
	public Date getBirthday() {
		return this.birthday;
	}

	
	public Integer getAccountEnabled() {
		return accountEnabled;
	}

	public void setAccountEnabled(Integer accountEnabled) {
		this.accountEnabled = accountEnabled;
	}

	public Integer getAccountExpired() {
		return accountExpired;
	}

	public void setAccountExpired(Integer accountExpired) {
		this.accountExpired = accountExpired;
	}

	public Integer getAccountLocked() {
		return accountLocked;
	}

	public void setAccountLocked(Integer accountLocked) {
		this.accountLocked = accountLocked;
	}

	public Integer getCredentialsExpired() {
		return credentialsExpired;
	}



	public User(User u) {
		
		this.id_ = u.id_;
		this.loginName = u.loginName;
		this.userName = u.userName;
		this.phone = u.phone;
		this.rzsj = u.rzsj;
		this.zw = u.zw;
		this.pwd = u.pwd;
		this.photo = u.photo;
		this.deptid = u.deptid;
		this.birthday = u.birthday;
		this.accountEnabled = u.accountEnabled;
		this.accountExpired = u.accountExpired;
		this.accountLocked = u.accountLocked;
		this.credentialsExpired = u.credentialsExpired;
	}

	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id_ == null) ? 0 : id_.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		User other = (User) obj;
		if (id_ == null) {
			if (other.id_ != null) {
				return false;
			}
		} else if (!id_.equals(other.id_)) {
			return false;
		}
		return true;
	}
	
}
