package com.game.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.Map.Entry;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

@Component
public class HttpUtil {

	private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

	@Value("${xinkuai.httpPost}")
	private String httpUrl;
	@Value("${xinkuai.fixAuth}")
	private String fixAuth;
	@Value("${xinkuai.appId}")
	private String appId;

	public String getHttpUrl() {
		return httpUrl;
	}

	public void setHttpUrl(String httpUrl) {
		this.httpUrl = httpUrl;
	}

	public String getFixAuth() {
		return fixAuth;
	}

	public void setFixAuth(String fixAuth) {
		this.fixAuth = fixAuth;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public static String sendPost(String url, TreeMap<String, Object> parms) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		RequestConfig requestConfig = getRequestConfig();
		HttpPost httpPost = new HttpPost(url);
		final List<NameValuePair> nvps = new ArrayList<>();
		if (parms != null && parms.size() > 0) {
			parms.forEach((key, value) -> {
				nvps.add(new BasicNameValuePair(key, value.toString()));
			});
		}
		if (nvps.size() > 0) {
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		httpPost.setConfig(requestConfig);

		String text = null;
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
			if (response == null) {
				logger.error("response is null  url {} parms{}", url, Arrays.toString(nvps.toArray()));
				return null;
			}
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				return null;
			}
			HttpEntity responseEntity = response.getEntity();
			text = EntityUtils.toString(responseEntity, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return text;
	}

	/**
	 * post 请求返回String
	 *
	 * @param url
	 * @param parms
	 * @return
	 */
	public static String sendPost(String url, Map<String, String> parms) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		RequestConfig requestConfig = getRequestConfig();
		HttpPost httpPost = new HttpPost(url);
		final List<NameValuePair> nvps = new ArrayList<>();
		if (parms != null && parms.size() > 0) {
			parms.forEach((key, value) -> {
				nvps.add(new BasicNameValuePair(key, value));
			});
		}
		if (nvps.size() > 0) {
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		httpPost.setConfig(requestConfig);

		String text = null;
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
			if (response == null) {
				logger.error("response is null  url {} parms{}", url, Arrays.toString(nvps.toArray()));
				return null;
			}
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				return null;
			}
			HttpEntity responseEntity = response.getEntity();
			text = EntityUtils.toString(responseEntity, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return text;
	}

	/**
	 * post 请求返回String
	 *
	 * @param url
	 * @param parms
	 * @return
	 */
	public static String sendHttpPost(String url, Map<String, String> parms) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		RequestConfig requestConfig = getRequestConfig();
		HttpPost httpPost = new HttpPost(url);
		final List<NameValuePair> nvps = new ArrayList<>();
		if (parms != null && parms.size() > 0) {
			parms.forEach((key, value) -> nvps.add(new BasicNameValuePair(key, value)));
		}
		if (nvps.size() > 0) {
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		httpPost.setConfig(requestConfig);
		String text = null;
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
			if (response == null) {
				logger.error("response is null  url {} parms{}", url, Arrays.toString(nvps.toArray()));
				return null;
			}
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				return null;
			}
			HttpEntity responseEntity = response.getEntity();
			text = EntityUtils.toString(responseEntity, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return text;
	}

	private static RequestConfig getRequestConfig() {
		return RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(10000).build();
	}

	private static RequestConfig getHttpRequestConfig() {
		return RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).build();
	}

	private HttpPost creatHttpPost(String url) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		RequestConfig requestConfig = getRequestConfig();
		HttpPost httpPost = new HttpPost(url);
		httpPost.setConfig(requestConfig);

		return httpPost;
	}

	/**
	 * post 请求返回String
	 *
	 * @param url
	 * @param parms
	 * @return
	 */
	public static String sendHttpPost(String url, Map<String, String> parms, Map<String, String> headPparms) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		RequestConfig requestConfig = getRequestConfig();
		HttpPost httpPost = new HttpPost(url);
		final List<NameValuePair> nvps = new ArrayList<>();
		if (parms != null && parms.size() > 0) {
			parms.forEach((key, value) -> nvps.add(new BasicNameValuePair(key, value)));
		}
		if (nvps.size() > 0) {
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		httpPost.setConfig(requestConfig);

		if (null != headPparms && headPparms.size() > 0) {
			Set<Entry<String, String>> entrySet = headPparms.entrySet();
			for (Entry<String, String> entry : entrySet) {
				httpPost.setHeader(entry.getKey(), entry.getValue());
			}
		}

		String text = null;
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
			if (response == null) {
				logger.error("response is null  url {} parms{}", url, Arrays.toString(nvps.toArray()));
				return null;
			}
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				return null;
			}
			HttpEntity responseEntity = response.getEntity();
			text = EntityUtils.toString(responseEntity, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return text;
	}

	/**
	 * @param url
	 * @param body
	 * @return
	 * @throws Throwable
	 */
	public String httpPost(String url, String body) throws Throwable {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		RequestConfig requestConfig = getRequestConfig();
		ContentType contentType = ContentType.create("application/json", Consts.UTF_8);
		StringEntity entity = new StringEntity(body, contentType);
		entity.setChunked(true);
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(entity);
		httpPost.setConfig(requestConfig);
		CloseableHttpResponse response = httpClient.execute(httpPost);
		try {
			if (response == null) {
				return null;
			}
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				return null;
			}
			HttpEntity responseEntity = response.getEntity();
			String text = EntityUtils.toString(responseEntity, "UTF-8");
			return text;
		} finally {
			response.close();
		}
	}

	/**
	 * @param url
	 * @return
	 * @throws Throwable
	 */
	public String httpGet(String url, Map<String, String> parms) throws Throwable {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		RequestConfig requestConfig = getRequestConfig();
		HttpGet httpGet = new HttpGet(url);
		httpGet.setConfig(requestConfig);
		CloseableHttpResponse response = httpClient.execute(httpGet);
		try {
			if (response == null) {
				logger.error("response is null  url {} ", url);
				return null;
			}
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				return null;
			}
			HttpEntity responseEntity = response.getEntity();
			String text = EntityUtils.toString(responseEntity, "UTF-8");
			return text;
		} finally {
			response.close();
		}
	}

	public  void sendToKuaiYou(String message) {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		RequestConfig build = RequestConfig.custom().setConnectTimeout(3000).setConnectionRequestTimeout(3000).setSocketTimeout(3000).build();
		HttpPost httpPost = new HttpPost(httpUrl);
		String body = fixAuth + appId + message;
		StringEntity stringEntity = new StringEntity(message, "UTF-8");
		httpPost.setEntity(stringEntity);
		httpPost.setHeader("Content-Type", "application/json;charset=utf8");
		httpPost.setHeader("token", DigestUtils.md5DigestAsHex((body).getBytes()));
		httpPost.setConfig(build);
		// 响应模型
		CloseableHttpResponse response = null;
		//System.err.println(message);
		try {
			// 由客户端执行(发送)Post请求

			response = httpClient.execute(httpPost);
			// 从响应模型中获取响应实体
			HttpEntity responseEntity = response.getEntity();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// 释放资源
				if (httpClient != null) {
					httpClient.close();
				}
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String httpPostWithjson(String url, String json) throws IOException {
		String result = "";
		HttpPost httpPost = new HttpPost(url);
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
			BasicResponseHandler handler = new BasicResponseHandler();
			StringEntity entity = new StringEntity(json, "utf-8");//解决中文乱码问题
			entity.setContentEncoding("UTF-8");
			entity.setContentType("application/json");
			httpPost.setEntity(entity);
			result = httpClient.execute(httpPost, handler);
			return result;
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			try {
				httpClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
