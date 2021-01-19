package com.thkmon.blogparser.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class PropertiesUtil {

    /**
     * UTF-8 ���ڵ� ������ properties ������ �о HashMap ��ü�� ����� �����Ѵ�.
     *
     * @param propFilePath
     * @return
     * @throws Exception
     */
    private static HashMap<String, String> readPropertiesFileCore(String propFilePath) throws Exception {
        if (propFilePath == null || propFilePath.length() == 0) {
            throw new Exception("PropertiesUtil readPropertiesFile : propFilePath == null || propFilePath.length() == 0");
        }

        File propFileObj = new File(propFilePath);
        if (!propFileObj.exists()) {
            throw new Exception("PropertiesUtil readPropertiesFile : propFileObj does not exists. [" + propFileObj.getAbsolutePath() + "]");
        }
        
        if (!propFileObj.canRead()) {
            throw new Exception("PropertiesUtil readPropertiesFile : propFileObj can not read. [" + propFileObj.getAbsolutePath() + "]");
        }

        HashMap<String, String> resultMap = new HashMap<String, String>();

        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        try {
            fileInputStream = new FileInputStream(propFileObj);
            inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            bufferedReader = new BufferedReader(inputStreamReader);

            String oneLine = null;
            while ((oneLine = bufferedReader.readLine()) != null) {
                if (oneLine == null || oneLine.length() == 0) {
                    continue;
                }
                
                // �ּ� ����
                if (oneLine.trim().startsWith("#")) {
                    continue;
                }
                
                int equalIndex = oneLine.indexOf("=");
                if (equalIndex < 0) {
                    continue;
                }
                
                // ������(key��)�� trim ó���Ѵ�. ������(value��)�� �ǵ������� ������ ���Ե� �� �ִٰ� �Ǵ��Ѵ�.
                String leftText = oneLine.substring(0, equalIndex).trim();
                String rightText = oneLine.substring(equalIndex + 1);
                
                // ��ȣ ���� �ؽ�Ʈ�� �������� ���� ��� ����
                if (leftText.length() == 0) {
                    continue;
                }
                
                resultMap.put(leftText, rightText);
            }

        } catch (IOException e) {
            throw e;

        } catch (Exception e) {
            throw e;

        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (Exception e) {
                // ����
            } finally {
                bufferedReader = null;
            }

            try {
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
            } catch (Exception e) {
                // ����
            } finally {
                inputStreamReader = null;
            }

            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (Exception e) {
                // ����
            } finally {
                fileInputStream = null;
            }
        }

        return resultMap;
    }
    
    
    public static HashMap<String, String> readPropertiesFile(String propFilePath) {
    	HashMap<String, String> resultMap = null;
    	
    	try {
    		resultMap = readPropertiesFileCore(propFilePath);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return resultMap;
    }
}