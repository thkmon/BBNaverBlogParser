package com.thkmon.blogparser.wordpress;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.thkmon.blogparser.common.Const;
import com.thkmon.blogparser.database.SimpleDBMapper;
import com.thkmon.blogparser.database.SimpleDBUtil;
import com.thkmon.blogparser.main.JSchWrapper;
import com.thkmon.blogparser.prototype.ObjList;
import com.thkmon.blogparser.prototype.StringMap;
import com.thkmon.blogparser.util.FolderUtil;
import com.thkmon.blogparser.util.ImageUtil;

public class WordpressMapper {
	
	
	/**
	 * 네이버 블로그 포스트를 가져와서 워드프레스 DB 에 insert 한다.
	 * 
	 * @param wordpressUrl
	 * @param naverUserId
	 * @param targetMap
	 * @throws SQLException
	 * @throws Exception
	 */
	public void insertWordpressPostFromNaverBlogPost(String wordpressUrl, String naverUserId, StringMap targetMap) throws SQLException, Exception {
		if (wordpressUrl == null || wordpressUrl.length() == 0) {
			return;
		}
		
		if (naverUserId == null || naverUserId.length() == 0) {
			return;
		}
		
		if (targetMap == null) {
			return;
		}

		String postNo = targetMap.get("logNo");
		if (postNo == null || postNo.length() == 0) {
			return;
		}
		
		// 포스트넘버가 숫자인 경우에만 진행
		if (!postNo.matches("[0-9]*")) {
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
			
			
			// 스마트에디터 3.0 여부
			boolean isSmartEditor3 = false;
			if (postViewAreas == null || postViewAreas.size() == 0) {
				isSmartEditor3 = true;
			}
			
			
			// 포스트 내용 가져오기
			String strHtml = "";
			if (!isSmartEditor3) {
				Element postViewArea = postViewAreas.get(0);
				strHtml = postViewArea.html();
			} else {
				postViewAreas = doc.select(".se_component_wrap");
				Element postViewArea = postViewAreas.get(1);
				strHtml = postViewArea.html();
			}
			
			if (strHtml == null || strHtml.length() == 0) {
				System.out.println("포스트 내용을 가져올 수 없습니다. (postNo : " + postNo + ")");
				return;
			}
			
			
			// 포스트 내용 보정 (이미지 상대경로화)
			strHtml = revisePostContents(postNo, strHtml);
			
			
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

				boolean isInserted = mapper.insert(conn, query, objList);
				System.out.println("isInserted ; " + isInserted);
				
				// SFTP 로 이미지 업로드
				if (isInserted) {
					uploadImagesBySFTP(postNo);
				}
			}
			
			SimpleDBUtil.commitAndClose(conn);

		} catch (SQLException e) {
			throw e;
			
		} catch (Exception e) {
			throw e;

		} finally {
			SimpleDBUtil.rollbackAndClose(conn);
		}
	}
	
	
	/**
	 * 네이버 블로그 포스트를 가져와서 워드프레스 DB 에 insert 한다.
	 * 
	 * @param wordpressUrl
	 * @param naverUserId
	 * @param postNo
	 * @throws SQLException
	 * @throws Exception
	 */
	public void updateWordpressPostFromNaverBlogPost(String wordpressUrl, String naverUserId, String postNo) throws SQLException, Exception {
		if (wordpressUrl == null || wordpressUrl.length() == 0) {
			return;
		}
		
		if (naverUserId == null || naverUserId.length() == 0) {
			return;
		}
		
		if (postNo == null || postNo.length() == 0) {
			return;
		}
		
		// 포스트넘버가 숫자인 경우에만 진행
		if (!postNo.matches("[0-9]*")) {
			return;
		}
		
		/*
		if (targetMap == null) {
			return;
		}

		String postNo = targetMap.get("logNo");
		if (postNo == null || postNo.length() == 0) {
			return;
		}
		*/
		
		Connection conn = null;
		SimpleDBMapper mapper = new SimpleDBMapper();
		
		try {
			conn = SimpleDBUtil.getConnection();
			
			String postID  = "";
			{
				String query = " SELECT ID FROM wp_posts WHERE post_name = ? ";

				ObjList objList = new ObjList();
				objList.add(String.valueOf(postNo));

				postID = mapper.selectFirstString(conn, query, objList, "");
				if (postID == null || postID.length() == 0) {
					System.out.println("업데이트할 로우를 찾을 수 없습니다. (postNo : " + postNo + ")");
					return;
				}
			}

			// https://blog.naver.com/naverId/" + postNo
			String urlString = "https://blog.naver.com/PostView.nhn?blogId=" + naverUserId + "&logNo=" + postNo + "&redirect=Dlog&widgetTypeCall=true&directAccess=false";

			Document doc = Jsoup.connect(urlString).get();
			Elements postViewAreas = doc.select("#postViewArea");
			
			
			// 스마트에디터 3.0 여부
			boolean isSmartEditor3 = false;
			if (postViewAreas == null || postViewAreas.size() == 0) {
				isSmartEditor3 = true;
			}
			
			
			// 포스트 내용 가져오기
			String strHtml = "";
			if (!isSmartEditor3) {
				Element postViewArea = postViewAreas.get(0);
				strHtml = postViewArea.html();
			} else {
				postViewAreas = doc.select(".se_component_wrap");
				Element postViewArea = postViewAreas.get(1);
				strHtml = postViewArea.html();
			}
			
			if (strHtml == null || strHtml.length() == 0) {
				System.out.println("포스트 내용을 가져올 수 없습니다. (postNo : " + postNo + ")");
				return;
			}
			
			
			// 포스트 제목 가져오기
			String strTitle = "";
			try {
				if (!isSmartEditor3) {
					strTitle = doc.select(".itemSubjectBoldfont").get(0).html().trim();
				}
			} catch (NullPointerException e) {
				// 제목을 못가져오는 경우 제목만 제외하고 나머지 업데이트 진행
				strTitle = "";
			} catch (Exception e) {
				// 제목을 못가져오는 경우 제목만 제외하고 나머지 업데이트 진행
				strTitle = "";
			}
			
			
			// 포스트 내용 보정 (이미지 상대경로화)
			strHtml = revisePostContents(postNo, strHtml);
			
			
			{
				StringBuffer buff = new StringBuffer();

				buff.append(" UPDATE wp_posts ");
				buff.append(" SET ");
				buff.append(" 	post_author = ?, ");
//				buff.append(" 	post_date = ?, ");
//				buff.append(" 	post_date_gmt = ?, ");
				buff.append(" 	post_content = ?, ");
				if (strTitle != null && strTitle.length() > 0) {
					buff.append(" 	post_title = ?, ");
				}
				buff.append(" 	post_excerpt = ?, ");
				buff.append(" 	post_status = ?, ");
				buff.append(" 	comment_status = ?, ");
				buff.append(" 	ping_status = ?, ");
				buff.append(" 	post_password = ?, ");
//				buff.append(" 	post_name = ?, ");
				buff.append(" 	to_ping = ?, ");
				buff.append(" 	pinged = ?, ");
//				buff.append(" 	post_modified = ?, ");
//				buff.append(" 	post_modified_gmt = ?, ");
				buff.append(" 	post_content_filtered = ?, ");
				buff.append(" 	post_parent = ?, ");
				buff.append(" 	guid = ?, ");
				buff.append(" 	menu_order = ?, ");
				buff.append(" 	post_type = ?, ");
				buff.append(" 	post_mime_type = ?, ");
				buff.append(" 	comment_count = ? ");
				buff.append(" WHERE ID = ? AND post_name = ? ");

				String query = buff.toString();
				
				
				ObjList objList = new ObjList();
				objList.add(String.valueOf("1"));
//				objList.add(String.valueOf(strDate2));
//				objList.add(String.valueOf(strDate2));
				objList.add(String.valueOf(strHtml));
				if (strTitle != null && strTitle.length() > 0) {
					objList.add(String.valueOf(strTitle));
				}
				objList.add(String.valueOf(""));
				objList.add(String.valueOf("publish"));
				objList.add(String.valueOf("open"));
				objList.add(String.valueOf("open"));
				objList.add(String.valueOf(""));
//				objList.add(String.valueOf(postNo));
				objList.add(String.valueOf(""));
				objList.add(String.valueOf(""));
//				objList.add(String.valueOf(strDate2));
//				objList.add(String.valueOf(strDate2));
				objList.add(String.valueOf(""));
				objList.add(String.valueOf("0"));
				objList.add(String.valueOf(wordpressUrl + "/?p=" + postNo));
				objList.add(String.valueOf("0"));
				objList.add(String.valueOf("post"));
				objList.add(String.valueOf(""));
				objList.add(String.valueOf("0"));
				
				objList.add(String.valueOf(postID));
				objList.add(String.valueOf(postNo));

				boolean isUpdated = mapper.insert(conn, query, objList);
				System.out.println("isUpdated ; " + isUpdated);
				
				// SFTP 로 이미지 업로드
				if (isUpdated) {
					uploadImagesBySFTP(postNo);
				}
			}

			SimpleDBUtil.commitAndClose(conn);

		} catch (SQLException e) {
			throw e;
			
		} catch (Exception e) {
			throw e;

		} finally {
			SimpleDBUtil.rollbackAndClose(conn);
		}
	}
	
	
	/**
	 * 블로그 이미지 경로가 포함되어있는 워드프레스 포스트 목록 가져오기
	 * 
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	/*
	public StringList getPostNoListHavingBlogImages() throws SQLException, Exception {
		StringList resultList = new StringList();
		
		Connection conn = null;
		SimpleDBMapper mapper = new SimpleDBMapper();
		
		try {
			conn = SimpleDBUtil.getConnection();
			
			String query = " SELECT post_name, post_content FROM wp_posts ";

			BasicMapList list = mapper.select(conn, query, null);
			if (list != null && list.size() > 0) {
				BasicMap map = null;
				
				int listCount = list.size();
				for (int i=0; i<listCount; i++) {
					map = list.get(i);
					if (map == null) {
						continue;
					}
					
					String postContent = String.valueOf(map.get("post_content"));
					if (postContent.indexOf("img src=\"https://blogfiles.pstatic.net") > -1) {
					// if (postContent.indexOf("img src") > -1) {
						resultList.add(String.valueOf(map.get("post_name")));
					}
				}
			}
			
		} catch (SQLException e) {
			throw e;
			
		} catch (Exception e) {
			throw e;

		} finally {
			SimpleDBUtil.rollbackAndClose(conn);
		}
		
		return resultList;
	}
	*/
	
	/*
	public StringList getPostNoListAll() throws SQLException, Exception {
		StringList resultList = new StringList();
		
		Connection conn = null;
		SimpleDBMapper mapper = new SimpleDBMapper();
		
		try {
			conn = SimpleDBUtil.getConnection();
			
			String query = " SELECT post_name, post_content FROM wp_posts ";

			BasicMapList list = mapper.select(conn, query, null);
			if (list != null && list.size() > 0) {
				BasicMap map = null;
				
				int listCount = list.size();
				for (int i=0; i<listCount; i++) {
					map = list.get(i);
					if (map == null) {
						continue;
					}
					
					resultList.add(String.valueOf(map.get("post_name")));
				}
			}
			
		} catch (SQLException e) {
			throw e;
			
		} catch (Exception e) {
			throw e;

		} finally {
			SimpleDBUtil.rollbackAndClose(conn);
		}
		
		return resultList;
	}
	*/
	
	/**
	 * 포스트 내용 보정 (이미지 상대경로화)
	 * 
	 * @param postNo
	 * @param strHtml
	 * @return
	 */
	private String revisePostContents(String postNo, String strHtml) throws NullPointerException, Exception {
		if (strHtml == null || strHtml.length() == 0) {
			return "";
		}
		
		String parentFolderPath = Const.POST_IMAGE_DIR_PATH;
		File parentFolderObj = new File(parentFolderPath);
		if (!parentFolderObj.exists()) {
			// System.err.println("The folder does not exists. (" + parentFolderObj.getAbsolutePath() + ")");
			// return strHtml;
			throw new Exception("The folder does not exists. (" + parentFolderObj.getAbsolutePath() + ")");
		}
		
		File dir = new File(parentFolderPath + postNo + "\\");
		if (dir.exists()) {
			FolderUtil.deleteFolder(dir.getAbsolutePath());
		}
		
		Document contentsDoc = Jsoup.parse(strHtml);
		
		Elements imgElems = contentsDoc.select("img");
		if (imgElems != null && imgElems.size() > 0) {
			int elemCount = imgElems.size();
			for (int i=0; i<elemCount; i++) {
				Element imgElem = imgElems.get(i);
				if (imgElem == null) {
					continue;
				}
				
				String oneImgSrc = imgElem.attr("src");
				if (oneImgSrc == null || oneImgSrc.length() == 0) {
					continue;
				}
				
				// blogfiles.pstatic.net, dthumb-phinf.pstatic.net
				if (oneImgSrc.indexOf(".pstatic.net") > -1) {
					String realImgUrl = oneImgSrc;
					
					// 주소 뒤에 "?type="이 붙어있을 경우 떼어낸다.
					String imgUrl = realImgUrl;
					int paramTypeIndex = imgUrl.indexOf("?");
					if (paramTypeIndex > -1) {
						imgUrl = imgUrl.substring(0, paramTypeIndex);
					}
					
					// 주소에서 확장자만 가져온다.
					String fileExtOnly = "";
					int lastSlashIndex = imgUrl.lastIndexOf("/");
					if (lastSlashIndex > -1) {
						String lastSlice = imgUrl.substring(lastSlashIndex + 1);
						int lastDotIndex = lastSlice.lastIndexOf(".");
						if (lastDotIndex > -1) {
							fileExtOnly = lastSlice.substring(lastDotIndex + 1);
						}
					}
					
					if (fileExtOnly == null || fileExtOnly.length() == 0) {
						fileExtOnly = "png";
					}
					
					String savePath = parentFolderPath + postNo + "\\" + String.format("%04d", i + 1) + "." + fileExtOnly;
					String newImgSrc = "/imgs/" + postNo + "/" + String.format("%04d", i + 1) + "." + fileExtOnly;
					
					boolean idDownloaded = false;
					
					// 해상도 낮은 이미지 가져오지 않도록 개선.
					if (!idDownloaded && realImgUrl.indexOf("?type=w80_blur") > -1) {
						idDownloaded = ImageUtil.downloadImgFromUrl(realImgUrl.replace("?type=w80_blur", "?type=w1"), savePath);
					}
					
					if (!idDownloaded) {
						idDownloaded = ImageUtil.downloadImgFromUrl(realImgUrl, savePath);
					}
					
					if (idDownloaded) {
						strHtml = strHtml.replace(oneImgSrc, newImgSrc);
					}
				}
			}
		}
		
		return strHtml;
	}
	
	
	/**
	 * SFTP 로 이미지 업로드
	 * 
	 * @param postNo
	 * @return
	 * @throws Exception
	 */
	public boolean uploadImagesBySFTP(String postNo) throws Exception {
		String parentFolderPath = Const.POST_IMAGE_DIR_PATH;
		File parentFolderObj = new File(parentFolderPath);
		if (!parentFolderObj.exists()) {
			return false;
		}
		
		File dir = new File(parentFolderPath + postNo + "\\");
		if (!dir.exists()) {
			return false;
		}
		
		File[] fileArr = dir.listFiles();
		if (fileArr == null || fileArr.length == 0) {
			return false;
		}
		
		JSchWrapper jschWrapper = null;
		
		int totalFileCount = 0;
		int uploadedFileCount = 0;
		
		try {
			jschWrapper = new JSchWrapper();
			
			// SFTP 접속하기
			jschWrapper.connectSFTP(Const.FTP_URL, Const.FTP_PORT, Const.FTP_USER, Const.FTP_PASSWORD);
			
			String ftpImageDirPath = Const.FTP_TMPDIR;
			if (ftpImageDirPath == null || ftpImageDirPath.length() == 0) {
				return false;
			}
			
			// 폴더 생성
			jschWrapper.mkdir(ftpImageDirPath, postNo);
			
			File oneFile = null;
			totalFileCount = fileArr.length;
			for (int i=0; i<totalFileCount; i++) {
				oneFile = fileArr[i];
				if (oneFile == null || !oneFile.exists()) {
					continue;
				}
				
				// 파일 업로드
				boolean isSuccess = jschWrapper.uploadFile(oneFile.getAbsolutePath(), ftpImageDirPath + "/" + postNo);
				if (isSuccess) {
					uploadedFileCount++;
				}
			}
			
		} catch (Exception e) {
			throw e;

		} finally {
			// SFTP 접속해제
			jschWrapper.disconnectSFTP();
		}
		
		if (uploadedFileCount == totalFileCount && totalFileCount > 0) {
			FolderUtil.deleteFolder(dir.getAbsolutePath());
			return true;
		} else {
			return false;
		}
	}
}