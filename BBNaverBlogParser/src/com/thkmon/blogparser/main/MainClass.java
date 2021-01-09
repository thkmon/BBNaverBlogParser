package com.thkmon.blogparser.main;

import java.io.File;
import java.net.URLDecoder;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;

import com.thkmon.blogparser.prototype.StringList;
import com.thkmon.blogparser.prototype.StringMap;
import com.thkmon.blogparser.prototype.StringMapList;
import com.thkmon.blogparser.util.FileUtil;
import com.thkmon.blogparser.util.HttpUtil;
import com.thkmon.blogparser.util.JsonUtil;

public class MainClass {
	
	
	public static void main(String[] args) {
		try {
			MainClass mainCls = new MainClass();
			mainCls.main();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void main() throws Exception {
		String folerPath = "C:\\test";
		String fileName = "naver_blog_article_numbers.txt";
		String filePath = folerPath + "\\" + fileName;
		File dirObj = new File(folerPath);
		if (!dirObj.exists()) {
			System.err.println("The folder does not exist. (" + folerPath + ")");
			return;
		}
		
		int beginPageNum = 1;
		int endPageNum = 34;
		StringMapList logNoList = new StringMapList();
		for (int i=beginPageNum; i<=endPageNum; i++) {
			logNoList = addNaverBlogArticleNumbers(logNoList, i);
		}
		
		logNoList = logNoList.reverse();
		
		System.out.println(logNoList);
		
		StringList fileContent = new StringList();
		int listCount = logNoList.size();
		if (listCount > 0) {
			for (int i=0; i<listCount; i++) {
				fileContent.add(logNoList.get(i).get("logNo"));
			}
		}
		
		FileUtil.writeFile(filePath, fileContent, false);
	}
	
	
	private StringMapList addNaverBlogArticleNumbers(StringMapList logNoList, int pageNumber) throws Exception {
		Hashtable<String, String> param = new Hashtable<String, String>();
		param.put("blogId", "bb_");
		param.put("viewdate", "");
		param.put("currentPage", String.valueOf(pageNumber));
		param.put("categoryNo", "");
		param.put("parentCategoryNo", "");
		param.put("countPerPage", "30");
		
		String strUrl = "https://blog.naver.com/PostTitleListAsync.nhn";
		String result = HttpUtil.postHttp(strUrl, param, "UTF-8");

		JSONObject jsonObj = JsonUtil.parseJsonObject(result);
		JSONArray jsonArray = JsonUtil.getJSONArray(jsonObj, "postList");
		if (jsonArray != null) {
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject oneJsonObj = jsonArray.getJSONObject(i);
				if (oneJsonObj == null) {
					continue;
				}

				String logNo = JsonUtil.getString(oneJsonObj, "logNo");
				String title = JsonUtil.getString(oneJsonObj, "title");
				if (title != null && title.indexOf("%") > -1) {
					title = URLDecoder.decode(title, "UTF-8");
				}
				
				String categoryNo = JsonUtil.getString(oneJsonObj, "categoryNo");
				String parentCategoryNo = JsonUtil.getString(oneJsonObj, "parentCategoryNo");
				String sourceCode = JsonUtil.getString(oneJsonObj, "sourceCode");
				String commentCount = JsonUtil.getString(oneJsonObj, "commentCount");
				String readCount = JsonUtil.getString(oneJsonObj, "readCount");
				String addDate = JsonUtil.getString(oneJsonObj, "addDate");
				String openType = JsonUtil.getString(oneJsonObj, "openType");
				String searchYn = JsonUtil.getString(oneJsonObj, "searchYn");
				String greenReviewBannerYn = JsonUtil.getString(oneJsonObj, "greenReviewBannerYn");
				String memologMovingYn = JsonUtil.getString(oneJsonObj, "memologMovingYn");
				String isPostSelectable = JsonUtil.getString(oneJsonObj, "isPostSelectable");
				String isPostNotOpen = JsonUtil.getString(oneJsonObj, "isPostNotOpen");
				String isPostBlocked = JsonUtil.getString(oneJsonObj, "isPostBlocked");
				String isBlockTmpForced = JsonUtil.getString(oneJsonObj, "isBlockTmpForced");
				
				StringMap map = new StringMap();
				map.put("logNo", logNo);
				map.put("title", title);
				logNoList.addNotDupl(map, "logNo");
			}
		}
		
		return logNoList;
	}
}