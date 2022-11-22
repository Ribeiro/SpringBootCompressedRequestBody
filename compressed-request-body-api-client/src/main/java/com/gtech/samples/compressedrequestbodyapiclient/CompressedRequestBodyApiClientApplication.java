package com.gtech.samples.compressedrequestbodyapiclient;

import com.gtech.samples.compressedrequestbodyapiclient.model.Order;
import com.gtech.samples.compressedrequestbodyapiclient.util.JsonUtils;
import okhttp3.*;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.IOException;

@SpringBootApplication
public class CompressedRequestBodyApiClientApplication implements CommandLineRunner {

	private static final boolean SHOULD_COMPRESS_REQUEST_BODY = true;
    private static final String LOCAL_SERVER_ORDERS_ENDPOINT = "http://localhost:8080/orders";
    public static final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json");
    private static Logger LOG = LoggerFactory.getLogger(CompressedRequestBodyApiClientApplication.class);

    public static void main(String[] args) {
        LOG.info("STARTING THE APPLICATION");
        SpringApplication.run(CompressedRequestBodyApiClientApplication.class, args);
        LOG.info("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) throws Exception {
		OkHttpClient client = new OkHttpClient();

        RequestBody jsonRequestBody = RequestBody.create(JsonUtils.toJson(new Order(1L, "John Smith", 2400.00)), MEDIA_TYPE_JSON);
        Request originalRequest = new Request.Builder()
											 .url(LOCAL_SERVER_ORDERS_ENDPOINT)
											 .post(jsonRequestBody)
											 .build();

        LOG.info("REQUEST COMPRESSED? " + SHOULD_COMPRESS_REQUEST_BODY);

		try (Response response = client.newCall(compress(originalRequest)).execute()) {
			if (!response.isSuccessful()){
				throw new IOException("Unexpected code " + response);
			}

            LOG.info("SERVER RESPONSE: " + response.body().string());
		}

    }

	private Request compress(Request originalRequest){
		if(!SHOULD_COMPRESS_REQUEST_BODY){
			return originalRequest;
		}

		return originalRequest.newBuilder()
								.header("Content-Encoding", "gzip")
								.method(originalRequest.method(), gzip(originalRequest.body()))
								.build();
	}

    private RequestBody gzip(final RequestBody body) {
        return new RequestBody() {
            @Override
            public void writeTo(@NotNull BufferedSink bufferedSink) throws IOException {
                BufferedSink gzipSink = Okio.buffer(new GzipSink(bufferedSink));
                body.writeTo(gzipSink);
                gzipSink.close();
            }

            @Override
            public MediaType contentType() {
                return body.contentType();
            }

            @Override
            public long contentLength() {
                return -1; //We don't know the compressed length in advance!
            }
        };
    }

}
