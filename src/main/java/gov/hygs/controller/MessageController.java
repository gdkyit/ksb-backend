package gov.hygs.controller;

import com.gdky.restful.config.Constants;
import com.gdky.restful.entity.ResponseMessage;
import gov.hygs.service.MessageService;
import gov.hygs.service.TkxxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017-07-07.
 */

@RestController
@RequestMapping(value = Constants.URI_API_PREFIX)
public class MessageController {
    @Autowired
    private MessageService messageService;

    @Autowired
    private TkxxService tkxxService;

    @GetMapping(value = "/messages/groupchange")
    public ResponseEntity<?> getGroupChangeMsg(){
        Map<String,Object> user = tkxxService.getCurrentUser();
        return new ResponseEntity<>(this.messageService.getGroupChangeMsg(user), HttpStatus.OK);
    }
}
