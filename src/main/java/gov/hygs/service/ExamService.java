package gov.hygs.service;

import gov.hygs.dao.ExamDao;
import gov.hygs.dao.TkxxDao;
import gov.hygs.entity.ExamItem;
import gov.hygs.entity.UserResult;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.gdky.restful.entity.ResponseMessage;

@Component
public class ExamService {
	@Resource
	private ExamDao examDao;
	@Resource
	private TkxxDao tkxxDao;

	public List<Map<String, Object>> getExam(String type) {
		UserDetails userDetails = (UserDetails) SecurityContextHolder
				.getContext().getAuthentication().getPrincipal();
		String loginName = userDetails.getUsername();
		Map<String, Object> user = this.tkxxDao.getUserByLoginName(loginName);
		List<Map<String,Object>> exams = this.examDao.getExam((Integer) user.get("ID_"), type);
		if(null != exams){
			for(Map<String,Object> exam :exams){
				String remark =(String)exam.get("remark");
				if(null == remark){
					remark =(String)exam.get("REMARK");
				}
				Integer examTime =(Integer)exam.get("exam_time");
				remark = remark.replaceAll("$score", examTime+"");
				exam.put("remark", remark);
			}
		}
		
		return exams;
	}
	public List<UserResult> getUserRs(Integer examId,Integer userId){
		return this.examDao.getUserRs(examId, userId);
	}

	/**
	 * 查询考试题目
	 * 
	 * @param examId
	 * @return
	 */
	public List<Map<String, Object>> getExamDetailByExamId(Integer examId) {
		Integer userId = this.getUserId();
		return this.examDao.getExamDetailByExamId(examId,userId);
	}

	public ResponseMessage doCheckExamItem(ExamItem item) {
		ResponseMessage returnValue = null;
		Map<String, Object> param = new HashMap<String, Object>();
		String rs = "恭喜答对";
		long sec = (item.getEndTime().getTime() - item.getStartTime().getTime()) / 1000;
		int timeFlag = 0;
		if (sec <= 30) {
			rs += "答题速度：神速  ";
		} else if (sec <= 60 * 2 && sec > 30) {
			rs += "答题速度：较快  ";
			timeFlag = 1;
		} else if (sec <= 60 * 8 && sec > 60 * 2) {
			rs += "答题速度：慢  ";
			timeFlag = 2;
		} else {
			rs += "答题速度：很慢 ";
			timeFlag = 3;
		}
		List<Map<String, Object>> das = this.tkxxDao.getTkDaByTkId(item
				.getTkId());
		if (null != das && das.size() > 0) {
			Map<String, String> daMap = new HashMap<String, String>();
			param.put("dans", daMap);
			String mode = item.getMode();
			boolean flag = true;
			if ("2".equals(mode)) {// 多选题
				String[] results = item.getResult().split(";");
				Map<String, String> map = new HashMap<String, String>();
				String dbDa = "";
				if (das.size() >= results.length) {
					for (int i = 0; i < results.length; i++) {
						map.put(results[i], results[i]);
					}
					for (Map<String, Object> da : das) {
						String key = (String) da.get("XZ_KEY");
						daMap.put(key, key);
						if (null == map.get(key)) {
							flag = false;
						}
						if (dbDa.length() > 0) {
							dbDa += ";";
						}
						dbDa += key;
					}
					if (false == flag) {
						rs = "你答题目错误,正确答案为" + dbDa;
					}
				} else {
					for (Map<String, Object> da : das) {
						String key = (String) da.get("XZ_KEY");
						daMap.put(key, key);
						map.put(key, key);
						if (dbDa.length() > 0) {
							dbDa += ";";
						}
						dbDa += key;
					}
					for (int i = 0; i < results.length; i++) {
						if (null == map.get(results[i])) {
							flag = false;
						}
					}
					if (false == flag) {
						rs = "你答题目错误,正确答案为" + dbDa;
					}
				}

			} else {// 单选题
				Map<String, Object> da = das.get(0);
				String key = (String) da.get("XZ_KEY");
				daMap.put(key, key);
				if (!item.getResult().equals(da.get("XZ_KEY"))) {
					rs = "你答题目错误,正确答案为" + da.get("XZ_KEY");
					flag = false;
				}
			}
			double score = 0;
			Map<String, Object> tktm = this.tkxxDao.getTktmById(item.getTkId());
			Double tmfz = (Double) tktm.get("TMFZ");
			Map<String, Double> sjxs = this.tkxxDao.getSjxs();
			Double xs = 0d;
			//EXAM_TYPE 原来值 不知道作用
			String examType = (String) tktm.get("MODE");
			String result = "N";
			if (true == flag) {
				result = "Y";
				if (0 == timeFlag) {
					xs = sjxs.get("Sjxs_30s");

				} else if (1 == timeFlag) {
					xs = sjxs.get("Sjxs_2min");
				} else if (2 == timeFlag) {
					xs = sjxs.get("Sjxs_8min");
				}
				score = tmfz * xs;
			} else {// 答题错误
				if ("2".equals(examType)) {// 抢答题 扣分
					score = tmfz * -1;
				}
			}
			UserDetails userDetails = (UserDetails) SecurityContextHolder
					.getContext().getAuthentication().getPrincipal();
			String loginName = userDetails.getUsername();
			Map<String, Object> user = this.tkxxDao
					.getUserByLoginName(loginName);
			UserResult userRs = new UserResult();
			userRs.setType(examType);
			userRs.setUserId((Integer) user.get("ID_"));
			userRs.setAnswer(item.getResult());
			userRs.setEndTime(item.getEndTime());
			userRs.setStartTime(item.getStartTime());
			userRs.setResultScore(score);
			userRs.setResult(result);
			userRs.setTkId(item.getTkId());
			userRs.setExamDetailId(item.getExamDetailId());
			userRs.setResultTime(sec);
			this.examDao.insertUserResult(userRs);

			param.put("rs", rs);
			param.put("userRs", userRs);
			returnValue = ResponseMessage.success(param);
		}else{
			returnValue = ResponseMessage.error("400","该题目还没有录入答案！");
		}
		return returnValue;
	}

	/**
	 * 积分排行榜
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getScoreRank() {
		return this.examDao.getScoreRank();
	}

	/**
	 * 获取某个群组积分榜
	 * @param groupId
	 * @return
	 */
	public List<Map<String, Object>> getScoreRank(Integer groupId) {
		return this.examDao.getScoreRank(groupId);
	}

	/**
	 * 个人在积分排行榜情况
	 * 
	 * @return
	 */
	public Map<String, Object> getUserScoreRank(int userId) {
		return this.examDao.getUserScoreRank(userId);

	}

	/**
	 * 积分排行榜本周
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getScoreRankByWeek() {
		return this.examDao.getScoreRankByWeek();
	}

	public List<Map<String, Object>> getScoreRankByWeek(Integer groupId) {
		return this.examDao.getScoreRankByWeek(groupId);
	}

	/**
	 * 个人在积分排行榜情况本周
	 * 
	 * @return
	 */
	public Map<String, Object> getUserScoreRankByWeek() {
		return this.examDao.getUserScoreRankByWeek(this.getUserId());

	}

	/**
	 * 积分排行榜本月
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getScoreRankByMonth() {
		return this.examDao.getScoreRankByMonth();
	}

	public List<Map<String, Object>> getScoreRankByMonth(Integer groupId) {
		return this.examDao.getScoreRankByMonth(groupId);
	}

	/**
	 * 个人在积分排行榜情况本月
	 * 
	 * @return
	 */
	public Map<String, Object> getUserScoreRankByMonth() {
		return this.examDao.getUserScoreRankByMonth(this.getUserId());

	}

	/**
	 * 积分排行榜本季度
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getScoreRankByQuarter() {
		return this.examDao.getScoreRankByQuarter();
	}

	public List<Map<String, Object>> getScoreRankByQuarter(Integer groupId) {
		return this.examDao.getScoreRankByQuarter(groupId);
	}

	/**
	 * 个人在积分排行榜情况本季度
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserScoreRankByQuarter() {
		return this.examDao.getUserScoreRankByQuarter(this.getUserId());

	}

	/**
	 * 积分排行榜本年
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getScoreRankByYear() {
		return this.examDao.getScoreRankByYear();
	}

	public List<Map<String, Object>> getScoreRankByYear(Integer groupId) {
		return this.examDao.getScoreRankByYear(groupId);
	}

	/**
	 * 个人在积分排行榜情况本年
	 * 
	 * @return
	 */
	public Map<String, Object> getUserScoreRankByYear() {
		return this.examDao.getUserScoreRankByYear(this.getUserId());

	}

	/**
	 * 答题排行榜本周
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getDtScoreRankByWeek() {
		return this.examDao.getDtScoreRankByWeek();
	}

	public List<Map<String, Object>> getDtScoreRankByWeek(Integer groupId) {
		return this.examDao.getDtScoreRankByWeek(groupId);
	}

	/**
	 * 个人在答题排行榜情况本周
	 * 
	 * @return
	 */
	public Map<String, Object> getUserDtScoreRankByWeek() {
		return this.examDao.getUserDtScoreRankByWeek(this.getUserId());

	}

	/**
	 * 答题排行榜本月
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getDtScoreRankByMonth() {
		return this.examDao.getDtScoreRankByMonth();
	}

	public List<Map<String, Object>> getDtScoreRankByMonth(Integer groupId) {
		return this.examDao.getDtScoreRankByMonth(groupId);
	}

	/**
	 * 个人在答题排行榜情况本月
	 * 
	 * @return
	 */
	public Map<String, Object> getUserDtScoreRankByMonth() {
		return this.examDao.getUserDtScoreRankByMonth(this.getUserId());

	}

	/**
	 * 答题排行榜本季度
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getDtScoreRankByQuarter() {
		return this.examDao.getDtScoreRankByQuarter();
	}

	public List<Map<String, Object>> getDtScoreRankByQuarter(Integer groupId) {
		return this.examDao.getDtScoreRankByQuarter(groupId);
	}

	/**
	 * 个人在答题排行榜情况本季度
	 * 
	 * @return
	 */
	public Map<String, Object> getUserDtScoreRankByQuarter() {
		return this.examDao.getUserDtScoreRankByQuarter(this.getUserId());

	}

	/**
	 * 答题排行榜本年
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getDtScoreRankByYear() {
		return this.examDao.getDtScoreRankByYear();
	}

	public List<Map<String, Object>> getDtScoreRankByYear(Integer groupId) {
		return this.examDao.getDtScoreRankByYear(groupId);
	}

	/**
	 * 个人在答题排行榜情况本年
	 * 
	 * @return
	 */
	public Map<String, Object> getUserDtScoreRankByYear() {
		return this.examDao.getUserDtScoreRankByYear(this.getUserId());
	}

	/**
	 * 抢答排行榜本周
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getQdScoreRankByWeek() {
		return this.examDao.getQdScoreRankByWeek();
	}

	public List<Map<String, Object>> getQdScoreRankByWeek(Integer groupId) {
		return this.examDao.getQdScoreRankByWeek(groupId);
	}

	/**
	 * 个人在抢答排行榜情况本周
	 * 
	 * @return
	 */
	public Map<String, Object> getUserQdScoreRankByWeek() {
		return this.examDao.getUserQdScoreRankByWeek(this.getUserId());

	}

	/**
	 * 抢答排行榜本月
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getQdScoreRankByMonth() {
		return this.examDao.getQdScoreRankByMonth();
	}

	public List<Map<String, Object>> getQdScoreRankByMonth(Integer groupId) {
		return this.examDao.getQdScoreRankByMonth(groupId);
	}

	/**
	 * 个人在抢答排行榜情况本月
	 * 
	 * @return
	 */
	public Map<String, Object> getUserQdScoreRankByMonth() {
		return this.examDao.getUserQdScoreRankByMonth(this.getUserId());

	}

	/**
	 * 抢答排行榜本季度
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getQdScoreRankByQuarter() {
		return this.examDao.getQdScoreRankByQuarter();
	}

	public List<Map<String, Object>> getQdScoreRankByQuarter(Integer groupId) {
		return this.examDao.getQdScoreRankByQuarter(groupId);
	}

	/**
	 * 个人在抢答排行榜情况本季度
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserQdScoreRankByQuarter() {
		return this.examDao.getUserQdScoreRankByQuarter(this.getUserId());

	}

	/**
	 * 抢答排行榜本年
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getQdScoreRankByYear() {
		return this.examDao.getQdScoreRankByYear();
	}

	public List<Map<String, Object>> getQdScoreRankByYear(Integer groupId) {
		return this.examDao.getQdScoreRankByYear(groupId);
	}

	/**
	 * 个人在抢答排行榜情况本年
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserQdScoreRankByYear() {

		return this.examDao.getUserQdScoreRankByYear(this.getUserId());

	}

	public Integer getUserId() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder
				.getContext().getAuthentication().getPrincipal();
		String loginName = userDetails.getUsername();
		Map<String, Object> user = this.tkxxDao.getUserByLoginName(loginName);
		Integer userId = (Integer) user.get("ID_");
		return userId;
	}

	public String getUserName() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder
				.getContext().getAuthentication().getPrincipal();
		String loginName = userDetails.getUsername();
		Map<String, Object> user = this.tkxxDao.getUserByLoginName(loginName);
		String userName = (String) user.get("USER_NAME");
		return userName;
	}

	/**
	 * 个人在抢答排行榜情况
	 * 
	 * @return
	 */
	public Map<String, Object> getUserQdScoreRank() {
		return this.examDao.getUserQdScoreRank(this.getUserId());
	}

	/**
	 * 某次考试排行榜
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getExamScoreRank(String examId) {
		return this.examDao.getExamScoreRank(examId);
	}
	/**
	 * 某用户某次考试成绩
	 * @param examId
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserExamScoreRank(String examId,Integer userId) {
		return this.examDao.getUserExamScoreRank(examId, userId);
	}
	public List<Map<String, Object>> getQdScoreRank(Integer groupId) {
		return this.examDao.getQdScoreRank(groupId);
	}

	/**
	 * 答题排行榜本周
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getDtScoreRank() {
		return this.examDao.getDtScoreRank();
	}

	public List<Map<String, Object>> getDtScoreRank(Integer groupId) {
		return this.examDao.getDtScoreRank(groupId);
	}

	/**
	 * 个人在答题排行榜情况
	 * 
	 * @return
	 */
	public Map<String, Object> getUserDtScoreRank() {
		return this.examDao.getUserDtScoreRank(this.getUserId());
	}

	public boolean checkUserPhoto(String loginName) {

		boolean result = false;
		String filePath = "/usr/local/tomcat/app/images/" + loginName
				+ "/avatar.jpg";
		File file = new File(filePath);
		if (file.exists()) {
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * 考试记录按天统计
	 * 
	 * @param userId
	 * @return
	 */
	public List<Map<String, Object>> getExamRecordList(Integer userId) {
		return this.examDao.getExamRecordList(userId);
	}
	/**
	 * 某个用户某个群组积分榜
	 * @param groupId
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserGroupScoreRank(Integer groupId,Integer userId) {
		return this.examDao.getUserGroupScoreRank(groupId, userId);
	}

	/**
	 * 考试记录明细
	 * 
	 * @param userId
	 * @param day
	 * @return
	 */
	public List<Map<String, Object>> getExamRecordDetail(Integer userId,
			String examId) {
		return this.examDao.getExamRecordDetail(userId, examId);
	}

	/**
	 * 用户参与的考试
	 * @param userId
	 * @return
	 */
	public List<Map<String, Object>> getUserExam(Integer userId) {
		return this.examDao.getUserExam(userId);
	}
	public List<Map<String, Object>> getScoreGroupByFlId(Integer userId) {
		return this.examDao.getScoreGroupByFlId(userId);
	}
}
