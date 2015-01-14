package tests;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Environment;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.github.axet.vget.VGet;

public class StreamTests extends InstrumentationTestCase {

    // public static final String URL_VIDEO =
    // "https://youtube.com/v/h2p20wDbr_Y&fs=1&autoplay=1&playerMode=normal&rel=0";
    public static final String URL_VIDEO = "https://www.youtube.com/watch?v=LPtXpTB5uao";

    public void testDownloadYoutube() throws Exception {
        File dest = new File(Environment.getExternalStorageDirectory(), "temp.mp4");
        VGet v = new VGet(new URL(URL_VIDEO), dest);
        v.download(new AtomicBoolean(), new Runnable() {
            
            @Override
            public void run() {
                Log.d("test", "run called");
            }
        });

        Log.d("test", "video downloaded");
    }

    public void testDownloadStream() throws Exception {
        // HttpClientBuilder builder = HttpClients.custom();
        // BasicLineFormatter lineformatter = new BasicLineFormatter();
        // DefaultHttpRequestWriterFactory requestWriterFactory = new
        // DefaultHttpRequestWriterFactory();
        // DefaultHttpResponseParserFactory responseParserFactory = new
        // DefaultHttpResponseParserFactory();
        //
        // PoolingHttpClientConnectionManager cm = new
        // PoolingHttpClientConnectionManager(
        // new ManagedHttpClientConnectionFactory(requestWriterFactory,
        // responseParserFactory));
        // builder.setConnectionManager(cm);
        // CloseableHttpClient httpclient = builder.build();

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(URL_VIDEO);
        // CloseableHttpResponse response = httpclient.execute(httpget);
        HttpResponse response = httpclient.execute(httpget);

        try {
            InputStream stream = response.getEntity().getContent();
            do {
                byte[] buffer = new byte[16 * 1024];
                int got = stream.read(buffer);
                if (got == -1) {
                    break;
                }

                Log.d("test", "got=" + new String(buffer, 0, got));
            } while (true);
        } finally {
            // response.close();
            HttpClientUtils.closeQuietly(httpclient);
        }
    }
}
