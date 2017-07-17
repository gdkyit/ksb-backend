package gov.hygs.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.gdky.restful.dao.BaseJdbcDao;

import gov.hygs.entity.LoudRecord;
import gov.hygs.entity.UserEntity;
import gov.hygs.entity.UserResult;
import gov.hygs.entity.UserTkdy;

/**
 * 题目信息
 * 
 * @author david
 *
 */
@Repository
public class TkxxDao extends BaseJdbcDao {
	/**
	 * 查询顶层节点分类
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getTopTmfl() {
		String sql = "select * from tkfl as fl,(select count(*) as rs,fl_id from tktm  where xybz='Y' and yxbz='Y' group by FL_ID) as tm where fl.PARENT_ID = 0  "
				+ " and fl.id_ = tm.fl_id order by fl.pxh ";
		return this.jdbcTemplate.queryForList(sql);
	}

	/**
	 * 根据上层节点查询子级分类节点
	 * 
	 * @param parentId
	 * @return
	 */
	public List<Map<String, Object>> getTmfl(int parentId) {
		String sql = "select * from tkfl where PARENT_ID = ? order by pxh ";
		return this.jdbcTemplate.queryForList(sql, new Object[] { parentId });
	}

	/**
	 * 根据分类查询题目信息
	 * 
	 * @param flId
	 * @param userId
	 * @return
	 */
	public List<Map<String, Object>> getTktmByFlId(int flId, Integer userId) {
		Integer rowCount = this.getSystemProp().get("fxts");
		StringBuffer sql = new StringBuffer(
				" SELECT tm.ID_, tm.FL_ID, tm.USER_ID, DATE_FORMAT(tm.CREATE_DATE, '%Y-%m-%d %T') AS CREATE_DATE, tm.SP_DATE ");
		sql.append(" 	, tm.SPR_ID, tm.DEPTID, tm.CONTENT, tm.TMFZ, tm.TMND ");
		sql.append(" 	, tm.TMLY_ID, tm.MODE, tm.YXBZ, tm.XYBZ, tm.DRBZ ");
		sql.append(
				" 	, tm.KSBZ, ly.TITLE AS lytitle, case when l.type = 1 then 'Y' else 'N' end SFDZ,ly.CONTENT AS lycontent ");
		sql.append(
				" FROM tmly ly,tktm tm left join laud_record l on l.zstk_id =tm.id_ and l.user_id = ? and l.type ='1' ");
		sql.append(" WHERE tm.TMLY_ID = ly.ID_ ");
		sql.append(" 	AND tm.xybz = 'Y' ");
		sql.append(" 	AND tm.yxbz = 'Y' ");
		sql.append(" 	AND tm.fl_id = ?  ORDER BY  RAND() LIMIT " + rowCount);
		return this.jdbcTemplate.queryForList(sql.toString(), new Object[] { userId, flId });
	}

	/**
	 * 根据id查询题目信息
	 * 
	 * @param id
	 * @return
	 */
	public Map<String, Object> getTktmById(String id) {
		String sql = "select * from tktm where id_ = ? ";
		return this.jdbcTemplate.queryForMap(sql, new Object[] { id });
	}

	public Map<String, Object> getUserByLoginName(String loginName) {
		String sql = "select u.id_,u.LOGIN_NAME,u.USER_NAME,u.PHONE,DATE_FORMAT(u.RZSJ,'%Y-%m-%d %T') RZSJ,u.ZW,u.PWD,u.PHOTO,u.DEPTID,DATE_FORMAT(u.BIRTHDAY,'%Y-%m-%d %T') BIRTHDAY,u.DLXX from user u where u.login_name = ?";
		return this.jdbcTemplate.queryForMap(sql, new Object[] { loginName });
	}

	public Map<String, Object> getUserByUserId(Integer userId) {
		String sql = "select u.id_,u.LOGIN_NAME,u.USER_NAME,u.PHONE,DATE_FORMAT(u.RZSJ,'%Y-%m-%d %T') RZSJ,u.ZW,u.PWD,u.PHOTO,u.DEPTID,DATE_FORMAT(u.BIRTHDAY,'%Y-%m-%d %T') BIRTHDAY,u.DLXX from user u where u.id_ = ?";
		Map<String, Object> user = this.jdbcTemplate.queryForMap(sql, new Object[] { userId });
		Integer deptId = (Integer) user.get("DEPTID");
		Map<String, Object> dept = this.getDeptBydeptId(deptId);
		user.put("dept", dept);
		Integer parentId = (Integer) dept.get("PARENT_ID");
		if(parentId==null){
			user.put("parentDept", null);
		}else{
			Map<String, Object> parentDept = this.getDeptBydeptId(parentId);
			user.put("parentDept", parentDept);
		}
		
		return user;
	}

	public Map<String, Object> getDeptBydeptId(Integer deptId) {
		String sql = "select dept_name,PARENT_ID,ID_ from dept where ID_ = ?";
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sql, new Object[] { deptId });
		if (null != ls && ls.size() == 1) {
			return ls.get(0);
		}
		return null;
	}

	/**
	 * 通过题目ID查询题目选择项
	 * 
	 * @param tkId
	 * @return
	 */
	public List<Map<String, Object>> getTkxzxByTkId(String tkId) {
		String sql = "select * from tkxzx where TK_ID = ? and content is not null order by xz_key";
		return this.jdbcTemplate.queryForList(sql, new Object[] { tkId });
	}

	/**
	 * 根据题目ID查询题目答案
	 * 
	 * @param tkId
	 * @return
	 */
	public List<Map<String, Object>> getTkDaByTkId(String tkId) {
		String sql = "select tkxzx.XZ_KEY from tkda,tkxzx  where tkda.tkxzxid = tkxzx.ID_ and tkda.TK_ID = ? order by tkxzx.XZ_KEY ";
		return this.jdbcTemplate.queryForList(sql, new Object[] { tkId });
	}

	public Map<String, Integer> getSystemProp() {
		String sql = "select * from system_props where key_ ='fxts' or key_ ='jfpms' ";
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sql);
		Map<String, Integer> rs = new HashMap<String, Integer>();
		for (Map<String, Object> obj : ls) {
			String key = (String) obj.get("KEY_");
			String value = (String) obj.get("VALUE");
			rs.put(key, Integer.parseInt(value));
		}
		return rs;
	}

	public Map<String, Double> getSjxs() {
		String sql = "select * from system_props where key_ like 'Sjxs%'";
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sql);
		Map<String, Double> rs = new HashMap<String, Double>();
		for (Map<String, Object> obj : ls) {
			String key = (String) obj.get("KEY_");
			String value = (String) obj.get("VALUE");
			rs.put(key, Double.parseDouble(value));
		}
		return rs;
	}

	public void insertUserResult(UserResult userRs) {
		String sql = "insert into user_result (user_id,tm_id,answer,result,result_time,result_score,start_time,end_time) values (?,?,?,?,?,?,?,?)";
		this.jdbcTemplate.update(sql,
				new Object[] { userRs.getUserId(), userRs.getTkId(), userRs.getAnswer(), userRs.getResult(),
						userRs.getResultTime(), userRs.getResultScore(), userRs.getStartTime(), userRs.getEndTime() });
	}

	public void insertLaudRecord(LoudRecord rec) {
		String sql = "insert into laud_record(user_id,dept_id,zstk_id,dz_date,type,remark) values(?,?,?,?,?,?)";
		this.jdbcTemplate.update(sql, new Object[] { rec.getUserId(), rec.getDeptId(), rec.getZstkId(), rec.getDzDate(),
				rec.getType(), rec.getRemark() });
	}

	public boolean checkLaudRecord(LoudRecord rec) {
		// TODO Auto-generated method stub
		String sql = "select 1 from laud_record where user_id = ? and zstk_id = ? and type = ? ";
		List ls = this.jdbcTemplate.queryForList(sql,new Object[] { rec.getUserId(), rec.getZstkId(), rec.getType()});
		if(ls.size()>0){
			return true;
		}else{
			return false;
		}
	}

	public void insertUser(Map<String, Object> user) {
		String sql = "insert into user (LOGIN_NAME,USER_NAME,PHONE,PWD,DEPTID) values(?,?,?,'90fe61c21ccccafc5c0797b7ec80ea7c',?) ";
		this.jdbcTemplate.update(sql,
				new Object[] { user.get("loginName"), user.get("userName"), user.get("phone"), user.get("deptId") });
	}

	public Map<String, Integer> getDepts() {
		String sql = "select id_,dept_name from dept";
		Map<String, Integer> depts = new HashMap<String, Integer>();
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sql);
		for (Map<String, Object> rec : ls) {
			Integer id = (Integer) rec.get("id_");
			String name = (String) rec.get("dept_name");
			depts.put(name, id);
		}
		return depts;
	}

	public void updateUser(UserEntity user) {
		String sql = " update user set user_name =?,phone=?,rzsj=?,birthday=? where  id_ = ?";
		this.jdbcTemplate.update(sql, new Object[] { user.getName(), user.getPhone(), user.getRzday(),
				user.getBirthday(), user.getUserId() });// Md5Utils.encodeMd5(user.getPassword())
	}

	public void updateUserPwd(UserEntity user) {
		String sql = " update user set pwd=? where  id_ = ?";
		this.jdbcTemplate.update(sql, new Object[] { user.getPassword(), user.getUserId() });//
	}

	public void insertTkdy(UserTkdy tkdy) {

		String sql = "insert into tkfldy(user_id,tkfl_id) values(?,?)";
		this.jdbcTemplate.update(sql, new Object[] { tkdy.getUserId(), tkdy.getFlId() });

	}

	public void clearUserTkDy(Integer userId) {
		String sql = "delete from tkfldy where user_id =?  ";
		this.jdbcTemplate.update(sql, new Object[] { userId });
	}

	public List<Map<String, Object>> getUserFl(Integer userId) {
		String sql = "select fl.*,tm.rs from tkfl as fl,tkfldy as dy ,(select count(*) as rs,fl_id from tktm  where xybz='Y' and yxbz='Y' group by FL_ID) as tm where dy.tkfl_id = fl.id_ and dy.user_id = ? and fl.id_ = tm.fl_id order by fl.pxh ";
		return this.jdbcTemplate.queryForList(sql, new Object[] { userId });

	}

	public List<Map<String, Object>> getUserLaud(int userId) {
		String sql = "select tm.content ,DATE_FORMAT(laud.dz_date,'%Y-%m-%d') dz_date,u.user_name  from laud_record as laud,tktm as tm ,user as u  "
				+ "  where  laud.type ='1' and tm.id_ = laud.zstk_id and tm.user_id =?   and u.id_ = laud.user_id ";
		return this.jdbcTemplate.queryForList(sql, new Object[] { userId });
	}
	
	/**
	 * 根据userId查看收到的题目纠错
	 * 
	 * @param userId
	 * @return
	 */
	public List<Map<String, Object>> getUserTmjiuchuo(Integer userId) {
		String sql = "select tm.user_id ctr,tm.CONTENT,DATE_FORMAT(lr.dz_date,'%Y-%m-%d') dz_date,lr.remark from laud_record lr,tktm tm  where lr.type =2 and tm.id_ = lr.ZSTK_ID  and tm.USER_ID =?";
		return this.jdbcTemplate.queryForList(sql, new Object[] { userId });
	}

	
	/**
	 * 答题学习记录按天统计
	 * 
	 * @param userId
	 * @return
	 */
	public List<Map<String, Object>> getDtxxRecordList(Integer userId) {
		StringBuffer sb = new StringBuffer(100);
		sb.append(
				"  select start_time,sum(score) as score,sum(right1) as rightCount,sum(error1) as errorCount,sum(right1+error1) totalCount  ");
		sb.append("  from(  ");
		sb.append(
				"  select result_score as score,case when result ='Y'  then 1 when result='N' then 0 end as right1, case when result ='Y'  then 0 when result='N' then 1 end as error1,DATE_FORMAT(start_time,'%Y-%m-%d') as start_time  ");
		sb.append("  from user_result where user_id =?   ");
		sb.append("  ) as b group by START_TIME  order by start_time desc ");
		return this.jdbcTemplate.queryForList(sb.toString(), new Object[] { userId });
	}

	/**
	 * 答题学习记录明细
	 * 
	 * @param userId
	 * @param day
	 * @return
	 */
	public List<Map<String, Object>> getDtxxRecordDetail(Integer userId, String day) {
		StringBuffer sb = new StringBuffer(100);
		sb.append(
				"  select tm.content,er.answer,er.result,er.result_SCORE as score from user_result as er,tktm as tm where  er.tm_id = tm.id_ ");
		sb.append("  and  er.user_id =? and DATE_FORMAT(start_time,'%Y-%m-%d')=?   ");
		return this.jdbcTemplate.queryForList(sb.toString(), userId, day);
	}

	/**
	 * 用户答题明细
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getTotalUserResultByUserId(Integer userId) {
		StringBuffer sb = new StringBuffer(600);
		sb.append(
				" select sum(rightCount) as totalRightCount,sum(errorCount) as totalErrorCount,sum(rightCount)+sum(errorCount) as totalCount from (  ");
		sb.append(" 		select case when  eur.RESULT='Y' then 1  when eur.result ='N' then 0 end as rightCount,  ");
		sb.append(" 		 case when eur.RESULT ='N' then 1 when eur.result='Y' then 0 end as errorCount  ");
		sb.append(
				" 		 from exam_user_result as eur,exam_detail as ed,exam as e where eur.EXAM_DETAIL_ID = ed.id_ and ed.EXAM_ID = e.ID_ and e.EXAM_TYPE ='2'  ");
		sb.append(" 		  and eur.USER_ID =?  ");

		sb.append(" 		union all  ");

		sb.append(" 		select case when  ur.RESULT='Y' then 1  when ur.result ='N' then 0 end as rightCount,  ");
		sb.append(" 		 case when ur.RESULT ='N' then 1 when ur.result='Y' then 0 end as errorCount  ");
		sb.append(" 		 from user_result as ur where  ur.user_id = ?  ");
		sb.append(" 		) as cc    ");
		return this.jdbcTemplate.queryForMap(sb.toString(), new Object[] { userId, userId });
	}

	/**
	 * 题库总题数
	 * 
	 * @return
	 */
	public int getTxtmCount() {
		String sql = "select count(*) as rs from  tktm where yxbz='Y' and xybz ='Y' ";
		return this.jdbcTemplate.queryForObject(sql, Integer.class);
	}

	/**
	 * 用户被点赞数
	 * 
	 * @param userId
	 * @return
	 */
	public int getUserLaudRecord(Integer userId) {
		String sql = "select count(*) as rs  from laud_record as la,tktm as tm where la.ZSTK_ID = tm.ID_ and tm.USER_ID = ? and la.type='1'";
		return this.jdbcTemplate.queryForObject(sql, new Object[] { userId }, Integer.class);
	}

	/**
	 * 用户被纠错数
	 * 
	 * @param userId
	 * @return
	 */
	public int getUserFix(Integer userId) {
		String sql = "select count(*) as rs  from laud_record as la,tktm as tm where la.ZSTK_ID = tm.ID_ and tm.USER_ID = ? and la.type='2'";
		return this.jdbcTemplate.queryForObject(sql, new Object[] { userId }, Integer.class);
	}

	public List<Map<String, Object>> getNewTaskTm(Integer userId) {
		Integer rowCount = this.getSystemProp().get("fxts");
		String sql = "select tm.*,ly.TITLE as lytitle,ly.CONTENT as lycontent from tktm as tm ,tmly as ly where tm.TMLY_ID = ly.ID_ and tm.tmnd <>1 and tm.fl_id in (select tkfl_id from tkfldy where user_id = ? ) ORDER BY  RAND() LIMIT "
				+ rowCount;
		return this.jdbcTemplate.queryForList(sql, new Object[] { userId });
	}

	public void insertDept(Map<String, Object> dept) {
		String sql = "insert into dept(dept_name,parent_id) values(?,?)";
		this.jdbcTemplate.update(sql, new Object[] { dept.get("dept_name"), dept.get("parent_id") });
	}

	public Integer getXxzxId(Integer parentId) {
		String sql = "select id_ from dept where parent_id =? and dept_name='信息中心'";
		return this.jdbcTemplate.queryForObject(sql, new Object[] { parentId }, Integer.class);
	}


	public Map<String, Object> getUserGxz(Integer userId) {
		String sql = "select count(*) count,sum(gxz) gxz from tk_gxjl as gxjl,tktm tm where tm.id_ = gxjl.tk_id and gxjl.user_id =? and gxly = 2";
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sql, new Object[] { userId });
		if (null != ls && ls.size() == 1) {
			return ls.get(0);
		} else {
			return null;
		}

	}

	/**
	 * 查看答题学习分类排名
	 * 
	 * @param flId
	 * @return
	 */
	public List<Map<String, Object>> getFlpm(String flId) {
		Integer jfpms = this.getSystemProp().get("jfpms");
		StringBuffer sb = new StringBuffer(600);
		sb.append(" select u.user_name,u.login_name,u.photo,flpm.* from (  ");
		sb.append(" 		select convert(@rank:=@rank+1,SIGNED) AS rank,aa.user_id,FORMAT(aa.score,2) as score from (   ");
		sb.append(" 		select sum(result_score) as score,fl_id ,ur.user_id from user_result as ur,tktm as tm where tm.id_ = ur.tm_id    ");
		sb.append(" 		and tm.fl_id = ?   ");
		sb.append(" 		group by tm.fl_id,ur.user_id ) as aa,(SELECT @rank:=0) C order by aa.score desc   ");
		sb.append(" 		) as flpm,user u where u.id_ = flpm.user_id order by rank  limit " + jfpms + " ");
		return this.jdbcTemplate.queryForList(sb.toString(), new Object[] { flId });
	}

	public Object getUserDtxxSroceRank(String flId, Integer userId) {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer(600);
		sb.append(" select u.user_name,u.login_name,flpm.* from (  ");
		sb.append(" 		select convert(@rank:=@rank+1,SIGNED) AS rank,aa.user_id,FORMAT(aa.score,2) as score from (   ");
		sb.append(" 		select sum(result_score) as score,fl_id ,ur.user_id from user_result as ur,tktm as tm where tm.id_ = ur.tm_id    ");
		sb.append(" 		and tm.fl_id = ?   ");
		sb.append(" 		group by tm.fl_id,ur.user_id ) as aa,(SELECT @rank:=0) C order by aa.score desc   ");
		sb.append(" 		) as flpm,user u where u.id_ = flpm.user_id and flpm.user_id = ? order by rank   ");
		return this.jdbcTemplate.queryForList(sb.toString(), new Object[] { flId, userId });
	}
}
