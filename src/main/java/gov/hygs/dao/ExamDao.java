package gov.hygs.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.gdky.restful.dao.BaseJdbcDao;

import gov.hygs.entity.ExamItem;
import gov.hygs.entity.UserResult;

@Repository
public class ExamDao extends BaseJdbcDao {
	/**
	 * 查询考试
	 * 
	 * @param userId
	 * @return
	 */
	public List<Map<String, Object>> getExam(int userId, String type) {
		StringBuffer sb = new StringBuffer(100);
		sb.append("  select exam.id_, ");
		sb.append(" DATE_FORMAT(exam.START_TIME,'%Y-%m-%d')  day,");
		sb.append(" DATE_FORMAT(exam.START_TIME,'%Y-%m-%d %T')  START_TIME,");
		sb.append(" DATE_FORMAT(exam.END_TIME,'%Y-%m-%d %T')  END_TIME,");
		sb.append(" exam.TITLE,");
		sb.append(" exam.EXAM_TYPE,");
		sb.append(" exam.FQR_ID,");
		sb.append(" exam.remark,");
		sb.append(" exam.exam_time");
		sb.append(" from exam as exam,exam_tsqz as tsqz,user_group as ug  ");
		sb.append("  where exam.id_ = tsqz.exam_id ");
		sb.append("    and tsqz.group_id = ug.group_id ");
		sb.append("   and ug.user_id =? ");
		sb.append("   and current_timestamp() between exam.start_time and exam.end_time+1 ");
		sb.append("   and exam.EXAM_TYPE =? ");
		sb.append("   and exam.id_ not in (select  aa.exam_id from  ");
		sb.append("  (select count(*) as rs,exam_id from exam_detail group by exam_id) as aa, ");
		sb.append("  (select count(*) as rs1,id_ as exam_id from ( ");
		sb.append("  select e.id_ from exam as e,exam_detail d,exam_user_result u where e.id_ = d.exam_id and d.id_ = u.exam_detail_id and u.user_id =? ");
		sb.append("  ) as bb group by exam_id) as cc  where aa.exam_id = cc.exam_id and aa.rs = cc.rs1) order by exam.start_time desc  ");
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(
				sb.toString(), new Object[] { userId, type, userId });

		return ls;
	}

	/**
	 * 查询考试题目
	 * 
	 * @param examId
	 * @return
	 */
	public List<Map<String, Object>> getExamDetailByExamId(Integer examId,Integer userId) {
		String sql = "select ly.TITLE as lytitle,ly.CONTENT as lycontent ,detail.id_ as detailId, detail.xh,detail.EXAM_ID,tm.ID_,tm.FL_ID,tm.USER_ID,DATE_FORMAT(tm.CREATE_DATE,'%Y-%m-%d %T') CREATE_DATE,tm.SP_DATE,tm.SPR_ID,tm.DEPTID,tm.CONTENT,tm.TMND,tm.TMLY_ID,tm.MODE,tm.YXBZ,tm.XYBZ,tm.DRBZ,tm.KSBZ"
				+ " from exam_detail as detail ,tktm as tm ,tmly as ly where tm.TMLY_ID = ly.ID_ and detail.exam_id = ? and detail.TM_ID = tm.ID_"
				+ " and detail.id_ not in (select exam_detail_id from exam_user_result where user_id = ?)  order by rand() ";
		return this.jdbcTemplate.queryForList(sql, new Object[] { examId,userId });
	}
	/**
	 * 用户已答题情况
	 * @param examId
	 * @param userId
	 * @return
	 */
	public List<UserResult> getUserRs(Integer examId,Integer userId){
		StringBuffer sb = new StringBuffer();
		sb.append(" select eur.*,ed.EXAM_ID,ed.tm_id from exam_user_result eur,exam_detail ed ");
		sb.append(" where ed.id_ = eur.EXAM_DETAIL_ID and eur.USER_ID=?");
		sb.append(" and ed.EXAM_ID =?");
		return this.jdbcTemplate.query(sb.toString(),new Object[]{userId,examId}, new RowMapper<UserResult>(){

			public UserResult mapRow(ResultSet rs, int arg1)
					throws SQLException {
				UserResult userRs = new UserResult();
				userRs.setUserId(rs.getInt("USER_ID"));
				userRs.setAnswer(rs.getString("ANSWER"));
				userRs.setEndTime(rs.getTimestamp("END_TIME"));
				userRs.setStartTime(rs.getTimestamp("START_TIME"));
				userRs.setResultScore(rs.getDouble("EXAM_SCORE"));
				userRs.setResult(rs.getString("RESULT"));
				userRs.setTkId(rs.getString("tm_id"));
				userRs.setResultTime(rs.getLong("EXAM_TIME"));
				userRs.setExamDetailId(rs.getInt("EXAM_DETAIL_ID"));
				userRs.setType(rs.getString("type"));
				return userRs;
			}
			
		});
	}
	public void insertUserResult(UserResult userRs) {
		if(checkExamUserResult(userRs.getUserId(), userRs.getExamDetailId())){
			String sql = "insert into exam_user_result (user_id,EXAM_DETAIL_ID,answer,result,exam_time,exam_score,start_time,end_time) values (?,?,?,?,?,?,?,?)";
			this.jdbcTemplate.update(
					sql,
					new Object[] { userRs.getUserId(), userRs.getExamDetailId(),
							userRs.getAnswer(), userRs.getResult(),
							userRs.getResultTime(), userRs.getResultScore(),
							userRs.getStartTime(), userRs.getEndTime() });
		}
	}
	public boolean checkExamUserResult(Object userId,Object examDetailId){
		String sql ="select count(*) as rs from exam_user_result where user_id =? and exam_detail_id =?";
		int rs = this.jdbcTemplate.queryForObject(sql, new Object[]{userId,examDetailId},Integer.class);
		if(0 == rs){
			return true;
		}
		
		return false;
	}

	/**
	 * 积分排行榜
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getScoreRank() {
		Integer jfpms = this.getSystemProp().get("jfpms");
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result as eur,exam_detail as ed,exam as e where eur.EXAM_DETAIL_ID = ed.id_ and ed.EXAM_ID = e.ID_ and e.EXAM_TYPE ='2'   group by eur.user_id  ");
		sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result   group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc limit "+jfpms+") as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString());
	}
	/**
	 * 根据题库分类统计学习分数
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getScoreGroupByFlId(Integer userId) {
		StringBuffer sb = new StringBuffer();
		sb.append(" select a.*,fl.tkmc from (  ");
		sb.append("  		select sum(ur.result_score) as score,tm.fl_id from user_result as ur ,tktm as tm where ur.user_id = ? and tm.ID_ = ur.TM_ID group by tm.fl_id  "); 
		sb.append("   		) as a,tkfl as fl   ");
		sb.append("   		where a.fl_id = fl.id_   ");
		sb.append("   		order by a.score desc ");
		return this.jdbcTemplate.queryForList(sb.toString(),new Object[]{userId});
	}
	public Map<String,Integer> getSystemProp(){
		String sql ="select * from system_props where key_ ='jfpms'  ";
		List<Map<String,Object>> ls =this.jdbcTemplate.queryForList(sql);
		Map<String,Integer> rs = new HashMap<String,Integer>();
		for(Map<String,Object> obj:ls){
			String key = (String)obj.get("KEY_");
			String value = (String)obj.get("VALUE");
			rs.put(key, Integer.parseInt(value));
		}
		return rs;
	}
	
	public List<Map<String, Object>> getScoreRank(Integer groupId) {
		Integer jfpms = this.getSystemProp().get("jfpms");
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name,u.photo from (  ");
		//sb.append("  		select sum(score) as score ,user_id from (  ");
		//sb.append("  		select sum(exam_score) as score,user_id from exam_user_result as eur,exam_detail as ed,exam as e where eur.EXAM_DETAIL_ID = ed.id_ and ed.EXAM_ID = e.ID_ and e.EXAM_TYPE ='2'   group by eur.user_id  ");
		//sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result   group by user_id  ");
	//	sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  and a.user_id in(select user_id from user_group where group_id =?) ");
		sb.append("  		order by a.score desc  limit "+jfpms+") as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString(),
				new Object[] { groupId });
	}
	
	public Map<String, Object> getUserGroupScoreRank(Integer groupId,Integer userId) {
	 
		StringBuffer sb = new StringBuffer();
		sb.append(" select * from (");
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
	//	sb.append("  		select sum(score) as score ,user_id from (  ");
	//	sb.append("  		select sum(exam_score) as score,user_id from exam_user_result as eur,exam_detail as ed,exam as e where eur.EXAM_DETAIL_ID = ed.id_ and ed.EXAM_ID = e.ID_ and e.EXAM_TYPE ='2'   group by eur.user_id  ");
	//	sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result   group by user_id  ");
//		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  and a.user_id in(select user_id from user_group where group_id =?) ");
		sb.append("  		order by a.score desc ) as ccd,(SELECT @rank:=0) C  ");
		sb.append(" ) as userGroupRank where  userGroupRank.user_Id= ?    ");
		List<Map<String,Object>> ls = this.jdbcTemplate.queryForList(sb.toString(),
				new Object[] { groupId ,userId});
		if(ls != null && ls.size() == 1){
			return ls.get(0);
		}else{
			return null;
		}
	}

	/**
	 * 个人在积分排行榜情况
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserScoreRank(Integer userId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result as eur,exam_detail as ed,exam as e where eur.EXAM_DETAIL_ID = ed.id_ and ed.EXAM_ID = e.ID_ and e.EXAM_TYPE ='2'   group by eur.user_id  ");
		sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sb
				.toString());
		return this.getUserRank(ls, userId);

	}

	private Map<String, Object> getUserRank(List<Map<String, Object>> ls,
			Integer userId) {
		Object userRank = null;
		Double score = null;
		for (Map<String, Object> rank : ls) {
			Integer uid = (Integer) rank.get("USER_ID");
			if (uid.equals(userId)) {
				userRank = rank.get("rank");
				score = (Double) rank.get("score");
				break;
			}
		}
		if (null != userRank) {
			Integer totalCount = ls.size() - ((Long) userRank).intValue();
			Map<String, Object> rs = new HashMap<String, Object>();
			rs.put("userRank", userRank);
			rs.put("userId", userId);
			rs.put("score", score);
			rs.put("totalCount", totalCount);
			return rs;
		} else {
			Map<String, Object> rs = new HashMap<String, Object>();
			rs.put("userRank", 0);
			rs.put("userId", userId);
			rs.put("score", 0);
			rs.put("totalCount", 0);
			return rs;
		}

	}

	/**
	 * 积分排行榜本周
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getScoreRankByWeek() {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate()) and week(end_time) = week(curdate())   group by user_id  ");
		sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate()) and week(end_time) = week(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString());
	}

	public List<Map<String, Object>> getScoreRankByWeek(Integer groupId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate()) and week(end_time) = week(curdate())   group by user_id  ");
		sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate()) and week(end_time) = week(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  and a.user_id in(select user_id from user_group where group_id =?) ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString(),
				new Object[] { groupId });
	}

	/**
	 * 个人在积分排行榜情况本周
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserScoreRankByWeek(Integer userId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate()) and week(end_time) = week(curdate())  group by user_id  ");
		sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate()) and week(end_time) = week(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sb
				.toString());
		return this.getUserRank(ls, userId);

	}

	/**
	 * 积分排行榜本月
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getScoreRankByMonth() {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate())  group by user_id  ");
		sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString());
	}

	public List<Map<String, Object>> getScoreRankByMonth(Integer groupId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate())  group by user_id  ");
		sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_ and a.user_id in(select user_id from user_group where group_id =?)  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString(),
				new Object[] { groupId });
	}

	/**
	 * 个人在积分排行榜情况本月
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserScoreRankByMonth(Integer userId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate())  group by user_id  ");
		sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sb
				.toString());
		return this.getUserRank(ls, userId);

	}

	/**
	 * 积分排行榜本季度
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getScoreRankByQuarter() {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate()) and quarter(end_time) = quarter(curdate())  group by user_id  ");
		sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate()) and quarter(end_time) = quarter(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString());
	}

	public List<Map<String, Object>> getScoreRankByQuarter(Integer groupId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate()) and quarter(end_time) = quarter(curdate())  group by user_id  ");
		sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate()) and quarter(end_time) = quarter(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_ and a.user_id in(select user_id from user_group where group_id =?)  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString(),
				new Object[] { groupId });
	}

	/**
	 * 个人在积分排行榜情况本季度
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserScoreRankByQuarter(Integer userId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate()) and quarter(end_time) = quarter(curdate())   group by user_id  ");
		sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate()) and quarter(end_time) = quarter(curdate())   group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sb
				.toString());
		return this.getUserRank(ls, userId);

	}

	/**
	 * 积分排行榜本年
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getScoreRankByYear() {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate())   group by user_id  ");
		sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate())   group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString());
	}

	public List<Map<String, Object>> getScoreRankByYear(Integer groupId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate())   group by user_id  ");
		sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate())   group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_ and a.user_id in(select user_id from user_group where group_id =?)  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString(),
				new Object[] { groupId });
	}

	/**
	 * 个人在积分排行榜情况本年
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserScoreRankByYear(Integer userId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate())  group by user_id  ");
		sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sb
				.toString());
		return this.getUserRank(ls, userId);
	}

	/**
	 * 答题排行榜本周
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getDtScoreRank() {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		// sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where   year(end_time) = year(curdate()) and month(END_TIME) = month(curdate()) and week(end_time) = week(curdate())   group by user_id  ");
		// sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result   group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString());
	}

	public List<Map<String, Object>> getDtScoreRank(Integer groupId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
	//	sb.append("  		select sum(score) as score ,user_id from (  ");
		// sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where   year(end_time) = year(curdate()) and month(END_TIME) = month(curdate()) and week(end_time) = week(curdate())   group by user_id  ");
		// sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result   group by user_id  ");
	//	sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_ and a.user_id in(select user_id from user_group where group_id =?) ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString(),
				new Object[] { groupId });
	}

	/**
	 * 个人在答题排行榜情况
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserDtScoreRank(Integer userId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
	//	sb.append("  		select sum(score) as score ,user_id from (  ");
		// sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate()) and week(end_time) = week(curdate())  group by user_id  ");
		// sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result  group by user_id  ");
	//	sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sb
				.toString());
		return this.getUserRank(ls, userId);

	}

	/**
	 * 答题排行榜本周
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getDtScoreRankByWeek() {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		// sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where   year(end_time) = year(curdate()) and month(END_TIME) = month(curdate()) and week(end_time) = week(curdate())   group by user_id  ");
		// sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate()) and week(end_time) = week(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString());
	}

	public List<Map<String, Object>> getDtScoreRankByWeek(Integer groupId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		// sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where   year(end_time) = year(curdate()) and month(END_TIME) = month(curdate()) and week(end_time) = week(curdate())   group by user_id  ");
		// sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate()) and week(end_time) = week(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_ and a.user_id in(select user_id from user_group where group_id =?) ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString(),
				new Object[] { groupId });
	}

	/**
	 * 个人在答题排行榜情况本周
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserDtScoreRankByWeek(Integer userId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		// sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate()) and week(end_time) = week(curdate())  group by user_id  ");
		// sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate()) and week(end_time) = week(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sb
				.toString());
		return this.getUserRank(ls, userId);
	}

	/**
	 * 答题排行榜本月
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getDtScoreRankByMonth() {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		// sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate())  group by user_id  ");
		// sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString());
	}

	public List<Map<String, Object>> getDtScoreRankByMonth(Integer groupId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		// sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate())  group by user_id  ");
		// sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  and a.user_id in(select user_id from user_group where group_id =?) ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString(),
				new Object[] { groupId });
	}

	/**
	 * 个人在答题排行榜情况本月
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserDtScoreRankByMonth(Integer userId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		// sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate())  group by user_id  ");
		// sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate()) and month(END_TIME) = month(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sb
				.toString());
		return this.getUserRank(ls, userId);

	}

	/**
	 * 答题排行榜本季度
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getDtScoreRankByQuarter() {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		// sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate()) and quarter(end_time) = quarter(curdate())  group by user_id  ");
		// sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate()) and quarter(end_time) = quarter(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString());
	}

	public List<Map<String, Object>> getDtScoreRankByQuarter(Integer groupId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		// sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate()) and quarter(end_time) = quarter(curdate())  group by user_id  ");
		// sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate()) and quarter(end_time) = quarter(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  and a.user_id in(select user_id from user_group where group_id =?) ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString(),
				new Object[] { groupId });
	}

	/**
	 * 个人在答题排行榜情况本季度
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserDtScoreRankByQuarter(Integer userId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		// sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate()) and quarter(end_time) = quarter(curdate())   group by user_id  ");
		// sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate()) and quarter(end_time) = quarter(curdate())   group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sb
				.toString());
		return this.getUserRank(ls, userId);
	}

	/**
	 * 答题排行榜本年
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getDtScoreRankByYear() {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		// sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate())   group by user_id  ");
		// sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate())   group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString());
	}

	public List<Map<String, Object>> getDtScoreRankByYear(Integer groupId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		// sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate())   group by user_id  ");
		// sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate())   group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_ and a.user_id in(select user_id from user_group where group_id =?)   ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString(),
				new Object[] { groupId });
	}

	/**
	 * 个人在答题排行榜情况本年
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserDtScoreRankByYear(Integer userId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		// sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where year(end_time) = year(curdate())  group by user_id  ");
		// sb.append("  		union all  ");
		sb.append("  		select sum(result_score) as score,user_id from user_result where year(end_time) = year(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sb
				.toString());
		return this.getUserRank(ls, userId);

	}

	/**
	 * 考试排行榜
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getExamScoreRank(String examId) {
		Integer jfpms = this.getSystemProp().get("jfpms");
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (    ");
		sb.append(" 	  		select a.* ,u.login_name,u.user_name,u.photo from (      ");
		sb.append(" 	   		select sum(score) as score,sum(time) time ,user_id from (    ");  
		sb.append(" 	  		select sum(eur.exam_score) as score,sum(eur.exam_time) time,eur.user_id from exam_user_result as eur,exam_detail as ed,exam as e where e.id_=ed.exam_id and eur.exam_detail_id = ed.id_ and ed.exam_id =?   group by user_id      ");
		sb.append(" 	  		) as bb group by bb.user_id      ");
		sb.append(" 	   		) as a,user as u      ");
		sb.append(" 	   		where a.user_id = u.id_     "); 
		sb.append(" 	   		order by a.score desc ,a.time limit "+jfpms+" ) as ccd,(SELECT @rank:=0) C     ");
		return this.jdbcTemplate.queryForList(sb.toString(),new Object[]{examId});
	}
	/**
	 * 考试排行榜
	 * 
	 * @return
	 */
	public Map<String, Object> getUserExamScoreRank(String examId,Integer userId) {
		StringBuffer sb = new StringBuffer();
		sb.append(" select * from ( ");
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  "); 
		sb.append("  		select sum(score) as score,sum(time) time ,user_id from (  ");
		sb.append("  		select sum(eur.exam_score) as score,sum(eur.exam_time) time,eur.user_id from exam_user_result as eur,exam_detail as ed,exam as e where e.id_=ed.exam_id and eur.exam_detail_id = ed.id_ and ed.exam_id =?   group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc ) as ccd,(SELECT @rank:=0) C  ");
		sb.append(" ) as userExamRank where userExamRank.user_id = ?   ");
		List<Map<String,Object>> ls = this.jdbcTemplate.queryForList(sb.toString(),new Object[]{examId,userId});
		if(ls != null && ls.size() == 1){
			return ls.get(0);
		}else{
			return null;
		}
	}

	public List<Map<String, Object>> getQdScoreRank(Integer groupId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where type='2'    group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_ and a.user_id in(select user_id from user_group where group_id =?)   ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString(),
				new Object[] { groupId });
	}

	/**
	 * 个人在抢答排行榜情况
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserQdScoreRank(Integer userId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where type='2'  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sb
				.toString());
		return this.getUserRank(ls, userId);

	}

	/**
	 * 抢答排行榜本周
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getQdScoreRankByWeek() {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where type='2'  and year(end_time) = year(curdate()) and month(END_TIME) = month(curdate()) and week(end_time) = week(curdate())   group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString());
	}

	public List<Map<String, Object>> getQdScoreRankByWeek(Integer groupId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where type='2'  and year(end_time) = year(curdate()) and month(END_TIME) = month(curdate()) and week(end_time) = week(curdate())   group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  and a.user_id in(select user_id from user_group where group_id =?) ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString(),
				new Object[] { groupId });
	}

	/**
	 * 个人在抢答排行榜情况
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserQdScoreRankByWeek(Integer userId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where type='2'  and year(end_time) = year(curdate()) and month(END_TIME) = month(curdate()) and week(end_time) = week(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sb
				.toString());
		return this.getUserRank(ls, userId);

	}

	/**
	 * 抢答排行榜本月
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getQdScoreRankByMonth() {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where type ='2'  and year(end_time) = year(curdate()) and month(END_TIME) = month(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString());
	}

	public List<Map<String, Object>> getQdScoreRankByMonth(Integer groupId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where type ='2'  and year(end_time) = year(curdate()) and month(END_TIME) = month(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_ and a.user_id in(select user_id from user_group where group_id =?)   ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString(),
				new Object[] { groupId });
	}

	/**
	 * 个人在抢答排行榜情况本月
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserQdScoreRankByMonth(Integer userId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where type='2' and year(end_time) = year(curdate()) and month(END_TIME) = month(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sb
				.toString());
		return this.getUserRank(ls, userId);
	}

	/**
	 * 抢答排行榜本季度
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getQdScoreRankByQuarter() {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where type='2' and year(end_time) = year(curdate()) and quarter(end_time) = quarter(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString());
	}

	public List<Map<String, Object>> getQdScoreRankByQuarter(Integer groupId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where type='2' and year(end_time) = year(curdate()) and quarter(end_time) = quarter(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  and a.user_id in(select user_id from user_group where group_id =?)  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString(),
				new Object[] { groupId });
	}

	/**
	 * 个人在抢答排行榜情况本季度
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserQdScoreRankByQuarter(Integer userId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where type='2' and year(end_time) = year(curdate()) and quarter(end_time) = quarter(curdate())   group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sb
				.toString());
		return this.getUserRank(ls, userId);

	}

	/**
	 * 抢答排行榜本年
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getQdScoreRankByYear() {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where type='2' and year(end_time) = year(curdate())   group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString());
	}

	public List<Map<String, Object>> getQdScoreRankByYear(Integer groupId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where type='2' and year(end_time) = year(curdate())   group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_ and a.user_id in(select user_id from user_group where group_id =?)  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		return this.jdbcTemplate.queryForList(sb.toString(),
				new Object[] { groupId });
	}

	/**
	 * 个人在抢答排行榜情况本年
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> getUserQdScoreRankByYear(Integer userId) {
		StringBuffer sb = new StringBuffer();
		sb.append("  select convert(@rank:=@rank+1,SIGNED) AS rank, ccd.* from (   ");
		sb.append("  		select a.* ,u.login_name,u.user_name from (  ");
		sb.append("  		select sum(score) as score ,user_id from (  ");
		sb.append("  		select sum(exam_score) as score,user_id from exam_user_result where type='2' and year(end_time) = year(curdate())  group by user_id  ");
		sb.append("  		) as bb group by bb.user_id  ");
		sb.append("  		) as a,user as u  ");
		sb.append("  		where a.user_id = u.id_  ");
		sb.append("  		order by a.score desc) as ccd,(SELECT @rank:=0) C  ");
		List<Map<String, Object>> ls = this.jdbcTemplate.queryForList(sb
				.toString());
		return this.getUserRank(ls, userId);
	}

	/**
	 * 考试记录按天统计
	 * 
	 * @param userId
	 * @return
	 */
	public List<Map<String, Object>> getExamRecordList(Integer userId) {
		StringBuffer sb = new StringBuffer(100);
		sb.append("  select c.*,e.title,e.EXAM_TYPE from (  ");
		sb.append("         select exam_id,sum(score) as score,sum(right1) as rightCount,sum(error1) as errorCount,sum(right1+error1) totalCount    ");
		sb.append(" 		  from(   ");
		sb.append(" 		  select exam_score as score,case when result ='Y'  then 1 when result='N' then 0 end as right1, case when result ='Y'  then 0 when result='N' then 1 end as error1, ");
		sb.append("           ed.EXAM_ID ");
		sb.append(" 		  from exam_user_result as eur,exam_detail as ed where eur.user_id = ? and eur.EXAM_DETAIL_ID = ed.ID_ ");
		sb.append(" 		   ) as b group by b.EXAM_ID ) as c,exam as e ");
		sb.append(" where c.exam_id = e.id_ order by e.START_TIME desc ");
		return this.jdbcTemplate.queryForList(sb.toString(),
				new Object[] { userId });
	}

	/**
	 * 考试记录明细
	 * 
	 * @param userId
	 * @param day
	 * @return
	 */
	public List<Map<String, Object>> getExamRecordDetail(Integer userId,
			String examId) {
		StringBuffer sb = new StringBuffer(100);
		sb.append(" select tm.content,er.answer,er.result,er.EXAM_SCORE as score from exam_user_result as er,tktm as tm,exam_detail ed where er.exam_detail_id = ed.id_ and ed.tm_id = tm.id_  ");
		sb.append(" and  er.user_id =? and ed.exam_id= ?  ");
		return this.jdbcTemplate.queryForList(sb.toString(), userId, examId);
	}

	public List<Map<String, Object>> getUserExam(Integer userId) {
		StringBuffer sb = new StringBuffer(200);
		sb.append("select  e.id_, ");
		sb.append(" DATE_FORMAT(e.START_TIME,'%Y-%m-%d %T')  START_TIME,");
		sb.append(" DATE_FORMAT(e.END_TIME,'%Y-%m-%d %T')  END_TIME,");
		sb.append(" e.TITLE,");
		sb.append(" e.EXAM_TYPE,");
		sb.append(" e.FQR_ID,");
		sb.append(" e.remark,");
		sb.append(" e.exam_time");
		sb.append(" from   ");
		sb.append("     exam as e,   ");
		sb.append("     (select distinct   ");
		sb.append("         exam_id   ");
		sb.append("     from   ");
		sb.append("         exam_user_result as eur, exam_detail as ed   ");
		sb.append("     where   ");
		sb.append("        eur.exam_detail_id = ed.id_   ");
		sb.append("             and eur.user_id = ?  ) as b   ");
		sb.append(" where   ");
		sb.append("     e.ID_ = b.exam_id  and e.exam_type='1'  order by e.start_time desc");
		return this.jdbcTemplate.queryForList(sb.toString(),
				new Object[] { userId });
	}

	public Map<String, Object> getTmByExam(ExamItem item) {
		// TODO Auto-generated method stub
		StringBuffer sql =new StringBuffer("select case when t.TMND='0' then e.jct  else  e.jct end tmfz from exam e,exam_detail d,tktm t where e.ID_=d.EXAM_ID and d.TM_ID=t.ID_");
		 sql.append(" and d.id_ = ? and d.TM_ID = ? ");
				
		return this.jdbcTemplate.queryForMap(sql.toString(), new Object[] { item.getExamDetailId(),item.getTkId() });
	}
}
