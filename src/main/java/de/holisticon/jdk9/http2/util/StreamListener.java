package de.holisticon.jdk9.http2.util;

import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.api.Stream.Listener.Adapter;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.http2.frames.PushPromiseFrame;
import org.eclipse.jetty.util.Callback;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.concurrent.Phaser;

/**
 * Created by janweinschenker on 14.12.16.
 */
public class StreamListener extends Adapter {

    private Phaser phaser;

    public StreamListener(Phaser phaser) {
        this.phaser = phaser;
    }

    @Override
    public Stream.Listener onPush(Stream stream, PushPromiseFrame frame) {
        System.err.println("onPush: " + frame);

        phaser.register();
        return this;
    }

    @Override
    public void onHeaders(Stream stream, HeadersFrame frame) {
        System.err.println("onHeaders: " + frame);
        if (frame.isEndStream()) {
            phaser.arrive();
        }
    }

    @Override
    public void onData(Stream stream, DataFrame frame, Callback callback) {
        System.err.println("onData: " + new String(frame.getData().array(), Charset.forName("UTF8")));
        frame.getData().get();
        callback.succeeded();
        if (frame.isEndStream()) {
            phaser.arrive();
        }
    }
}
