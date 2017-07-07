package gov.hygs.service;

import com.gdky.restful.entity.ResponseMessage;
import gov.hygs.dao.MessageDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017-07-07.
 */
@Transactional
@Service
public class MessageService {

    @Resource
    private MessageDao messageDao;
    public ResponseMessage getGroupChangeMsg(Map<String, Object> user) {
        List<Map<String,Object>> ls = this.messageDao.getGroupChangeMsg(user);
        if (ls.size()>0){
            return new ResponseMessage("", "200", ls);
        }else{
            return new ResponseMessage("", "204", ls);
        }
    }
}
