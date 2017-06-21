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

////////////////////
//////已废弃	
////////////////////
//@RestController
//@RequestMapping(value = Constants.URI_API_PREFIX)
public class KsbController {
	@Resource
	private TkxxService tkxxService;
	@Resource
	private ExamService examService;
	@Resource
	private UserGoupService userGoupService;
	@Resource
	private ZskService zskService;




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

}
