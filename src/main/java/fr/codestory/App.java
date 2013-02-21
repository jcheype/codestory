package fr.codestory;

import com.jcheype.webServer.*;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: juliencheype
 * Date: 21/4/13
 * Time: 19:24
 * To change this template use File | Settings | File Templates.
 */
public class App {
    private final ServerBootstrap bootstrap;
    private final SimpleRestHandler restHandler;
    private final HttpApiServerHandler delfaultChannelHandler;
    private final Channel channel;

    public App(){
        bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        restHandler = new SimpleRestHandler();
        delfaultChannelHandler = new HttpApiServerHandler(restHandler);
        bootstrap.setPipelineFactory(new WebServerPipelinefactory(delfaultChannelHandler));
        channel = bootstrap.bind(new InetSocketAddress(9999));
    }

    public void registerRoute(){
        restHandler.get(new Route("/"){
            @Override
            public void handle(Request request, Response response, Map<String, String> map) throws Exception {
                response.write("welcome");
            }
        })
        .get(new Route("/chat/:channel"){
            @Override
            public void handle(Request request, Response response, Map<String, String> map) throws Exception {
                response.write(map.get("channel"));
            }
        })
        .get(new Route("/chat/:channel"){
            @Override
            public void handle(Request request, Response response, Map<String, String> map) throws Exception {

            }
        });
    }




    public static void main(String[] args) {
        App app = new App();
        app.registerRoute();
    }
}
