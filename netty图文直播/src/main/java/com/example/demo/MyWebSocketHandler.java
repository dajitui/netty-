package com.example.demo;

import com.example.demo.util.Mage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * 接收/处理/响应客户端websocket请求的核心业务处理类
 *
 * @author dajitui
 */

public class MyWebSocketHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketServerHandshaker handshaker;
    private static final String WEB_SOCKET_URL = "ws://localhost:8888/websocket";
    public static ChannelGroup channelGroup;

    //客户端与服务端创建连接的时候调用
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //NettyConfig.group.add(ctx.channel());
        System.out.println("有一个客户端与服务端连接开启...");
    }

    //客户端与服务端断开连接的时候调用
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //NettyConfig.group.remove(ctx.channel());
        for (String s : NettyConfig.homeMap.keySet()) {
            channelGroup = NettyConfig.homeMap.get(s);
            if (channelGroup.contains(ctx)) {
                channelGroup.remove(ctx);
                break;
            }
        }
        System.out.println("一个客户端与服务端连接关闭...");
    }

    //服务端接收客户端发送过来的数据结束之后调用
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    //工程出现异常的时候调用
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    //服务端处理客户端websocket请求的核心方法
    @Override
    protected void messageReceived(ChannelHandlerContext context, Object msg) throws Exception {
        //处理客户端向服务端发起http握手请求的业务
        if (msg instanceof FullHttpRequest) {
            handHttpRequest(context, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) { //处理websocket连接业务
            handWebsocketFrame(context, (WebSocketFrame) msg);
        }
    }

    /**
     * 处理客户端与服务端之前的websocket业务
     *
     * @param ctx
     * @param frame
     */
    private void handWebsocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws IOException {
        //判断是否是关闭websocket的指令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
        }
        //判断是否是ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        //判断是否是二进制消息，如果是二进制消息，抛出异常
        if (!(frame instanceof TextWebSocketFrame)) {
            System.out.println("目前我们不支持二进制消息");
            throw new RuntimeException("【" + this.getClass().getName() + "】不支持消息");
        }
        //返回应答消息
        //获取客户端向服务端发送的消息
        //String request = ((TextWebSocketFrame) frame).text();
        //System.out.println("服务端收到客户端的消息====>>>" + request);
        //TextWebSocketFrame tws = new TextWebSocketFrame(new Date().toString()
        //       + ctx.channel().id()
        //        + " ===>>> "
        //        + request);

        //群发，服务端向每个连接上来的客户端群发消息
        //NettyConfig.group.writeAndFlush(tws);
        String msg = ((TextWebSocketFrame) frame).text();
        Mage mage = new Mage();
        try {
            //解析用户发送的消息
            mage = mage.strChangeToMage(msg);
            if (mage.isCount()) {
                //concurrentHashMap.put(mage.getName(),ctx);
                if (NettyConfig.homeMap.containsKey(mage.getHomeid())) {
                    channelGroup = NettyConfig.homeMap.get(mage.getHomeid());
                } else {
                    channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
                }
                channelGroup.add(ctx.channel());
                //System.out.println(channelGroup.size());
                NettyConfig.homeMap.put(mage.getHomeid(), channelGroup);
            } else {
                TextWebSocketFrame tws = new TextWebSocketFrame(mage.getName() + " ===>>>" + mage.getMsg());
                channelGroup = NettyConfig.homeMap.get(mage.getHomeid());
                //System.out.println(channelGroup.size());
                channelGroup.writeAndFlush(tws);
            }
        } catch (Exception e) {
            /*System.out.println("接收的信息无法被服务端解析" + msg);
            ctx.close();
            e.printStackTrace();*/
            byte[] imagedata = DatatypeConverter.parseBase64Binary(msg.substring(msg.indexOf(",") + 1));
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imagedata));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmss");
            String name = formatter.format(new Date()) + "-" + UUID.randomUUID().toString();
            ImageIO.write(bufferedImage, "png", new File("D:\\netty项目照片\\" + name + ".png"));
        }
    }

    /**
     * 处理客户端向服务端发起http握手请求的业务
     *
     * @param ctx
     * @param req
     */
    private void handHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        if (!req.getDecoderResult().isSuccess()
                || !("websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, req,
                    new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                WEB_SOCKET_URL, null, true, 6553600);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    /**
     * 服务端向客户端响应消息
     *
     * @param ctx
     * @param req
     * @param res
     */
    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req,
                                  DefaultFullHttpResponse res) {
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        //服务端向客户端发送数据
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
