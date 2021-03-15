package com.thkmon.blogparser.main;

import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;

import com.thkmon.blogparser.common.Const;
import com.thkmon.blogparser.prototype.StringList;
import com.thkmon.blogparser.prototype.StringMap;
import com.thkmon.blogparser.prototype.StringMapList;
import com.thkmon.blogparser.util.HttpUtil;
import com.thkmon.blogparser.util.JsonUtil;
import com.thkmon.blogparser.wordpress.WordpressMapper;

public class MainClass {

	private static final String naverUserId = Const.optionProperties.get("naver_user_id");
	private static final String wordpressUrl = Const.optionProperties.get("wordpress_url");
	private static final int beginPageNum = 1;
	private static final int endPageNum = 1;
	private static final int countPerPage = 5; // 5, 10, 15, 20, 30 가능
	
	
	public static void main(String[] args) {
		try {
			MainClass mainCls = new MainClass();
			
			// 1. 최근 포스트 인서트 기능
			mainCls.insertRecentPosts();
			
			// 2. 특정 포스트 업데이트 기능
			// mainCls.updatePost("222250162215");
			
			// 블로그 이미지 경로가 포함되어있는 워드프레스 포스트들만 가져와서 내용 업데이트. 이미지 파일은 따로 FTP 업로드 해야함
			// mainCls.updatePostHavingImages();
			
		} catch (NullPointerException e) {
			e.printStackTrace();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 최근 포스트 인서트 기능
	 * 
	 * @throws SQLException
	 * @throws Exception
	 */
	private void insertRecentPosts() throws SQLException, Exception {
		StringMapList logNoList = new StringMapList();
		for (int i = beginPageNum; i <= endPageNum; i++) {
			logNoList = addNaverBlogArticleNumbers(logNoList, i);
		}

		logNoList = logNoList.reverse();

		System.out.println(logNoList);

		WordpressMapper wordpressMapper = new WordpressMapper();
		
		int totalCount = logNoList.size();
		for (int i = 0; i < totalCount; i++) {
			StringMap map = logNoList.get(i);
			wordpressMapper.insertWordpressPostFromNaverBlogPost(wordpressUrl, naverUserId, map);
		}
	}
	
	
	/**
	 * 특정 포스트 업데이트 기능
	 * 
	 * @param postNo
	 * @throws SQLException
	 * @throws Exception
	 */
	private void updatePost(String postNo) throws SQLException, Exception {
		WordpressMapper wordpressMapper = new WordpressMapper();
		wordpressMapper.updateWordpressPostFromNaverBlogPost(wordpressUrl, naverUserId, postNo);
	}
	
	
	
	/**
	 * 블로그 이미지 경로가 포함되어있는 워드프레스 포스트들만 가져와서 내용 업데이트. 이미지 파일은 따로 FTP 업로드 해야함
	 * 
	 * @throws SQLException
	 * @throws Exception
	 */
	private void updatePostHavingImages() throws SQLException, Exception {
		WordpressMapper wordpressMapper = new WordpressMapper();
		StringList postNoList = wordpressMapper.getPostNoListHavingBlogImages();
		if (postNoList != null) {
			System.out.println("postNoList.size() : " + postNoList.size());
			if (postNoList.size() > 0) {
				System.out.println("postNoList : " + postNoList);
				for (String postNo : postNoList) {
					this.updatePost(postNo);
				}
			}
		}
	}
	
	
	/**
	 * 특정 페이지의 포스트 정보들을 가져온다.
	 * 
	 * @param logNoList
	 * @param pageNumber
	 * @return
	 * @throws Exception
	 */
	private StringMapList addNaverBlogArticleNumbers(StringMapList logNoList, int pageNumber) throws SQLException, Exception {
		Hashtable<String, String> param = new Hashtable<String, String>();
		param.put("blogId", naverUserId);
		param.put("viewdate", "");
		param.put("currentPage", String.valueOf(pageNumber));
		param.put("categoryNo", "");
		param.put("parentCategoryNo", "");
		param.put("countPerPage", String.valueOf(countPerPage));

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
				// String parentCategoryNo = JsonUtil.getString(oneJsonObj, "parentCategoryNo");
				// String sourceCode = JsonUtil.getString(oneJsonObj, "sourceCode");
				// String commentCount = JsonUtil.getString(oneJsonObj, "commentCount");
				// String readCount = JsonUtil.getString(oneJsonObj, "readCount");
				String addDate = JsonUtil.getString(oneJsonObj, "addDate");
				// String openType = JsonUtil.getString(oneJsonObj, "openType");
				// String searchYn = JsonUtil.getString(oneJsonObj, "searchYn");
				// String greenReviewBannerYn = JsonUtil.getString(oneJsonObj, "greenReviewBannerYn");
				// String memologMovingYn = JsonUtil.getString(oneJsonObj, "memologMovingYn");
				// String isPostSelectable = JsonUtil.getString(oneJsonObj, "isPostSelectable");
				// String isPostNotOpen = JsonUtil.getString(oneJsonObj, "isPostNotOpen");
				// String isPostBlocked = JsonUtil.getString(oneJsonObj, "isPostBlocked");
				// String isBlockTmpForced = JsonUtil.getString(oneJsonObj, "isBlockTmpForced");

				StringMap map = new StringMap();
				map.put("logNo", logNo);
				map.put("title", title);
				map.put("categoryNo", categoryNo);
				// map.put("parentCategoryNo", parentCategoryNo);
				// map.put("sourceCode", sourceCode);
				// map.put("commentCount", commentCount);
				// map.put("readCount", readCount);
				map.put("addDate", addDate);
				// map.put("openType", openType);
				// map.put("searchYn", searchYn);
				// map.put("greenReviewBannerYn", greenReviewBannerYn);
				// map.put("memologMovingYn", memologMovingYn);
				// map.put("isPostSelectable", isPostSelectable);
				// map.put("isPostNotOpen", isPostNotOpen);
				// map.put("isPostBlocked", isPostBlocked);
				// map.put("isBlockTmpForced", isBlockTmpForced);

				logNoList.addNotDupl(map, "logNo");
			}
		}

		return logNoList;
	}
}