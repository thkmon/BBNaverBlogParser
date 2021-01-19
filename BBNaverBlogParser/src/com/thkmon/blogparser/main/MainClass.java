package com.thkmon.blogparser.main;

import java.net.URLDecoder;
import java.sql.Connection;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.thkmon.blogparser.common.Const;
import com.thkmon.blogparser.database.SimpleDBMapper;
import com.thkmon.blogparser.database.SimpleDBUtil;
import com.thkmon.blogparser.prototype.ObjList;
import com.thkmon.blogparser.prototype.StringMap;
import com.thkmon.blogparser.prototype.StringMapList;
import com.thkmon.blogparser.util.HttpUtil;
import com.thkmon.blogparser.util.JsonUtil;

public class MainClass {

	private static final String naverUserId = Const.optionProperties.get("naver_user_id");
	private static final String wordpressUrl = Const.optionProperties.get("wordpress_url");
	private static final int beginPageNum = 1;
	private static final int endPageNum = 1;
	
	public static void main(String[] args) {
		try {
			MainClass mainCls = new MainClass();
			mainCls.main();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void main() throws Exception {
		// String folerPath = "C:\\test";
		// String fileName = "naver_blog_article_numbers.txt";
		// String filePath = folerPath + "\\" + fileName;
		// File dirObj = new File(folerPath);
		// if (!dirObj.exists()) {
		// System.err.println("The folder does not exist. (" + folerPath + ")");
		// return;
		// }

		// int beginPageNum = 1;
		// int endPageNum = 1;
		StringMapList logNoList = new StringMapList();
		for (int i = beginPageNum; i <= endPageNum; i++) {
			logNoList = addNaverBlogArticleNumbers(logNoList, i);
		}

		logNoList = logNoList.reverse();

		System.out.println(logNoList);

		int totalCount = logNoList.size();
		for (int i = 0; i < totalCount; i++) {
			StringMap map = logNoList.get(i);
			insertWordpressPostFromNaverBlogPost(map);
		}

		// StringList fileContent = new StringList();
		// int listCount = logNoList.size();
		// if (listCount > 0) {
		// for (int i = 0; i < listCount; i++) {
		// fileContent.add(logNoList.get(i).get("logNo"));
		// }
		// }
		//
		// FileUtil.writeFile(filePath, fileContent, false);
	}

	/**
	 * 특정 페이지의 포스트 정보들을 가져온다.
	 * 
	 * @param logNoList
	 * @param pageNumber
	 * @return
	 * @throws Exception
	 */
	private StringMapList addNaverBlogArticleNumbers(StringMapList logNoList, int pageNumber) throws Exception {
		Hashtable<String, String> param = new Hashtable<String, String>();
		param.put("blogId", naverUserId);
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

	/**
	 * 네이버 블로그 포스트를 가져와서 워드프레스 DB 에 insert 한다.
	 * 
	 * @param targetMap
	 * @throws Exception
	 */
	private void insertWordpressPostFromNaverBlogPost(StringMap targetMap) throws Exception {
		if (targetMap == null) {
			return;
		}

		String postNo = targetMap.get("logNo");
		if (postNo == null || postNo.length() == 0) {
			return;
		}

		Connection conn = null;
		SimpleDBMapper mapper = new SimpleDBMapper();

		try {
			conn = SimpleDBUtil.getConnection();

			{
				String query = " SELECT COUNT(*) FROM wp_posts WHERE post_name = ? ";

				ObjList objList = new ObjList();
				objList.add(String.valueOf(postNo));

				int rowCount = mapper.selectFirstInt(conn, query, objList, 0);
				if (rowCount > 0) {
					return;
				}
			}

			// https://blog.naver.com/naverId/" + postNo
			String urlString = "https://blog.naver.com/PostView.nhn?blogId=" + naverUserId + "&logNo=" + postNo + "&redirect=Dlog&widgetTypeCall=true&directAccess=false";

			Document doc = Jsoup.connect(urlString).get();
			Elements postViewAreas = doc.select("#postViewArea");
			
			// 스마트에디터 3.0
			if (postViewAreas == null || postViewAreas.size() == 0) {
				// postViewAreas = doc.select(".se_component_wrap");
				System.err.println("해당 글은 가져올 수 없습니다. 스마트에디터 3.0인지 확인하시기 바랍니다.");
				return;
			}
			
			Element postViewArea = postViewAreas.get(0);
			

			String strHtml = postViewArea.html();
			
			int nextID = 0;
			{
				String query = " SELECT MAX(ID) + 1 FROM wp_posts ";
				nextID = mapper.selectFirstInt(conn, query, null, 0);
			}

			{
				StringBuffer buff = new StringBuffer();

				buff.append(" INSERT INTO ");
				buff.append(" wp_posts ( ");
				buff.append(" 	ID, ");
				buff.append(" 	post_author, ");
				buff.append(" 	post_date, ");
				buff.append(" 	post_date_gmt, ");
				buff.append(" 	post_content, ");
				buff.append(" 	post_title, ");
				buff.append(" 	post_excerpt, ");
				buff.append(" 	post_status, ");
				buff.append(" 	comment_status, ");
				buff.append(" 	ping_status, ");
				buff.append(" 	post_password, ");
				buff.append(" 	post_name, ");
				buff.append(" 	to_ping, ");
				buff.append(" 	pinged, ");
				buff.append(" 	post_modified, ");
				buff.append(" 	post_modified_gmt, ");
				buff.append(" 	post_content_filtered, ");
				buff.append(" 	post_parent, ");
				buff.append(" 	guid, ");
				buff.append(" 	menu_order, ");
				buff.append(" 	post_type, ");
				buff.append(" 	post_mime_type, ");
				buff.append(" 	comment_count ");
				buff.append(" ) values ( ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	?, ");
				buff.append(" 	? ");
				buff.append(" ) ");

				String query = buff.toString();

				String strTitle = targetMap.get("title");
				String strDate = targetMap.get("addDate");
				String[] strDateArr = strDate.split("\\.");
				
				try {
					Integer.parseInt(strDateArr[0].trim());
				} catch (NumberFormatException e) {
					System.err.println("하루가 지나지 않은 포스트는 업로드할 수 없습니다.");
					return;
				}
				
				String strYear = String.format("%04d", Integer.parseInt(strDateArr[0].trim()));
				String strMonth = String.format("%02d", Integer.parseInt(strDateArr[1].trim()));
				String strDay = String.format("%02d", Integer.parseInt(strDateArr[2].trim()));

				String strDate2 = strYear + "-" + strMonth + "-" + strDay + " " + "00:00:00";

				ObjList objList = new ObjList();
				objList.add(String.valueOf(nextID));
				objList.add(String.valueOf("1"));
				objList.add(String.valueOf(strDate2));
				objList.add(String.valueOf(strDate2));
				objList.add(String.valueOf(strHtml));
				objList.add(String.valueOf(strTitle));
				objList.add(String.valueOf(""));
				objList.add(String.valueOf("publish"));
				objList.add(String.valueOf("open"));
				objList.add(String.valueOf("open"));
				objList.add(String.valueOf(""));
				objList.add(String.valueOf(postNo));
				objList.add(String.valueOf(""));
				objList.add(String.valueOf(""));
				objList.add(String.valueOf(strDate2));
				objList.add(String.valueOf(strDate2));
				objList.add(String.valueOf(""));
				objList.add(String.valueOf("0"));
				objList.add(String.valueOf(wordpressUrl + "/?p=" + postNo));
				objList.add(String.valueOf("0"));
				objList.add(String.valueOf("post"));
				objList.add(String.valueOf(""));
				objList.add(String.valueOf("0"));

				boolean bInserted = mapper.insert(conn, query, objList);
				System.out.println("bInserted ; " + bInserted);
			}

			SimpleDBUtil.commitAndClose(conn);

		} catch (Exception e) {
			throw e;

		} finally {
			SimpleDBUtil.rollbackAndClose(conn);
		}
	}
}