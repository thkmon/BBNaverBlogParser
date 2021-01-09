package com.thkmon.blogparser.util;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;

import com.thkmon.blogparser.prototype.StringMap;
import com.thkmon.blogparser.prototype.StringMapList;

public class HttpUtil {

    public static String postHttp(String urlString, Hashtable<String, String> param, String encoding) throws Exception {
        StringBuffer paramBuffer = new StringBuffer();
        if (param != null) {
            Enumeration keys = param.keys();
            while (keys.hasMoreElements()) {
                Object objKey = keys.nextElement();
                if (objKey == null) {
                    continue;
                }

                String strKey = String.valueOf(objKey);

                Object objValue = param.get(strKey);
                if (objValue == null) {
                    objValue = "";
                }

                String strValue = String.valueOf(objValue);
                strValue = URLEncoder.encode(strValue, "UTF-8");
                
                if (paramBuffer.length() > 0) {
                    paramBuffer.append("&").append(strKey).append("=").append(strValue);
                } else {
                    paramBuffer.append(strKey).append("=").append(strValue);
                }
            }
        }

        return postHttp(urlString, paramBuffer.toString(), encoding);
    }
    
    
    public static String postHttp(String urlString, String parameters, String encoding) throws Exception {
        if (encoding == null || encoding.length() == 0) {
            encoding = "UTF-8";
        }

        HttpURLConnection ucon = null;
        String retVal = null;

        OutputStream os = null;
        java.io.DataOutputStream wr = null;
        InputStream is = null;

        try {
            URL url = new URL(urlString);

            ucon = (HttpURLConnection) url.openConnection();

            ucon.setRequestMethod("POST");

            ucon.setDoOutput(true);
            ucon.setUseCaches(false);

            ucon.setRequestProperty("Accept-Language", encoding);
            ucon.setRequestProperty("connection", "Keep-Alive");
            ucon.setRequestProperty("cache-control", "no-cache");
            ucon.setRequestMethod("POST");

            os = ucon.getOutputStream();
            
            wr = new java.io.DataOutputStream(os);
            wr.writeBytes(parameters);
            wr.flush();
            wr.close();

            int status = ucon.getResponseCode();

            if (status >= HttpURLConnection.HTTP_OK || status < HttpURLConnection.HTTP_MULT_CHOICE) {
                is = ucon.getInputStream();
                StringBuffer buf = new StringBuffer();

                int c = 0;

                while ((c = is.read()) != -1) {
                    buf.append((char) c);
                }

                retVal = buf.toString();
//                retVal = new String(retVal.getBytes("iso-8859-1"));
            }
            ucon.disconnect();

        } catch (Exception e) {
            throw e;

        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
            } catch (Exception e) {
            }

            try {
                if (wr != null) {
                    wr.close();
                }
            } catch (IOException e) {
            } catch (Exception e) {
            }

            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
            } catch (Exception e) {
            }

            try {
                if (ucon != null) {
                    ucon.disconnect();
                }
            } catch (NullPointerException e) {
            } catch (Exception e) {
            }
        }

        return retVal;
    }
}