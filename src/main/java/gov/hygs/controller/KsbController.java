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
import org.springframework.http.HttpStatus;
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
import com.gdky.restful.entity.ResponseMessage;

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
	public ResponseEntity<?> onSubmit(@RequestParam("file") MultipartFile file) throws IOException {
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
		return new ResponseEntity<>(ResponseMessage.success("upload OK"), HttpStatus.OK);

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
		return new ResponseEntity<>(ResponseMessage.success(result), HttpStatus.OK);
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
		return new ResponseEntity<>(ResponseMessage.success(rs), HttpStatus.OK);
	}

	/**
	 * 通过题目ID查询题目选择项
	 * 
	 * @param tkId
	 * @return
	 */
	@RequestMapping(value = "/tkxzx", method = RequestMethod.GET)
	public ResponseEntity<?> getTkxzxByTkId(@RequestParam("tkId") String tkId) {
		return new ResponseEntity<>(ResponseMessage.success(this.tkxxService.getTkxzxByTkId(tkId)), HttpStatus.OK);
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
		return new ResponseEntity<>(ResponseMessage.success(this.tkxxService.doCheckDtxx(item)), HttpStatus.OK);
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
		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
	}

	/**
	 * 查询当前考试 当前时间当前人所在群组未进行的考试
	 * 
	 * @return
	 */
	@RequestMapping(value = "/exam", method = RequestMethod.GET)
	public ResponseEntity<?> getExam(@RequestParam("type") String type) {
		return new ResponseEntity<>(ResponseMessage.success(this.examService.getExam(type)), HttpStatus.OK);
	}

	/**
	 * 通过考试ID查询题目
	 * 
	 * @param examId
	 * @return
	 */
	@RequestMapping(value = "/examDetail", method = RequestMethod.GET)
	public ResponseEntity<?> getExamDetail(@RequestParam("examId") Integer examId) {
		List<Map<String, Object>> ls = this.examService.getExamDetailByExamId(examId);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("examDetail", ls);
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		List<UserResult> urs = this.examService.getUserRs(examId, userId);
		param.put("userRs", urs);
		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);

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
		return new ResponseEntity<>(ResponseMessage.success(this.examService.doCheckExamItem(item)), HttpStatus.OK);
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
		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
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
		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
	}

	@RequestMapping(value = "/persionInfo", method = RequestMethod.GET)
	public ResponseEntity<?> getPersionInfo(@RequestParam("userId") Integer userId) {

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("userInfo", this.tkxxService.getUserByUserId(userId));
		param.put("userScore", this.examService.getScoreGroupByFlId(userId));
		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
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

		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
	}

	/**
	 * 答题排行榜
	 * 
	 * @return
	 */
	@RequestMapping(value = "/dtphb", method = RequestMethod.GET)
	public ResponseEntity<?> getDtphb() {
		// Integer userId = (Integer)
		// this.tkxxService.getCurrentUser().get("ID_");
		// List<Map<String, Object>> userGroup =
		// this.userGoupService.getUserGroup(userId);
		Map<String, List<Map<String, Object>>> param = new HashMap<String, List<Map<String, Object>>>();
		// 答题学习排行榜
		param.put("dtScoreRank", this.examService.getDtScoreRank());// 累计
		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
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

		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
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
		return new ResponseEntity<>(ResponseMessage.success(this.tkxxService.getCurrentUser()), HttpStatus.OK);
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
		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
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
		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
	}

	/**
	 * 查看用户群组
	 * 
	 * @return
	 */
	@RequestMapping(value = "/userGroup", method = RequestMethod.GET)
	public ResponseEntity<?> getUserGroup() {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return new ResponseEntity<>(ResponseMessage.success(this.userGoupService.getUserGroup(userId)), HttpStatus.OK);
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
		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
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
		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
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
		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
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
		return new ResponseEntity<>(ResponseMessage.success(this.tkxxService.getUserTkfl(userId, parentId)),
				HttpStatus.OK);
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
		return new ResponseEntity<>(ResponseMessage.success(this.tkxxService.getUserLaud(userId)), HttpStatus.OK);
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
		return new ResponseEntity<>(ResponseMessage.success(this.examService.getExamRecordList(userId)), HttpStatus.OK);
	}

	/**
	 * 考试记录明细
	 * 
	 * @param userId
	 * @param day
	 * @return
	 */
	@RequestMapping(value = "/examRecordDetail", method = RequestMethod.GET)
	public ResponseEntity<?> getExamRecordDetail(@RequestParam("examId") String examId) {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return new ResponseEntity<>(ResponseMessage.success(this.examService.getExamRecordDetail(userId, examId)),
				HttpStatus.OK);
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
		return new ResponseEntity<>(ResponseMessage.success(this.tkxxService.getDtxxRecordList(userId)), HttpStatus.OK);
	}

	/**
	 * 答题学习记录明细
	 * 
	 * @param userId
	 * @param day
	 * @return
	 */
	@RequestMapping(value = "/dtxxRecordDetail", method = RequestMethod.GET)
	public ResponseEntity<?> getDtxxRecordDetail(@RequestParam("day") String day) {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return new ResponseEntity<>(ResponseMessage.success(this.tkxxService.getDtxxRecordDetail(userId, day)),
				HttpStatus.OK);
	}

	/**
	 * 新手任务题目
	 * 
	 * @param userId
	 * @param day
	 * @return
	 */

	@RequestMapping(value = "/newTaskTm", method = RequestMethod.GET)
	public ResponseEntity<?> getNewTaskTm() {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return new ResponseEntity<>(ResponseMessage.success(this.tkxxService.getNewTaskTm(userId)), HttpStatus.OK);
	}

	@RequestMapping(value = "/userZskTs", method = RequestMethod.GET)
	public ResponseEntity<?> getUserZskTs() {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return new ResponseEntity<>(ResponseMessage.success(this.zskService.getZsk(userId)), HttpStatus.OK);
	}

	@RequestMapping(value = "/userExam", method = RequestMethod.GET)
	public ResponseEntity<?> getUserExam() {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return new ResponseEntity<>(ResponseMessage.success(this.examService.getUserExam(userId)), HttpStatus.OK);
	}

	/**
	 * 查看用户积分按题库分类统计
	 * 
	 * @return
	 */
	@RequestMapping(value = "/userScoreGroupByFlId", method = RequestMethod.GET)
	public ResponseEntity<?> getUserScoreGroupByFlId(@RequestParam("userId") Integer userId) {
		return new ResponseEntity<>(ResponseMessage.success(this.examService.getScoreGroupByFlId(userId)),
				HttpStatus.OK);
	}

	/**
	 * 根据userId查看收到的题目纠错
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/userTmJiuChuo", method = RequestMethod.GET)
	public ResponseEntity<?> getUserTmjiuchuo() {
		return new ResponseEntity<>(ResponseMessage.success(this.tkxxService.getUserTmjiuchuo()), HttpStatus.OK);
	}

	/**
	 * 查询用户出题贡献值
	 * 
	 * @return
	 */
	@RequestMapping(value = "/userGxz", method = RequestMethod.GET)
	public ResponseEntity<?> getUserGxz() {
		return new ResponseEntity<>(ResponseMessage.success(this.tkxxService.getUserGxz()), HttpStatus.OK);
	}

	/**
	 * 查看答题学习分类排名
	 * 
	 * @param flId
	 * @return
	 */
	@RequestMapping(value = "/flpm", method = RequestMethod.GET)
	public ResponseEntity<?> getFlpm(@RequestParam("flId") String flId) {
		return new ResponseEntity<>(ResponseMessage.success(this.tkxxService.getFlpm(flId)), HttpStatus.OK);
	}

	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public ResponseEntity<?> test() {
		return new ResponseEntity<>(ResponseMessage.success(this.tkxxService.test()), HttpStatus.OK);
	}

}
