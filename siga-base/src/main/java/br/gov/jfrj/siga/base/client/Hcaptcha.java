package br.gov.jfrj.siga.base.client;

import br.gov.jfrj.siga.base.Prop;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Hcaptcha {

	public static JSONObject validar(String secretKey, String responseUIHcaptcha, String remoteip) {
		String proxyHost = Prop.get("/http.proxyHost");
		Integer proxyPort = Prop.getInt("/http.proxyPort");
		HttpURLConnection connection = null;

		try {
			connection = buildHttpConnection("https://hcaptcha.com/siteverify", proxyHost, proxyPort);
			connection.setRequestProperty("Accept", "application/json");
			connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestMethod("POST");
			connection.setDoInput(true);
			connection.setDoOutput(true);


			String requestBody = "secret="+URLEncoder.encode(secretKey, "UTF-8") +
					"&response="+URLEncoder.encode(responseUIHcaptcha, "UTF-8") +
					"&remoteip="+URLEncoder.encode(remoteip, "UTF-8");
			connection.getOutputStream().write(requestBody.getBytes());

			InputStream inputStream = connection.getInputStream();

			return new JSONObject(stringFromInputStream(inputStream));

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

	}

	private static HttpURLConnection buildHttpConnection(String url, String proxyHost, Integer proxyPort) throws IOException {
		URL urlInstance = new URL(url);

		if (proxyHost != null && !proxyHost.isEmpty()) {
			InetSocketAddress socketAddress = new InetSocketAddress(proxyHost, proxyPort);
			Proxy proxy = new Proxy(Proxy.Type.HTTP, socketAddress);
			return (HttpURLConnection) urlInstance.openConnection(proxy);
		}
		else
			return (HttpURLConnection) urlInstance.openConnection();

	}

	private static String stringFromInputStream(InputStream input) throws IOException {
		byte[] buffer = new byte[1024];
		int length;

		ByteArrayOutputStream result = new ByteArrayOutputStream();
		while ((length = input.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}

		return result.toString(StandardCharsets.UTF_8.name());
	}

}
