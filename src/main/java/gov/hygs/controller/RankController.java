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
	 * 积分榜
	 * 
	 * @return
	 */
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
		ResponseMessage rs = null;
		if (param.size() > 0) {
			rs = ResponseMessage.success(param);
		} else {
			rs = ResponseMessage.error("400", "没设置默认积分榜");
		}
		return new ResponseEntity<>(rs, HttpStatus.OK);
	}

	/**
	 * 群组榜
	 * 
	 * @param groupId
	 * @return
	 */
	@RequestMapping(value = "/qzb", method = RequestMethod.GET)
	public ResponseEntity<?> getQzb(@RequestParam("groupId") Integer groupId) {
		Map<String, Object> param = new HashMap<String, Object>();
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		param.put("scoreRank", this.examService.getScoreRank(groupId));// 累计
		param.put("userScoreRank", this.examService.getUserGroupScoreRank(groupId, userId));
		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
	}

	/**
	 * 用户已参与的考试列表
	 * 
	 * @return
	 */
	@RequestMapping(value = "/userExam", method = RequestMethod.GET)
	public ResponseEntity<?> getUserExam() {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return new ResponseEntity<>(ResponseMessage.success(this.examService.getUserExam(userId)), HttpStatus.OK);
	}

	/**
	 * 考试榜
	 * 
	 * @return
	 */
	@RequestMapping(value = "/ksb", method = RequestMethod.GET)
	public ResponseEntity<?> getKsb(@RequestParam("examId") String examId) {
		Map<String, Object> param = new HashMap<String, Object>();
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		// 考试排行榜
		param.put("examRank", this.examService.getExamScoreRank(examId));
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
		Map<String, Object> param = new HashMap<String, Object>();
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		// 考试排行榜
		param.put("dtxxRank",this.tkxxService.getFlpm(flId));
		param.put("userDtxxRank", this.tkxxService.getUserDtxxSroceRank(flId, userId));

		
		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
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

	@RequestMapping(value = "/personalInfo", method = RequestMethod.GET)
	public ResponseEntity<?> getPersonalInfo(@RequestParam("userId") Integer userId) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("userInfo", this.tkxxService.getUserByUserId(userId));
		param.put("userScore", this.examService.getScoreGroupByFlId(userId));
		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
	}

}
