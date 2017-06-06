package gov.hygs.entity;

import java.util.Date;

/**
 * 用于提交用户答题
 * @author david
 *
 */
public class ExamItem {
	private String tkId;
	private String result;
	private Date startTime;
	private Date endTime;
	private String mode;
	private Integer examDetailId;
	
	
	public Integer getExamDetailId() {
		return examDetailId;
	}
	public void setExamDetailId(Integer examDetailId) {
		this.examDetailId = examDetailId;
	}
	public String getTkId() {
		return tkId;
	}
	public void setTkId(String tkId) {
		this.tkId = tkId;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	
	
}
