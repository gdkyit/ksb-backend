package gov.hygs.dao;

import com.gdky.restful.dao.BaseJdbcDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017-07-07.
 */
@Repository
public class MessageDao extends BaseJdbcDao {
    public List<Map<String,Object>> getGroupChangeMsg(Map<String, Object> user){
        StringBuffer sql = new StringBuffer();
        sql.append("select gt.GROUP_NAME as groupname");
        sql.append(" from user_group ug, grouptable gt ");
        sql.append(" where ug.GROUP_ID = gt.ID_ ");
        sql.append(" and ug.read_mark ='N' ");
        sql.append(" and ug.user_id =? ");
        return this.jdbcTemplate.queryForList(sql.toString(),new Object[]{user.get("ID_")});
    }
}

