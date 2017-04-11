package de.holisticon.jdk9;

import org.apache.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.Jetty;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.util.concurrent.TimeUnit;

/**
 * Hello world!
 *
 * @see <a href="https://webtide.com/the-new-jetty-9-http-client/">the-new-jetty-9-http-client</a>
 * @see <a href="http://git.eclipse.org/c/jetty/org.eclipse.jetty.project.git/tree/jetty-http2/http2-client/src/test/java/org/eclipse/jetty/http2/client/Client.java">Jetty HTTP2Client Example</a>
 * @see <a href="https://blogs.oracle.com/brewing-tests/entry/http_2_with_jetty_server">http_2_with_jetty_server</a>
 */
public class App {

  private static final Logger LOG = Logger.getLogger(App.class);

  public static void main(String[] args) {
    App app = new App();
    String host = "localhost";
    int port = 8443;
    String path = "/greeting?name=JavaLand";
    app.performAsyncHttpRequest(host, port, path);
    app.performDefaultHttpRequest(host, port, path);
    System.exit(0);
  }

  public void performAsyncHttpRequest(String host, int port, String path) {

    LOG.debug("============================================= Asynchronous example ===");
    try {
      HttpClient httpClient = getHttpClient();
      httpClient.start();
      String uri = String.format("https://%s:%s%s", host, port, path);

      Request request =
          httpClient.newRequest(uri)
                    .onResponseContent((response, byteBuffer) -> {
                      LOG.debug("content: " + BufferUtil.toString(byteBuffer));
                    });
      request.send(result -> {
        LOG.debug("http version: " +
            result.getResponse().getVersion());
      });
      
      LOG.debug("request created!!!");
      Thread.sleep(5000);

    } catch (Exception e) {
      LOG.error("Exception:", e);
    }
  }

  public void performDefaultHttpRequest(String host, int port, String path) {

    LOG.debug("============================================= Synchronous example ===");
    try {
      HttpClient httpClient = getHttpClient();
      httpClient.start();
      String uri = String.format("https://%s:%s%s", host, port, path);

      ContentResponse response = httpClient.GET(uri);

      LOG.debug("http version: " + response.getVersion());
      LOG.debug(response.getContentAsString());

    } catch (Exception e) {
      LOG.error("Exception:", e);
    }
  }


  private HttpClient getHttpClient() {
    SslContextFactory sslContextFactory = new SslContextFactory();
    HttpClientTransport transport = new HttpClientTransportOverHTTP2(
        new HTTP2Client());
    HttpClient httpClient = new HttpClient(transport, sslContextFactory);

    // Configure HttpClient, for example:
    httpClient.setFollowRedirects(false);
    return httpClient;
  }


  /**
   * Create the necessary request headers.
   *
   * @param host
   * @param port
   * @param path
   * @param http2Client
   * @return
   */
  private HeadersFrame getRequestHeaders(String host, int port, String path, HTTP2Client http2Client) {
    HttpFields requestFields = new HttpFields();
    requestFields.put("User-Agent", http2Client.getClass().getName() + "/" + Jetty.VERSION);
    MetaData.Request metaData = new MetaData.Request("GET", new HttpURI("https://" + host + ":" + port + path), HttpVersion.HTTP_2, requestFields);
    HeadersFrame headersFrame = new HeadersFrame(metaData, null, true);
    return headersFrame;
  }

}
