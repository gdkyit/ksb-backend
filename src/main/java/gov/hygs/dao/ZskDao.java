package gov.hygs.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.gdky.restful.dao.BaseJdbcDao;

@Repository
public class ZskDao extends BaseJdbcDao {
   public List<Map<String,Object>> getZsk(Integer userId){
	   StringBuffer sql =new StringBuffer(200);
	  sql.append("   select zsk.ID_,zsk.USER_ID,DATE_FORMAT(zsk.CREATE_DATE,'%Y-%m-%d %T') CREATE_DATE,zsk.SP_DATE,zsk.SPR_ID,zsk.DEPTID,zsk.CONTENT,zsk.ZSKLY_ID,zsk.TITLE,zsk.YXBZ,zsk.XYBZ,ly.TITLE as lytitle,ly.CONTENT as lycontent from  zskly as ly,zsktsnr as tsnr,zsk_jl as zsk,zsktsqz as qz ,user_group as ug ");  
	  sql.append("    ,zsdtsjl as tsjl where tsjl.id_ =  tsnr.tsjlid ");  
	  sql.append("     and tsnr.TSJLID =qz.ID_ and tsnr.ZSKID = zsk.ID_ and qz.GROUP_ID = ug.GROUP_ID and zsk.zskly_id = ly.id_ ");  
	  sql.append("     and ug.USER_ID =?  order by tsjl.tsrq desc ");  
	   List<Map<String,Object>> ls =  this.jdbcTemplate.queryForList(sql.toString(),new Object[]{userId}) ; 
	/*   if(null != ls){
		   for(Map<String,Object> zsk :ls){
			   String content = (String)zsk.get("CONTENT");
			   content = content.replaceAll("\n", "<br />");
			   zsk.put("CONTENT", content);
		   }
	   }*/
	   
	   return ls;
   }

public List<Map<String, Object>> getXdjl(Integer userId) {
	// TODO Auto-generated method stub
	   StringBuffer sql =new StringBuffer(200);
		  sql.append("   select xdjl.ID_,xdjl.USER_ID,DATE_FORMAT(xdjl.CREATE_DATE,'%Y-%m-%d %T') CREATE_DATE,xdjl.SP_DATE,xdjl.SPR_ID,xdjl.DEPTID,xdjl.CONTENT,xdjl.xdjlLY_ID,xdjl.TITLE,xdjl.YXBZ,xdjl.XYBZ,ly.TITLE as lytitle,ly.CONTENT as lycontent from  xdjlly as ly,xdjltsnr as tsnr,xdjl_jl as xdjl,xdjltsqz as qz ,user_group as ug ");  
		  sql.append("    ,zsdtsjl as tsjl where tsjl.id_ =  tsnr.tsjlid ");  
		  sql.append("     and tsnr.TSJLID =qz.ID_ and tsnr.xdjlID = xdjl.ID_ and qz.GROUP_ID = ug.GROUP_ID and xdjl.xdjlly_id = ly.id_ ");  
		  sql.append("     and ug.USER_ID =?  order by tsjl.tsrq desc ");  
		   List<Map<String,Object>> ls =  this.jdbcTemplate.queryForList(sql.toString(),new Object[]{userId}) ; 
		/*   if(null != ls){
			   for(Map<String,Object> xdjl :ls){
				   String content = (String)xdjl.get("CONTENT");
				   content = content.replaceAll("\n", "<br />");
				   xdjl.put("CONTENT", content);
			   }
		   }*/
		   
		   return ls;
}
   
  
}
