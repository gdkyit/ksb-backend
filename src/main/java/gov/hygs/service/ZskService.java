package gov.hygs.service;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import gov.hygs.dao.ZskDao;

import org.springframework.stereotype.Component;

@Component
public class ZskService {
	@Resource
	private ZskDao zskDak;
	 public List<Map<String,Object>> getZsk(Integer userId){
		 return this.zskDak.getZsk(userId);
	 }
	public  List<Map<String,Object>>  getXdjl(Integer userId) {
		// TODO Auto-generated method stub
		 return this.zskDak.getXdjl(userId);
	}
}
