package com.gdky.restful.config;
public final class Constants {

	/**
	 * prefix of REST API
	 */
	public static final String URI_API_PREFIX = "/api";

	public static final String URI_PUB_PREFIX = "/pub/api";
	
	public static final String URI_API_VERSION = "1.1";

	public static final String FRAMEWORK_SCHEMA = ""; // zs_new.


	public static final String URI_API_FRAMEWORK_PRIFIX = "/api/fw";
	
	public static final String TOKEN_SECRET = "c2VjcmV0IGZvciBnZHpz";
	
	public static final String SALT = "Z2R6c21pcw==";
	
	public static final Integer TOKEN_EXPIRATION = 864000; //seconds
	
	public static final String AUTH_HEADER_NAME = "X-AUTH-TOKEN";
	
	public static final String HASHID_SALT = "gdzs & gdky";

	public static final int HASHID_LEN = 6;
	
	public  static final String UPLOAD_LOCATION="d:/upload"; //win
	
//	public  static final String UPLOAD_LOCATION="/usr/local/upload"; //linux
	
	public static final String BARCODE_LOCATION = UPLOAD_LOCATION + "/barcode";
}
