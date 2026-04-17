package olihef.stratvis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;

@Service
public class GeoapifyService {
	private final String apiKey;
	public GeoapifyService(@Value("${geoapify.api.key:}") String apiKey) {
		this.apiKey = apiKey;
	}
	public String fetch(double lng, double lat) {
		try {
			OkHttpClient client = new OkHttpClient();
			String url = "https://api.geoapify.com/v1/geocode/reverse?lat=" + lat + "&lon=" + lng + "&apiKey=" + this.apiKey;
			Request request = new Request.Builder()
				.url(url)
				.method("GET", null)
				.build();
			Response response = client.newCall(request).execute();
			String body = response.body().string();
			return body;
		} catch (IOException e) {
			return "Placeholder";
		}
	}
}
