package gov.hygs;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.gdky.restful.config.Constants;

@RestController
@RequestMapping(value = Constants.URI_API_PREFIX)
public class TestController {

	@Autowired
    private JdbcTemplate jdbcTemplate;
	
	@RequestMapping(value = "/test", method = RequestMethod.POST)
	public ResponseEntity<?> test(){ 
		String sql ="select * from user ";
		List<Map<String,Object>> ls = jdbcTemplate.queryForList(sql);
		Map<String,Object> mp = ls.get(0);
		
        System.out.println("本程序存在5秒后自动退出");  
		return ResponseEntity.ok(mp.toString());
		
	}
	
}
