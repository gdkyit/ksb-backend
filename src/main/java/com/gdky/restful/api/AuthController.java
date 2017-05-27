package com.gdky.restful.api;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.gdky.restful.config.Constants;
import com.gdky.restful.entity.AuthRequest;
import com.gdky.restful.entity.AuthResponse;
import com.gdky.restful.entity.Role;
import com.gdky.restful.security.CustomUserDetails;
import com.gdky.restful.security.TokenUtils;
import com.gdky.restful.service.AuthService;
import com.gdky.restful.service.UserLogService;

@RestController
@RequestMapping(value = Constants.URI_API_PREFIX)
public class AuthController {
	
	  @Autowired
	  private AuthenticationManager authenticationManager;

	  @Autowired
	  private TokenUtils tokenUtils;
	  
	  @Autowired
	  private HttpServletRequest httpRequest;
	  
	  @Autowired
	  private UserLogService userLogService;
	  
	  @Autowired
	  private UserDetailsService userDetailsService;
	 // @Autowired
	 // private AccountService accountService;
	  
	  @Autowired
	  private AuthService authService;
	  
	
	/**
	 * 身份认证接口，使用jwt验证，以post方式提交{"username":"<name>","password":"<password>"}
	 * 成功后获取一个hash过的token
	 * {"token" : "<token hasn>"}
	 * 访问验证api时，在请求头部加上 x-auth-token: <token hasn>
	 * 测试验证api /protect/api
	 * @throws AuthenticationException
	 */
	@RequestMapping(value = "/auth", method = RequestMethod.POST)
	public ResponseEntity<?> login(@RequestBody AuthRequest authReq,HttpServletRequest req) throws AuthenticationException{
		//进行验证
	    Authentication authentication = this.authenticationManager.authenticate(
	      new UsernamePasswordAuthenticationToken(
	        authReq.getUsername(),
	        authReq.getPassword()
	      )
	    );
	    
	    SecurityContextHolder.getContext().setAuthentication(authentication);

	    // Reload password post-authentication so we can generate token
	    CustomUserDetails userDetails = (CustomUserDetails) this.userDetailsService.loadUserByUsername(authReq.getUsername());
	    Role role = authService.getRolesByUser(userDetails.getUsername()).get(0);
	    //List<AsideMenu> menu = accountService.getMenuByUser(userDetails.getId());
	    String token = this.tokenUtils.generateToken(userDetails);
	    
	    //登记登录信息
	    userLogService.addLog(userDetails, authService.getForWAddr(httpRequest), "用户登录");
	    
	    AuthResponse resp = new AuthResponse(token);
	    resp.setTokenhash(token);
	    resp.setJgId(userDetails.getJgId());
	    //resp.setPermission(accountService.getPermissionByUser(userDetails));
	    resp.setLo(role.getId());
	  //  resp.setMenu(menu);
	    resp.setNames(userDetails.getNames());
	    

	    // 返回 token与账户信息
	    return ResponseEntity.ok(resp);
	}
	
	@RequestMapping(value = "/auth", method = RequestMethod.GET)
	public ResponseEntity<?> validateAuth() {
		return ResponseEntity.ok("ok");
	}

}