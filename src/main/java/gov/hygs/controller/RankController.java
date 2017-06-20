package gov.hygs.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gdky.restful.config.Constants;
import com.gdky.restful.entity.ResponseMessage;

import gov.hygs.service.ExamService;
import gov.hygs.service.TkxxService;
import gov.hygs.service.UserGoupService;
import gov.hygs.service.ZskService;

@RestController
@RequestMapping(value = Constants.URI_API_PREFIX)
public class RankController {

	@Resource
	private TkxxService tkxxService;
	@Resource
	private ExamService examService;
	@Resource
	private UserGoupService userGoupService;
	@Resource
	private ZskService zskService;

	/**
	 * 积分榜个人秀
	 * 
	 * @return
	 */
	@RequestMapping(value = "/grx", method = RequestMethod.GET)
	public ResponseEntity<?> getGrx() {
		Map<String, Object> param = new HashMap<String, Object>();
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		List<Map<String, Object>> ls = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> userGroup = this.userGoupService.getUserGroup(userId);
		if (null != userGroup) {
			for (Map<String, Object> group : userGroup) {
				Map<String, Object> param1 = new HashMap<String, Object>();
				Integer groupId = (Integer) group.get("ID_");
				String isDefault = (String) group.get("is_default");
				if (isDefault.equals("Y")) {
					param1.put("group", group);
					// param1.put("scoreRank",
					// this.examService.getScoreRank(groupId));// 累计
					param1.put("userScoreRank", this.examService.getUserGroupScoreRank(groupId, userId));
					ls.add(param1);
				}
			}
		}
		param.put("totalUserResult", this.tkxxService.getTotalUserResultByUserId(userId));
		param.put("userGroupRank", ls);
		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
	}

	@RequestMapping(value = "/jfb", method = RequestMethod.GET)
	public ResponseEntity<?> getJfb() {
		Map<String, Object> param = new HashMap<String, Object>();
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		List<Map<String, Object>> userGroup = this.userGoupService.getUserGroup(userId);
		if (null != userGroup) {
			for (Map<String, Object> group : userGroup) {
				Integer groupId = (Integer) group.get("ID_");
				String isDefault = (String) group.get("is_default");
				if (isDefault.equals("Y")) {
					param.put("group", group);
					param.put("scoreRank", this.examService.getScoreRank(groupId));// 累计
					param.put("userScoreRank", this.examService.getUserGroupScoreRank(groupId, userId));
				}
			}
		}
		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
	}

	@RequestMapping(value = "/qzb", method = RequestMethod.GET)
	public ResponseEntity<?> getJfbByQz(@RequestParam("groupId") Integer groupId) {
		Map<String, Object> param = new HashMap<String, Object>();
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		param.put("scoreRank", this.examService.getScoreRank(groupId));// 累计
		param.put("userScoreRank", this.examService.getUserGroupScoreRank(groupId, userId));
		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
	}

	@RequestMapping(value = "/userExam", method = RequestMethod.GET)
	public ResponseEntity<?> getUserExam() {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return new ResponseEntity<>(ResponseMessage.success(this.examService.getUserExam(userId)), HttpStatus.OK);
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

	/**
	 * 用户订阅题目分类
	 * 
	 * @param parentId
	 * @return
	 */
	@RequestMapping(value = "/tkfl", method = RequestMethod.GET)
	public ResponseEntity<?> getUserTkfl() {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return new ResponseEntity<>(ResponseMessage.success(this.tkxxService.getUserTkfl(userId)), HttpStatus.OK);
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
	
	/**
	 * 获取头像
	 * 
	 * @return
	 */
	@RequestMapping(value = "/checkUserPhoto", method = RequestMethod.GET)
	public ResponseEntity<?> checkUserPhoto(@RequestParam("loginName") String loginName) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("rs", this.examService.checkUserPhoto(loginName));
		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
	}
}
