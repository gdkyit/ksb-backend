package gov.hygs.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
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
import com.gdky.restful.security.CustomUserDetails;

import gov.hygs.entity.UserEntity;
import gov.hygs.entity.UserGroup;
import gov.hygs.entity.UserTkdy;
import gov.hygs.service.ExamService;
import gov.hygs.service.TkxxService;
import gov.hygs.service.UserGoupService;
import gov.hygs.service.ZskService;

@RestController
@RequestMapping(value = Constants.URI_API_PREFIX)
public class PersonalController {

	@Resource
	private TkxxService tkxxService;
	@Resource
	private ExamService examService;
	@Resource
	private UserGoupService userGoupService;
	@Resource
	private ZskService zskService;
	/**
	 * 当前用户信息
	 * 用户贡献值
	 * 
	 * @return
	 */
	@RequestMapping(value = "/currentUser", method = RequestMethod.GET)
	public ResponseEntity<?> getCurrentUser() {
		Map<String, Object> param = new HashMap<String, Object>();
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		param.put("userInfo",this.tkxxService.getCurrentUser());
		param.put("userGxz",this.tkxxService.getUserGxz(userId));
		List<Map<String, Object>> userGroup = this.userGoupService.getUserGroup(userId);
		if (null != userGroup) {
			for (Map<String, Object> group : userGroup) {
				Integer groupId = (Integer) group.get("ID_");
				String isDefault = (String) group.get("is_default");
				if (isDefault!=null&&isDefault.equals("Y")) {
					param.put("group", group);
					param.put("userScoreRank", this.examService.getUserGroupScoreRank(groupId, userId));
				}
			}

		}
		ResponseMessage rs = null;
		if (param.get("group")!=null) {
			param.put("totalUserResult", this.tkxxService.getTotalUserResultByUserId(userId));
			rs = ResponseMessage.success(param);
		} else {
			param.put("userScoreRank","没设置默认积分榜无法查看个人排名信息！");
			rs = ResponseMessage.success(param);
		}
		return new ResponseEntity<>(rs, HttpStatus.OK);
	}

	
	/**
	 * 个人信息维护
	 * 
	 * @param user
	 * @return
	 */
	@Transactional
	@RequestMapping(value = "/updateUser", method = RequestMethod.POST)
	public ResponseEntity<?> updateUser(@RequestBody UserEntity user) {
		this.tkxxService.updateUser(user);
		return new ResponseEntity<>(ResponseMessage.success("OK"), HttpStatus.OK);
	}

	/**
	 * 修改密码
	 * 
	 * @param user
	 * @return
	 */
	@Transactional
	@RequestMapping(value = "/updateUserPwd", method = RequestMethod.POST)
	public ResponseEntity<?> updateUserPwd(@RequestBody UserEntity user) {
		this.tkxxService.updateUserPwd(user);
		return new ResponseEntity<>(ResponseMessage.success("OK"), HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(path = "/upload", method = RequestMethod.POST)
	public ResponseEntity<?> onSubmit(@RequestParam("file") MultipartFile file) throws IOException {
		
		if (null != file) {
			CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
				    .getAuthentication()
				    .getPrincipal();
			String path = Constants.UPLOAD_LOCATION;

			File uploadDir = new File(path);
			if (!uploadDir.exists()) {
				uploadDir.mkdirs();
			}
			String userName = userDetails.getLoginName();

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
			
			userGoupService.updateUpload(userDetails.getId_(),"/" + userName+ "/avatar.jpg");
			
		}
		return new ResponseEntity<>(ResponseMessage.success("upload OK"), HttpStatus.OK);

	}

	/**
	 * 题库分类订阅
	 * 
	 * @param
	 * @return
	 */
	@Transactional
	@RequestMapping(value = "/tkfldy", method = RequestMethod.POST)
	public ResponseEntity<?> saveTkfldy(@RequestBody List<UserTkdy> userTkdys) {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		if (null != userTkdys) {
			this.tkxxService.insertTkdy(userTkdys, userId);
		}
		return new ResponseEntity<>(ResponseMessage.success("OK"), HttpStatus.OK);
	}

	/**
	 * 查看所有题目分类及用户订阅
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
		result.put("tkfl", rs);
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		result.put("userTkfldy", this.tkxxService.getUserTkFLdy(userId));
		return new ResponseEntity<>(ResponseMessage.success(result), HttpStatus.OK);
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
	 * 查看所有群组及用户所在群组
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
	@Transactional
	@RequestMapping(value = "/saveUserGroups", method = RequestMethod.POST)
	public ResponseEntity<?> saveUserGroups(@RequestBody List<UserGroup> userGroups) {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		this.userGoupService.insertUserGroup(userGroups, userId);
		return new ResponseEntity<>(ResponseMessage.success("OK"), HttpStatus.OK);
	}

	/**
	 * 设置默认积分榜群组
	 * 
	 * @param userGroups
	 * @return
	 */
	@Transactional
	@RequestMapping(value = "/saveUserDefaultGroup", method = RequestMethod.POST)
	public ResponseEntity<?> saveUserDefaultGroup(@RequestBody UserGroup userGroup) {
		Integer userId = (Integer) this.tkxxService.getCurrentUser().get("ID_");
		this.userGoupService.updateUserDefaultGroup(userGroup, userId);
		return new ResponseEntity<>(ResponseMessage.success("OK"), HttpStatus.OK);
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
	 * 根据userId查看收到的题目纠错
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/userTmJiuChuo", method = RequestMethod.GET)
	public ResponseEntity<?> getUserTmjiuchuo() {
		return new ResponseEntity<>(ResponseMessage.success(this.tkxxService.getUserTmjiuchuo()), HttpStatus.OK);
	}


}
