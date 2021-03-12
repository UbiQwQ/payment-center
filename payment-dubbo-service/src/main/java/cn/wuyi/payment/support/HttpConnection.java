package cn.wuyi.payment.support;

import org.apache.commons.lang.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;


public class HttpConnection {

	/**
	 *
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyManagementException 
	 */
	public static  String getUrl(String url, String encoding,String referer) throws NoSuchAlgorithmException, KeyManagementException {
		  TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }

	            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
	            }

	            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
	            }
	        } };
	        // "TLSv1.2"不正确的话，会出现"Received fatal alert: handshake_failure"异常
	        // Ref：logback.qos.ch/manual/usingSSL_ja.html
	        SSLContext sc = SSLContext.getInstance("TLSv1.2");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		if (StringUtils.isEmpty(encoding) || StringUtils.equals(encoding, "null")) {
			encoding = "utf-8";
		}

		InputStream ins = null;
		HttpURLConnection conn = null;
		try {
			// logger.info(Utils.getTime() + "get>>>" + url);
			URL serverUrl = new URL(url);
			conn = (HttpURLConnection) serverUrl.openConnection();
			conn.setRequestMethod("GET");// "POST" ,"GET"

			// wft
			conn.setConnectTimeout(1000 * 60 * 3);
			conn.setReadTimeout(1000 * 60 * 3);

			// conn.setDoOutput(true);
			
			conn.addRequestProperty("Referer", referer);
			conn.addRequestProperty("Cookie", "");
			conn.addRequestProperty("Accept-Charset", "GB2312;");// GB2312,
			conn.addRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2.8) Firefox/3.6.8");
			conn.connect();

			// if (conn.getHeaderFields().get("Set-Cookie") != null) {
			// for (String s : conn.getHeaderFields().get("Set-Cookie")) {
			// cookie += s;
			// }
			// }
			if (conn.getResponseCode() >= 400) {
				ins = conn.getErrorStream();
			} else {
				ins = conn.getInputStream();
			}
			InputStreamReader inr = new InputStreamReader(ins, encoding);
			BufferedReader bfr = new BufferedReader(inr);

			String line = "";
			StringBuffer res = new StringBuffer();
			do {
				res.append(line);
				line = bfr.readLine();
				// logger.info(line);
			} while (line != null);

			return res.toString();
		} catch (Exception e) {
			// Utils.PrintInfo(url, getClass().getSimpleName());
			 e.printStackTrace();
			return null;
		} finally {
			if (ins != null) {
				try {
					ins.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
	
}
