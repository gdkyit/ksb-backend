package gov.hygs.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gdky.restful.config.Constants;

import gov.hygs.entity.ExamItem;
import gov.hygs.entity.LoudRecord;
import gov.hygs.entity.UserEntity;
import gov.hygs.entity.UserGroup;
import gov.hygs.entity.UserResult;
import gov.hygs.entity.UserTkdy;
import gov.hygs.service.ExamService;
import gov.hygs.service.TkxxService;
import gov.hygs.service.UserGoupService;
import gov.hygs.service.ZskService;

@RestController
@RequestMapping(value = Constants.URI_API_PREFIX)
public class KsbController {
	
	@Resource
	private TkxxService tkxxService;
	@Resource
	private ExamService examService;
	@Resource
	private UserGoupService userGoupService;
	@Resource
	private ZskService zskService;
	
	
	@ResponseBody
	@RequestMapping(path = "/upload", method = RequestMethod.POST)
	public String onSubmit(@RequestParam("file") MultipartFile file) throws IOException {
		if (null != file) {
			// String path ="/Users/david/Documents/github/sxbapp";//
			String path = "/usr/local/tomcat/app/images";

			File uploadDir = new File(path);
			if (!uploadDir.exists()) {
				uploadDir.mkdirs();
			}
			String userName = SecurityContextHolder.getContext().getAuthentication().getName();

			File userDir = new File(path + File.separator + userName);
			if (!userDir.exists()) {
				userDir.mkdirs();
			}
			File storageFile = new File(path + File.separator + userName + File.separator + "avatar.jpg");
			if (!storageFile.exists()) {
				storageFile.createNewFile();
			} else {
				storageFile.delete();
				storageFile.createNewFile();
			}
			IOUtils.copy(file.getInputStream(), new FileOutputStream(storageFile));
		}

		return "upload OK";
	}

	/**
	 * 题目分类订阅
	 * 
	 * @param parentId
	 * @return
	 */
	@RequestMapping(value = "/userTkfl", method = RequestMethod.GET)
	public ResponseEntity<?> getTkfl(@RequestParam("parentId") String parentId) {

		List<Map<String, Object>> rs = null;
		if (StringUtils.isEmpty(parentId)) {

			rs = this.tkxxService.getTopTmfl();
		} else {
			rs = this.tkxxService.getTmfl(Integer.parseInt(parentId));
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("rs", rs);
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		result.put("userTkfldy", this.tkxxService.getUserTkFLdy(userId));
		return ResponseEntity.ok(result);
	}

	/**
	 * 根据题库分类查询随机查询5条题目
	 * 
	 * @param parentId
	 * @return
	 */
	@RequestMapping(value = "/tk", method = RequestMethod.GET)
	public ResponseEntity<?> getTk(@RequestParam("parentId") String parentId) {

		List<Map<String, Object>> rs = null;
		rs = this.tkxxService.getTktmByFlId(Integer.parseInt(parentId));
		return ResponseEntity.ok(rs);
	}

	/**
	 * 通过题目ID查询题目选择项
	 * 
	 * @param tkId
	 * @return
	 */
	@RequestMapping(value = "/tkxzx", method = RequestMethod.GET)
	public List<Map<String, Object>> getTkxzxByTkId(@RequestParam("tkId") String tkId) {
		return this.tkxxService.getTkxzxByTkId(tkId);
	}

	/**
	 * 检查答题学习题目
	 * 
	 * @param item
	 * @return
	 * @throws AuthenticationException
	 */
	@RequestMapping(value = "/checkDtxx", method = RequestMethod.POST)
	public ResponseEntity<?> doCheckDtxx(@RequestBody ExamItem item) throws AuthenticationException {
		return ResponseEntity.ok(this.tkxxService.doCheckDtxx(item));
	}

	/**
	 * 题目点赞
	 * 
	 * @param rec
	 * @return
	 * @throws AuthenticationException
	 */
	@RequestMapping(value = "/laudRecord", method = RequestMethod.POST)
	public ResponseEntity<?> doTmLoudRecord(@RequestBody LoudRecord rec) throws AuthenticationException {
		String rs = "点赞成功";
		this.tkxxService.insertLaudRecord(rec);
		Map<String, String> param = new HashMap<String, String>();
		param.put("rs", rs);
		return ResponseEntity.ok(param);
	}

	/**
	 * 查询当前考试 当前时间当前人所在群组未进行的考试
	 * 
	 * @return
	 */
	@RequestMapping(value = "/exam", method = RequestMethod.GET)
	public List<Map<String, Object>> getExam(@RequestParam("type") String type) {
		return this.examService.getExam(type);
	}

	/**
	 * 通过考试ID查询题目
	 * 
	 * @param examId
	 * @return
	 */
	@RequestMapping(value = "/examDetail", method = RequestMethod.GET)
	public Map<String, Object> getExamDetail(@RequestParam("examId") Integer examId) {
		List<Map<String, Object>> ls = this.examService.getExamDetailByExamId(examId);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("examDetail", ls);
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		List<UserResult> urs = this.examService.getUserRs(examId, userId);
		param.put("userRs", urs);
		return param;

	}

	/**
	 * 考试校验
	 * 
	 * @param item
	 * @return
	 * @throws AuthenticationException
	 */
	@RequestMapping(value = "/checkExamItem", method = RequestMethod.POST)
	public ResponseEntity<?> doCheckExamItem(@RequestBody ExamItem item) throws AuthenticationException {
		return ResponseEntity.ok(this.examService.doCheckExamItem(item));
	}

	/**
	 * 个人排行榜情况
	 * 
	 * @return
	 */
	@RequestMapping(value = "/checkUserPhoto", method = RequestMethod.GET)
	public ResponseEntity<?> checkUserPhoto(@RequestParam("loginName") String loginName) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("rs", this.examService.checkUserPhoto(loginName));
		return ResponseEntity.ok(param);
	}

	/**
	 * 当前人排行榜情况
	 * 
	 * @return
	 */
	@RequestMapping(value = "/grphb", method = RequestMethod.GET)
	public ResponseEntity<?> getGrphb() {
		Map<String, Object> param = new HashMap<String, Object>();
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		param.put("hasPhoto", this.examService.checkUserPhoto(userName));
		param.put("userName", this.examService.getUserName());

		// 积分排行榜
		param.put("scoreRank", this.examService.getUserScoreRank(userId));// 累计

		param.put("totalUserResult", this.tkxxService.getTotalUserResultByUserId(userId));
		List<Map<String, Object>> ls = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> userGroup = this.userGoupService.getUserGroup(userId);
		if (null != userGroup) {
			for (Map<String, Object> group : userGroup) {
				Map<String, Object> param1 = new HashMap<String, Object>();
				Integer groupId = (Integer) group.get("ID_");
				param1.put("group", group);
				param1.put("scoreRank", this.examService.getScoreRank(groupId));// 累计
				param1.put("userScoreRank", this.examService.getUserGroupScoreRank(groupId, userId));
				ls.add(param1);
			}
		}
		param.put("userGroupRank", ls);
		return ResponseEntity.ok(param);
	}

	@RequestMapping(value = "/persionInfo", method = RequestMethod.GET)
	public ResponseEntity<?> getPersionInfo(@RequestParam("userId") Integer userId) {

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("userInfo", this.tkxxService.getUserByUserId(userId));
		param.put("userScore", this.examService.getScoreGroupByFlId(userId));
		return ResponseEntity.ok(param);
	}

	/**
	 * 积分排行榜
	 * 
	 * @return
	 */
	@RequestMapping(value = "/jfphb", method = RequestMethod.GET)
	public ResponseEntity<?> getJfphb() {
		Map<String, List<Map<String, Object>>> param = new HashMap<String, List<Map<String, Object>>>();

		// 积分排行榜
		param.put("scoreRank", this.examService.getScoreRank());// 累计
		// Integer userId
		// =(Integer)this.tkxxService.getCurrentUser().get("ID_");
		// List<Map<String,Object>> userGroup
		// =this.userGoupService.getUserGroup(userId);
		// List<Map<String,Object>> ls = new ArrayList<Map<String,Object>>();
		// if(null != userGroup){
		// for(Map<String,Object> group:userGroup){
		// Map<String,Object> param1 = new HashMap<String,Object>();
		// Integer groupId = (Integer)group.get("ID_");
		// param1.put("group", group);
		// param1.put("scoreRank", this.examService.getScoreRank(groupId));//累计

		// ls.add(param1);
		// }
		// }
		// param.put("userGroupRank", ls);
		return ResponseEntity.ok(param);
	}

	/**
	 * 答题排行榜
	 * 
	 * @return
	 */
	@RequestMapping(value = "/dtphb", method = RequestMethod.GET)
	public ResponseEntity<?> getDtphb() {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		List<Map<String, Object>> userGroup = this.userGoupService.getUserGroup(userId);
		Map<String, List<Map<String, Object>>> param = new HashMap<String, List<Map<String, Object>>>();
		// 答题学习排行榜
		param.put("dtScoreRank", this.examService.getDtScoreRank());// 累计

		// List<Map<String,Object>> ls = new ArrayList<Map<String,Object>>();
		// if(null != userGroup){
		// for(Map<String,Object> group:userGroup){
		// Map<String,Object> param1 = new HashMap<String,Object>();
		// Integer groupId = (Integer)group.get("ID_");
		// param1.put("group", group);
		// param1.put("dtScoreRank",
		// this.examService.getDtScoreRank(groupId));//累计
		// param1.put("dtScoreRankByMonth",
		// this.examService.getDtScoreRankByMonth(groupId));//本月
		// param1.put("dtScoreRankByQuarter",
		// this.examService.getDtScoreRankByQuarter(groupId));//本季度
		// param1.put("dtScoreRankByWeek",
		// this.examService.getDtScoreRankByWeek(groupId));//本周
		// param1.put("dtScoreRankByYear",
		// this.examService.getDtScoreRankByYear(groupId));//本年
		// ls.add(param1);
		// }
		// }
		// param.put("userGroupRank", ls);
		return ResponseEntity.ok(param);
	}

	/**
	 * 抢答排行榜
	 * 
	 * @return
	 */
	@RequestMapping(value = "/qdphb", method = RequestMethod.GET)
	public ResponseEntity<?> getQdphb(@RequestParam("examId") String examId) {
		Map<String, Object> param = new HashMap<String, Object>();
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		// 考试排行榜
		param.put("qdScoreRank", this.examService.getExamScoreRank(examId));
		param.put("userExamRank", this.examService.getUserExamScoreRank(examId, userId));

		return ResponseEntity.ok(param);
	}

	public void initUser() throws Exception {
		this.tkxxService.initUser();
	}

	public void initDept() throws Exception {
		this.tkxxService.initDept();
	}

	public void initGdUser() throws Exception {
		this.tkxxService.initGdUser();
	}

	/**
	 * 当前用户信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/currentUser", method = RequestMethod.GET)
	public ResponseEntity<?> getCurrentUser() {
		return ResponseEntity.ok(this.tkxxService.getCurrentUser());
	}

	/**
	 * 个人信息维护
	 * 
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/updateUser", method = RequestMethod.POST)
	public ResponseEntity<?> updateUser(@RequestBody UserEntity user) {
		this.tkxxService.updateUser(user);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("rs", "ok");
		return ResponseEntity.ok(param);
	}

	/**
	 * 修改密码
	 * 
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/updateUserPwd", method = RequestMethod.POST)
	public ResponseEntity<?> updateUserPwd(@RequestBody UserEntity user) {
		this.tkxxService.updateUserPwd(user);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("rs", "ok");
		return ResponseEntity.ok(param);
	}

	/**
	 * 查看用户群组
	 * 
	 * @return
	 */
	@RequestMapping(value = "/userGroup", method = RequestMethod.GET)
	public ResponseEntity<?> getUserGroup() {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return ResponseEntity.ok(this.userGoupService.getUserGroup(userId));
	}

	/**
	 * 查看所有群组
	 * 
	 * @return
	 * @throws AuthenticationException
	 */
	@RequestMapping(value = "/groups", method = RequestMethod.GET)
	public ResponseEntity<?> getGroups() throws AuthenticationException {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("groups", this.userGoupService.getGroups());
		param.put("userGroups", this.userGoupService.getUserGroup(userId));
		return ResponseEntity.ok(param);
	}

	/**
	 * 群组申请
	 * 
	 * @param userGroups
	 * @return
	 */
	@RequestMapping(value = "/saveUserGroups", method = RequestMethod.POST)
	public ResponseEntity<?> saveUserGroups(@RequestBody List<UserGroup> userGroups) {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		this.userGoupService.insertUserGroup(userGroups, userId);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("rs", "ok");
		return ResponseEntity.ok(param);
	}

	/**
	 * 群组申请
	 * 
	 * @param
	 * @return
	 */
	@RequestMapping(value = "/tkfldy", method = RequestMethod.POST)
	public ResponseEntity<?> saveTkfldy(@RequestBody List<UserTkdy> userTkdys) {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		if (null != userTkdys) {
			this.tkxxService.insertTkdy(userTkdys, userId);
		}
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("rs", "ok");
		return ResponseEntity.ok(param);
	}

	/**
	 * 题目分类
	 * 
	 * @param parentId
	 * @return
	 */
	@RequestMapping(value = "/tkfl", method = RequestMethod.GET)
	public ResponseEntity<?> getUserTkfl(@RequestParam("parentId") String parentId) {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return ResponseEntity.ok(this.tkxxService.getUserTkfl(userId, parentId));
	}

	/**
	 * 
	 * 查看用户收到的点赞
	 * 
	 * @return
	 */
	@RequestMapping(value = "/userLaud", method = RequestMethod.GET)
	public ResponseEntity<?> getUserLaud() {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return ResponseEntity.ok(this.tkxxService.getUserLaud(userId));
	}

	/**
	 * 考试记录按天统计
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/examRecordList", method = RequestMethod.GET)
	public ResponseEntity<?> getExamRecordList() {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return ResponseEntity.ok(this.examService.getExamRecordList(userId));
	}

	/**
	 * 考试记录明细
	 * 
	 * @param userId
	 * @param day
	 * @return
	 */
	@RequestMapping(value = "/examRecordDetail", method = RequestMethod.GET)
	public List<Map<String, Object>> getExamRecordDetail(@RequestParam("examId") String examId) {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return this.examService.getExamRecordDetail(userId, examId);
	}

	/**
	 * 答题学习记录按天统计
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/dtxxRecordList", method = RequestMethod.GET)
	public ResponseEntity<?> getDtxxRecordList() {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return ResponseEntity.ok(this.tkxxService.getDtxxRecordList(userId));
	}

	/**
	 * 答题学习记录明细
	 * 
	 * @param userId
	 * @param day
	 * @return
	 */
	@RequestMapping(value = "/dtxxRecordDetail", method = RequestMethod.GET)
	public List<Map<String, Object>> getDtxxRecordDetail(@RequestParam("day") String day) {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return this.tkxxService.getDtxxRecordDetail(userId, day);
	}

	/**
	 * 新手任务题目
	 * 
	 * @param userId
	 * @param day
	 * @return
	 */

	@RequestMapping(value = "/newTaskTm", method = RequestMethod.GET)
	public List<Map<String, Object>> getNewTaskTm() {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return this.tkxxService.getNewTaskTm(userId);
	}

	@RequestMapping(value = "/userZskTs", method = RequestMethod.GET)
	public List<Map<String, Object>> getUserZskTs() {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return this.zskService.getZsk(userId);
	}

	@RequestMapping(value = "/userExam", method = RequestMethod.GET)
	public List<Map<String, Object>> getUserExam() {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return this.examService.getUserExam(userId);
	}

	/**
	 * 查看用户积分按题库分类统计
	 * 
	 * @return
	 */
	@RequestMapping(value = "/userScoreGroupByFlId", method = RequestMethod.GET)
	public ResponseEntity<?> getUserScoreGroupByFlId(@RequestParam("userId") Integer userId) {
		return ResponseEntity.ok(this.examService.getScoreGroupByFlId(userId));
	}

	/**
	 * 根据userId查看收到的题目纠错
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/userTmJiuChuo", method = RequestMethod.GET)
	public List<Map<String, Object>> getUserTmjiuchuo() {
		return this.tkxxService.getUserTmjiuchuo();
	}

	/**
	 * 查询用户出题贡献值
	 * 
	 * @return
	 */
	@RequestMapping(value = "/userGxz", method = RequestMethod.GET)
	public Map<String, Object> getUserGxz() {
		return this.tkxxService.getUserGxz();
	}

	/**
	 * 查看答题学习分类排名
	 * 
	 * @param flId
	 * @return
	 */
	@RequestMapping(value = "/flpm", method = RequestMethod.GET)
	public List<Map<String, Object>> getFlpm(@RequestParam("flId") String flId) {
		return this.tkxxService.getFlpm(flId);
	}
}
