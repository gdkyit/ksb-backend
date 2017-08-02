package gov.hygs.entity;

public class UserGroup {
	private Integer userId;
	private Integer groupId;
	private String isDefault;
	private String readMark;
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Integer getGroupId() {
		return groupId;
	}
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
	public String getIsDefault() {
		return isDefault;
	}
	public void setIsDefault(String isDefault) {
		this.isDefault = isDefault;
	}
	public String getReadMark() {
		return readMark;
	}
	public void setReadMark(String readMark) {
		this.readMark = readMark;
	}
	
	
	
}
