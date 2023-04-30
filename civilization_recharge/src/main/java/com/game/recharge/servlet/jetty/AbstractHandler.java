/**
 * 
 */
package com.game.recharge.servlet.jetty;

import com.alibaba.fastjson.JSON;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 *
 */
public abstract class AbstractHandler {
	protected static final String OK = "ok";
	protected static final String ERROR = "error";

	/**
	 * @param request
	 * @return
	 */
	protected String getRequestJson(HttpServletRequest request)
			throws IOException {
		InputStream in = request.getInputStream();
		int length = 0;
		ByteArrayOutputStream bos = null;
		DataOutputStream output = null;
		try {
			bos = new ByteArrayOutputStream();
			output = new DataOutputStream(bos);
			byte[] data = new byte[4096];
			while ((length = in.read(data, 0, data.length)) != -1) {
				output.write(data, 0, length);
			}
			byte[] bytes = bos.toByteArray();
			String text = new String(bytes, "UTF-8");
			return text;
		} finally {
			if (output != null) {
				output.close();
			}
		}
	}

	/**
	 * @param request
	 * @param _class
	 * @return
	 */
	protected <T> T getPostObject(HttpServletRequest request, Class<T> _class)
			throws IOException {
		InputStream in = request.getInputStream();
		int length = 0;
		ByteArrayOutputStream bos = null;
		DataOutputStream output = null;
		try {
			bos = new ByteArrayOutputStream();
			output = new DataOutputStream(bos);
			byte[] data = new byte[4096];
			// int totalLen = 0;
			while ((length = in.read(data, 0, data.length)) != -1) {
				output.write(data, 0, length);
				// totalLen +=length;
			}
			byte[] bytes = bos.toByteArray();
			String text = new String(bytes, "UTF-8");
			if (text == null || text.length() < 1) {
				return null;
			}
			return JSON.parseObject(text, _class);
		} finally {
			if (output != null) {
				output.close();
			}
		}

	}

	/**
	 * 获取流中的字符串
	 * 
	 * @param is
	 * @return
	 */
	protected String stream2String(InputStream is) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			String line = "";
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			tryClose(br);
		}
		return "";
	}

	/**
	 * 向客户端应答结果
	 *
	 * @param response
	 * @param content
	 */
	protected void sendToClient(HttpServletResponse response, String content) {
		response.setContentType("text/plain;charset=utf-8");
		try {
			PrintWriter writer = response.getWriter();
			writer.write(content);
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭输出流
	 *
	 * @param os
	 */
	protected void tryClose(OutputStream os) {
		try {
			if (null != os) {
				os.close();
				os = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭writer
	 *
	 * @param writer
	 */
	protected void tryClose(Writer writer) {
		try {
			if (null != writer) {
				writer.close();
				writer = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭Reader
	 *
	 * @param reader
	 */
	protected void tryClose(Reader reader) {
		try {
			if (null != reader) {
				reader.close();
				reader = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final SimpleDateFormat datetimeFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	protected boolean after(Date dateTime, String dateExpression) {
		if (dateTime == null)
			return true;
		if (dateExpression != null && dateExpression.length() > 0
				&& (dateExpression = dateExpression.trim()).length() > 0) {
			try {
				Date date = datetimeFormat.parse(dateExpression);
				return dateTime.after(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	protected boolean before(Date dateTime, String dateExpression) {
		if (dateTime == null)
			return true;
		if (dateExpression != null && dateExpression.length() > 0
				&& (dateExpression = dateExpression.trim()).length() > 0) {
			try {
				Date date = datetimeFormat.parse(dateExpression);
				return dateTime.before(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	protected boolean contains(String str, String expression) {
		if (expression != null && expression.length() > 0) {
			if (expression.contains("%")) {
				String value = expression.replace("%", "");
				if (!str.contains(value)) {
					return false;
				}
			} else {
				if (!expression.equals(str)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	public abstract void handle(HttpServletRequest request,
			HttpServletResponse response) throws IOException;
}
