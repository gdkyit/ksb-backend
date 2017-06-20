package gov.hygs.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gdky.restful.config.Constants;
import com.gdky.restful.entity.ResponseMessage;

import gov.hygs.entity.ExamItem;
import gov.hygs.entity.UserResult;
import gov.hygs.service.ExamService;
import gov.hygs.service.TkxxService;
import gov.hygs.service.UserGoupService;
import gov.hygs.service.ZskService;
@RestController
@RequestMapping(value = Constants.URI_API_PREFIX)
public class ExamController {
	

	@Resource
	private TkxxService tkxxService;
	@Resource
	private ExamService examService;
	@Resource
	private UserGoupService userGoupService;
	@Resource
	private ZskService zskService;

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

}
