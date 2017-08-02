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
        sql.append("select ug.id_,gt.GROUP_NAME as groupname");
        sql.append(" from user_group ug, grouptable gt ");
        sql.append(" where ug.GROUP_ID = gt.ID_ ");
        sql.append(" and ug.read_mark ='N' ");
        sql.append(" and ug.user_id =? ");
        sql.append(" and (now()<=gt.effective_date or gt.effective_date is null) ");
        return this.jdbcTemplate.queryForList(sql.toString(),new Object[]{user.get("ID_")});
    }

    public void markMsgRead(Map<String, Object> user, String id) {
        StringBuffer sql = new StringBuffer();
        sql.append(" update user_group ug ");
        sql.append(" set ug.read_mark = 'Y' ");
        sql.append(" where ug.user_id =? and ug.id_ = ? ");
        this.jdbcTemplate.update(sql.toString(), new Object[]{user.get("ID_"),id});

    }
}

