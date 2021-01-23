package com.thkmon.blogparser.common;

import java.util.HashMap;

import com.thkmon.blogparser.util.PropertiesUtil;

public class Const {
	public static final HashMap<String, String> optionProperties = PropertiesUtil.readPropertiesFile("/test/option.properties");
	
	public static final String DB_URL = optionProperties.get("db_url");
	public static final String DB_PORT = optionProperties.get("db_port");
	public static final String DB_NAME = optionProperties.get("db_name");
	
	public static final String DB_USER = optionProperties.get("db_user");
	public static final String DB_PASSWORD = optionProperties.get("db_password");
}