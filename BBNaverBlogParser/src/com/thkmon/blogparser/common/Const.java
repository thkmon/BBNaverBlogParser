package com.thkmon.blogparser.common;

import java.util.HashMap;

import com.thkmon.blogparser.util.PropertiesUtil;
import com.thkmon.blogparser.util.StringUtil;

public class Const {
	public static final HashMap<String, String> optionProperties = PropertiesUtil.readPropertiesFile("/test/option.properties");
	
	public static final String DB_URL = optionProperties.get("db_url");
	public static final String DB_PORT = optionProperties.get("db_port");
	public static final String DB_NAME = optionProperties.get("db_name");
	
	public static final String DB_USER = optionProperties.get("db_user");
	public static final String DB_PASSWORD = optionProperties.get("db_password");
	
	public static final String FTP_URL = optionProperties.get("ftp_url");
	public static final int FTP_PORT = StringUtil.parseInt(optionProperties.get("ftp_port"), 22);
	public static final String FTP_USER = optionProperties.get("ftp_user");
	public static final String FTP_PASSWORD = optionProperties.get("ftp_password");
	public static final String FTP_TMPDIR = optionProperties.get("ftp_tmpdir");
	
	public static final String POST_IMAGE_DIR_PATH = "/itarchives/www/imgs/";
}