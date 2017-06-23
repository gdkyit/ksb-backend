package gov.hygs.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gdky.restful.config.Constants;
import com.gdky.restful.entity.ResponseMessage;

import gov.hygs.entity.ExamItem;
import gov.hygs.entity.LoudRecord;
import gov.hygs.service.ExamService;
import gov.hygs.service.TkxxService;
import gov.hygs.service.UserGoupService;
import gov.hygs.service.ZskService;
@RestController
@RequestMapping(value = Constants.URI_API_PREFIX)
public class DtxxController {
	

	@Resource
	private TkxxService tkxxService;
	@Resource
	private ExamService examService;
	@Resource
	private UserGoupService userGoupService;
	@Resource
	private ZskService zskService;

	
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
	@Transactional
	@RequestMapping(value = "/checkDtxx", method = RequestMethod.POST)
	public ResponseEntity<?> doCheckDtxx(@RequestBody ExamItem item) throws AuthenticationException {
		ResponseMessage rs=this.tkxxService.doCheckDtxx(item);
		return new ResponseEntity<>(rs, HttpStatus.OK);
	}


	/**
	 * 题目点赞
	 * 
	 * @param rec
	 * @return
	 * @throws AuthenticationException
	 */
	@Transactional
	@RequestMapping(value = "/laudRecord", method = RequestMethod.POST)
	public ResponseEntity<?> doTmLoudRecord(@RequestBody LoudRecord rec) throws AuthenticationException {
		String rs = "点赞成功";
		this.tkxxService.insertLaudRecord(rec);
		Map<String, String> param = new HashMap<String, String>();
		param.put("rs", rs);
		return new ResponseEntity<>(ResponseMessage.success(param), HttpStatus.OK);
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

	
	@RequestMapping(value = "/userZskTs", method = RequestMethod.GET)
	public ResponseEntity<?> getUserZskTs() {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		return new ResponseEntity<>(ResponseMessage.success(this.zskService.getZsk(userId)), HttpStatus.OK);
	}
}
