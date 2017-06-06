package gov.hygs.entity;

import java.util.Date;

public class LoudRecord {
	
	private Integer userId;
	private Integer deptId;
	private String zstkId;
	private Date dzDate;
	private String type;
	private String remark;
	
	

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Integer getDeptId() {
		return deptId;
	}
	public void setDeptId(Integer deptId) {
		this.deptId = deptId;
	}
	public String getZstkId() {
		return zstkId;
	}
	public void setZstkId(String zstkId) {
		this.zstkId = zstkId;
	}
	public Date getDzDate() {
		return dzDate;
	}
	public void setDzDate(Date dzDate) {
		this.dzDate = dzDate;
	}
	
}
