package com.gdky.restful.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to transport messages back to the client.
 *
 */
public class ResponseMessage {

	private String code;
	private Object data;
	private String error;

	public ResponseMessage(String error, String code, Object data) {
		this.code = code;
		this.data = data;
		this.error = error;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public static ResponseMessage success(Object data) {
		return new ResponseMessage("", "200", data);
	}

	public static ResponseMessage error(String code, String error) {
		return new ResponseMessage(error, code, "");
	}

}