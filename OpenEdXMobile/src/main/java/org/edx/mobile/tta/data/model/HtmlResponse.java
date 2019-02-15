package org.edx.mobile.tta.data.model;

import android.provider.DocumentsContract;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Url;

public class HtmlResponse {

    private String content;

    public HtmlResponse(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static final class HtmlResponseConverter implements Converter<ResponseBody, HtmlResponse>{

        public static final Converter.Factory FACTORY = new Converter.Factory() {
            @Override
            public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                if (type == HtmlResponse.class) return new HtmlResponseConverter();
                return null;
            }
        };

        @Override
        public HtmlResponse convert(ResponseBody responseBody) throws IOException {
//            Document document = Jsoup.parse(responseBody.string());
//            Element value = document.select("script").get(1);
//            String content = value.html();
//            return new HtmlResponse(content);
            return new HtmlResponse(responseBody.string());
        }
    }

    public static interface HtmlResponseService{
        @GET
        Call<HtmlResponse> getHtmlResponse(@Url HttpUrl absoluteUrl);
    }
}
