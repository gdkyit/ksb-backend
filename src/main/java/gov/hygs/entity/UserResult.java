package gov.hygs.entity;

import java.util.Date;

public class UserResult {
	private int userId;
	private String tkId;
	private String answer;
	private String result;
	private Long resultTime;
	private Double resultScore;
	private Date startTime;
	private Date endTime;
	private Integer examDetailId;
	private String type ;
	
	
	
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Integer getExamDetailId() {
		return examDetailId;
	}
	public void setExamDetailId(Integer examDetailId) {
		this.examDetailId = examDetailId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getTkId() {
		return tkId;
	}
	public void setTkId(String tkId) {
		this.tkId = tkId;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	
	public Long getResultTime() {
		return resultTime;
	}
	public void setResultTime(Long resultTime) {
		this.resultTime = resultTime;
	}
	public Double getResultScore() {
		return resultScore;
	}
	public void setResultScore(Double resultScore) {
		this.resultScore = resultScore;
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
	
	
}
