package gov.hygs.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.gdky.restful.entity.ResponseMessage;
import com.gdky.restful.utils.Md5Utils;

import gov.hygs.dao.TkxxDao;
import gov.hygs.entity.ExamItem;
import gov.hygs.entity.LoudRecord;
import gov.hygs.entity.UserEntity;
import gov.hygs.entity.UserResult;
import gov.hygs.entity.UserTkdy;

/**
 * 题库
 * 
 * @author david
 * 
 */
@Component
public class TkxxService {
	@Resource
	private TkxxDao tkxxDao;

	/**
	 * 查询顶层节点分类
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getTopTmfl() {
		List<Map<String, Object>> fls = tkxxDao.getTopTmfl();
		return fls;
	}

	/**
	 * 根据上层节点查询子级分类节点
	 * 
	 * @param parentId
	 * @return
	 */
	public List<Map<String, Object>> getTmfl(int parentId) {
		List<Map<String, Object>> fls = tkxxDao.getTmfl(parentId);
		return fls;
	}

	/**
	 * 根据分类查询题目信息
	 * 
	 * @param flId
	 * @param userId 
	 * @return
	 */
	public List<Map<String, Object>> getTktmByFlId(int flId, Integer userId) {
		return tkxxDao.getTktmByFlId(flId,userId);
	}

	/**
	 * 通过题目ID查询题目选择项
	 * 
	 * @param tkId
	 * @return
	 */
	public List<Map<String, Object>> getTkxzxByTkId(String tkId) {
		List<Map<String, Object>> ls = tkxxDao.getTkxzxByTkId(tkId);
		if (null == ls || ls.size() == 0) {
			ls = tkxxDao.getTkxzxByTkId("0");
		}
		return ls;
	}

	/**
	 * 根据题目ID查询题目答案
	 * 
	 * @param tkId
	 * @return
	 */
	public List<Map<String, Object>> getTkDaByTkId(String tkId) {
		return tkxxDao.getTkDaByTkId(tkId);
	}

	public ResponseMessage doCheckDtxx(ExamItem item) {
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
		List<Map<String, Object>> das = this.getTkDaByTkId(item.getTkId());
		if(null != das && das.size()>0){
		Map<String,String> daMap = new HashMap<String,String>();
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
			String key = (String)da.get("XZ_KEY");
			daMap.put(key, key);
			if (!item.getResult().equals(da.get("XZ_KEY"))) {
				rs = "你答题目错误,正确答案为" + da.get("XZ_KEY");
				flag = false;
			}
		}
		double score = 0;
		Map<String, Object> tktm = this.tkxxDao.getTktmById(item.getTkId());
		Double tmfz = (Double) tktm.get("TMFZ");
//		Map<String, Double> sjxs = this.tkxxDao.getSjxs();
//		Double xs = 0d;
//		String result = "N";
//		if (true == flag) {
//			result = "Y";
//			if (0 == timeFlag) {
//				xs = sjxs.get("Sjxs_30s");
//
//			} else if (1 == timeFlag) {
//				xs = sjxs.get("Sjxs_2min");
//			} else if (2 == timeFlag) {
//				xs = sjxs.get("Sjxs_8min");
//			}
//			score = tmfz * xs;
//		}
		String result = "N";
		if (true == flag) {
			result = "Y";
			score = tmfz;
		}
	
		UserDetails userDetails = (UserDetails) SecurityContextHolder
				.getContext().getAuthentication().getPrincipal();
		String loginName = userDetails.getUsername();
		Map<String, Object> user = this.tkxxDao.getUserByLoginName(loginName);
		UserResult userRs = new UserResult();
		userRs.setUserId((Integer) user.get("ID_"));
		userRs.setAnswer(item.getResult());
		userRs.setEndTime(item.getEndTime());
		userRs.setStartTime(item.getStartTime());
		userRs.setResultScore(score);
		userRs.setResult(result);
		userRs.setTkId(item.getTkId());
		userRs.setResultTime(sec);
		this.tkxxDao.insertUserResult(userRs);
		
		param.put("rs", rs);
		param.put("userRs", userRs);
		returnValue = ResponseMessage.success(param);
		}else{
			returnValue =ResponseMessage.error("400", "该题目还没有录入答案，请点纠错进行题目纠错");
		}
		return returnValue;
	}

	public ResponseMessage insertLaudRecord(LoudRecord rec) {
		UserDetails userDetails = (UserDetails) SecurityContextHolder
				.getContext().getAuthentication().getPrincipal();
		String loginName = userDetails.getUsername();
		Map<String, String> param = new HashMap<String, String>();
		Map<String, Object> user = this.tkxxDao.getUserByLoginName(loginName);
		rec.setUserId((Integer) user.get("ID_"));
		rec.setDeptId((Integer) user.get("DEPTID"));
		if( rec.getType().equals("1")){
			if(tkxxDao.checkLaudRecord(rec)){
				String rs = "题目已经被点赞过了";
				return ResponseMessage.error("400",rs);
			}else{
				this.tkxxDao.insertLaudRecord(rec);
				String rs = "点赞成功";
				return ResponseMessage.success(rs);
			}
		}else{
				this.tkxxDao.insertLaudRecord(rec);
				String rs = "成功";
				return ResponseMessage.success(rs);
		}
	}
	public void initGdUser()throws IOException{
		String path = "/Users/david/downloads/deptuser.xls";
		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(path));
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);
		Map<String, Integer> depts = this.tkxxDao.getDepts();
		for (int i = 3; i <= sheet.getLastRowNum(); i++) {
			HSSFRow row = sheet.getRow(i);
			HSSFCell xmCell = row.getCell(1);
			String jgmc = xmCell.getStringCellValue();
			xmCell = row.getCell(2);
			String userName = xmCell.getStringCellValue();
			xmCell = row.getCell(4);
			String phone = xmCell.getStringCellValue();
			String loginName =phone;
			Integer parentId = depts.get(jgmc);
			Integer deptId = this.tkxxDao.getXxzxId(parentId);
			Map<String,Object> user = new HashMap<String,Object>();
			user.put("userName", userName);
			user.put("loginName", loginName);
			user.put("phone", phone);
			user.put("deptId",deptId);
			System.out.println(user);
			this.tkxxDao.insertUser(user);
		}
	
	}
	public void initDept()throws IOException{
		String path = "/Users/david/downloads/deptuser.xls";
		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(path));
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);
		Map<String,String> map = new LinkedHashMap<String,String>();
		for (int i = 3; i <= sheet.getLastRowNum(); i++) {
			HSSFRow row = sheet.getRow(i);
			HSSFCell xmCell = row.getCell(1);
			String jgmc = xmCell.getStringCellValue();
			map.put(jgmc, jgmc);
		}
		Collection<String> ls =map.values();
		for(String swjg:ls){
			if("广州".equals(swjg)){
				continue;
			}
			if(swjg.length() == 2){
				Integer parentId =1;
				Map<String,Object> dept =new HashMap<String,Object>();
				dept.put("dept_name",swjg);
				dept.put("parent_id",parentId);
				this.tkxxDao.insertDept(dept);
			}
		}
		Map<String, Integer> depts = this.tkxxDao.getDepts();
		for(String swjg:ls){
			if("广州".equals(swjg)){
				continue;
			}
			if(swjg.length() > 2){
				String parentDept = swjg.substring(0,2);
				Integer parentId =depts.get(parentDept);
				Map<String,Object> dept =new HashMap<String,Object>();
				dept.put("dept_name",swjg);
				dept.put("parent_id",parentId);
				this.tkxxDao.insertDept(dept);
			}
		}
		 depts = this.tkxxDao.getDepts();
		for(String swjg:ls){
			System.out.println(swjg);
			if("广州".equals(swjg)){
				continue;
			}
			if(swjg.length() >= 2){
				 
				Integer parentId =depts.get(swjg);
				Map<String,Object> dept =new HashMap<String,Object>();
				dept.put("dept_name","信息中心");
				dept.put("parent_id",parentId);
				this.tkxxDao.insertDept(dept);
			}
		}
	}
	public void initUser() throws IOException {
		Map<String, Integer> depts = this.tkxxDao.getDepts();
		String path = "/Users/david/downloads/b.xls";
		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(path));
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);
		String sj = "河源市国家税务局";
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			Map<String, Object> user = new HashMap<String, Object>();
			HSSFRow row = sheet.getRow(i);
			String deptName = sj.substring(0, 3);
			HSSFCell xmCell = row.getCell(0);
			String xm = xmCell.getStringCellValue();
			System.out.print(xm);
			user.put("userName", xm);
			HSSFCell phoneCell = row.getCell(1);
			String phone = NumberToTextConverter.toText(phoneCell
					.getNumericCellValue());
			System.out.print(phone);
			user.put("loginName", phone);
			user.put("phone", phone);
			HSSFCell sjCell = row.getCell(4);
			String sjName = null;
			sjName = sjCell.getStringCellValue();

			HSSFCell fjCell = row.getCell(3);
			String fjName = null;
			if (!StringUtils.isEmpty(fjCell.getStringCellValue())) {
				deptName = sjName.substring(0, 3);
				fjName = fjCell.getStringCellValue();
				System.out.print(fjName);
				deptName = fjName;
			} else {
				System.out.print(sjName);
				deptName = sjName;
			}
			user.put("deptId", depts.get(deptName));

			System.out.println(" ");
			this.tkxxDao.insertUser(user);
		}

	}

	/**
	 * 获取当前用户信息
	 * @return
	 */
	public Map<String, Object> getCurrentUser() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder
				.getContext().getAuthentication().getPrincipal();
		String loginName = userDetails.getUsername();
		Map<String, Object> user = this.tkxxDao.getUserByLoginName(loginName);
		return user;
	}

	public void updateUser(UserEntity user) {
		this.tkxxDao.updateUser(user);
	}

	public void updateUserPwd(UserEntity user) {
		user.setPassword(Md5Utils.encodeMd5(user.getPassword()));
		this.tkxxDao.updateUserPwd(user);
	}

	public void insertTkdy(List<UserTkdy> tkdys, Integer userId) {
		this.tkxxDao.clearUserTkDy(userId);
		for (UserTkdy dy : tkdys) {
			dy.setUserId(userId);
			this.tkxxDao.insertTkdy(dy);
		}
	}

	public List<Map<String, Object>> getUserTkfl(Integer userId) {
		List<Map<String, Object>> ls = this.tkxxDao.getUserFl(userId);
		if (ls.size() == 0) {
			ls = this.getTopTmfl();
		}
		return ls;
	}

	public List<Map<String, Object>> getUserTkFLdy(Integer userId) {
		return this.tkxxDao.getUserFl(userId);
	}

	public List<Map<String, Object>> getUserLaud(int userId) {
		return this.tkxxDao.getUserLaud(userId);
	}

	/**
	 * 答题学习记录按天统计
	 * 
	 * @param userId
	 * @return
	 */
	public List<Map<String, Object>> getDtxxRecordList(Integer userId) {
		return this.tkxxDao.getDtxxRecordList(userId);
	}

	/**
	 * 答题学习记录明细
	 * 
	 * @param userId
	 * @param day
	 * @return
	 */
	public List<Map<String, Object>> getDtxxRecordDetail(Integer userId,
			String day) {
		return this.tkxxDao.getDtxxRecordDetail(userId, day);
	}
	/**
	 * 用户答题和点赞次数
	 * @param userId
	 * @return
	 */
	public Map<String,Object> getTotalUserResultByUserId(Integer userId){
		Map<String,Object> result = this.tkxxDao.getTotalUserResultByUserId(userId);
		//int tmCount = this.tkxxDao.getTxtmCount(); 题库题目数
		//BigDecimal totalCount =(BigDecimal)result.get("totalCount");
		//if (null == totalCount) totalCount=new BigDecimal(0);
		//Integer unDoneCount = tmCount-totalCount.intValue();
		//result.put("unDoneCount", unDoneCount);
		//result.put("userLaudRecord", this.tkxxDao.getUserLaudRecord(userId));用户被点赞数
		//result.put("userFix", this.tkxxDao.getUserFix(userId));用户被纠错数
		return result;
	}
	public Map<String,Object> getUserByUserId(Integer userId){
		return this.tkxxDao.getUserByUserId(userId);
	}

	public List<Map<String,Object>> getNewTaskTm(Integer userId){
		return this.tkxxDao.getNewTaskTm(userId);
	}
	
	/**
	 * 根据userId查看收到的题目纠错
	 * @param userId
	 * @return
	 */
	public List<Map<String,Object>> getUserTmjiuchuo(){
		Integer userId = (Integer) this.getCurrentUser().get("ID_");
		return this.tkxxDao.getUserTmjiuchuo(userId);
	}
	/**
	 * 查询用户出题贡献值
	 * @param userId 
	 * @return
	 */
	public Map<String,Object> getUserGxz(Integer userId){
		return this.tkxxDao.getUserGxz(userId);
	}
	/**
	 * 查看答题学习分类排名
	 * @param flId
	 * @return
	 */
	public List<Map<String,Object>> getFlpm(String flId){
		return this.tkxxDao.getFlpm(flId);
	}

	public Object getUserDtxxSroceRank(String flId, Integer userId) {
		// TODO Auto-generated method stub
		return tkxxDao.getUserDtxxSroceRank(flId,userId);
	}
	
}
