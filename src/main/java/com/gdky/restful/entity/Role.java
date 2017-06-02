package com.gdky.restful.entity;

import java.io.Serializable;

public class Role implements Serializable {

	/** serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** ID_. */
	private Integer id_;

	/** 角色名称. */
	private String roleName;

	/** 描述. */
	private String ms;


	/**
	 * Constructor.
	 */
	public Role() {
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
	 * Set the 角色名称.
	 * 
	 * @param roleName
	 *            角色名称
	 */
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	/**
	 * Get the 角色名称.
	 * 
	 * @return 角色名称
	 */
	public String getRoleName() {
		return this.roleName;
	}

	/**
	 * Set the 描述.
	 * 
	 * @param ms
	 *            描述
	 */
	public void setMs(String ms) {
		this.ms = ms;
	}

	/**
	 * Get the 描述.
	 * 
	 * @return 描述
	 */
	public String getMs() {
		return this.ms;
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
		Role other = (Role) obj;
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
