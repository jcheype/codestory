package fr.codestory;

import com.google.common.base.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.jcheype.webServer.*;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: juliencheype
 * Date: 21/4/13
 * Time: 19:24
 * To change this template use File | Settings | File Templates.
 */
public class App {
    private final Cache<String, List> msgCache = CacheBuilder.newBuilder()
            .maximumSize(100000)
            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build(CacheLoader.from(new Supplier<List>() {
                public List get() {
                    return new ArrayList();
                }
            }));

    private final Cache<String, Response> ctxCache = CacheBuilder.newBuilder()
            .maximumSize(100000)
            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build(CacheLoader.from(new Supplier<Response>() {
                public Response get() {
                    return null;
                }
            }));


    private final ServerBootstrap bootstrap;
    private final SimpleRestHandler restHandler;
    private final HttpApiServerHandler delfaultChannelHandler;
    private final Channel channel;


    public App() {
        bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        restHandler = new SimpleRestHandler();
        delfaultChannelHandler = new HttpApiServerHandler(restHandler);
        bootstrap.setPipelineFactory(new WebServerPipelinefactory(delfaultChannelHandler));
        channel = bootstrap.bind(new InetSocketAddress(19999));
    }

    public void registerRoute() {
        restHandler.get(new Route("/") {
            @Override
            public void handle(Request request, Response response, Map<String, String> map) throws Exception {
                response.write("welcome");
            }
        })
                .get(new Route("/chat/:channel") {
                    @Override
                    public void handle(Request request, Response response, Map<String, String> map) throws Exception {
                        String room = map.get("channel");

                        sendMessage(room, request.parameters);
                        response.write(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
                    }
                })

                .get(new Route("/listen/:channel") {
                    @Override
                    public void handle(Request request, Response response, Map<String, String> map) throws Exception {
                        String room = map.get("channel");
                        List list = msgCache.getUnchecked(room);
                        //noinspection SynchronizationOnLocalVariableOrMethodParameter,SynchronizationOnLocalVariableOrMethodParameter
                        synchronized (list) {
                            if (list.isEmpty()) {
                                ctxCache.asMap().put(room, response);
                            } else {
                                response.write(list.toString());
                                msgCache.invalidate(room);
                            }
                        }
                    }
                });


    }


    public void sendMessage(String uuid, Object message) {
        List list = msgCache.getUnchecked(uuid);
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (list) {
            //noinspection unchecked
            list.add(message);

            ConcurrentMap<String, Response> ctxMap = ctxCache.asMap();
            Response response = ctxMap.remove(uuid);
            if (response != null) {
                response.write(list.toString());
                msgCache.invalidate(uuid);
            }
        }
    }

    public static void main(String[] args) {
        App app = new App();
        app.registerRoute();
    }
}
