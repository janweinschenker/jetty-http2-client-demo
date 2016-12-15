package de.holisticon.jdk9;

import de.holisticon.jdk9.http2.util.StreamListener;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.api.server.ServerSessionListener;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.util.FuturePromise;
import org.eclipse.jetty.util.Jetty;
import org.eclipse.jetty.util.Promise;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        String host = "nghttp2.org";
        int port = 443;
        String path = "/#/";
        app.performHttpRequest(host, port, path);
    }

    public HTTP2Client performHttpRequest(String host, int port, String path) {
        HTTP2Client http2Client = new HTTP2Client();
        FuturePromise<Session> sessionPromise = new FuturePromise<>();
        SslContextFactory contextFactory = new SslContextFactory(true);
        http2Client.addBean(contextFactory);

        try {
            http2Client.start();
            http2Client.connect(contextFactory, new InetSocketAddress(host, port), new ServerSessionListener.Adapter(), sessionPromise);
            Session session = sessionPromise.get(5, TimeUnit.SECONDS);
            HeadersFrame requestHeaders = getRequestHeaders(host, port, path, http2Client);
            final Phaser phaser = new Phaser(2);
            session.newStream(requestHeaders, new Promise.Adapter<Stream>(), new StreamListener(phaser));

            phaser.awaitAdvanceInterruptibly(phaser.arrive(), 5, TimeUnit.SECONDS);
            http2Client.stop();
        } catch (InterruptedException e) {
            LOG.error("InterruptedException", e);
        } catch (ExecutionException e) {
            LOG.error("ExecutionException", e);
        } catch (TimeoutException e) {
            LOG.error("TimeoutException", e);
        } catch (Exception e) {
            LOG.error("Exception", e);
        }

        return http2Client;
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
