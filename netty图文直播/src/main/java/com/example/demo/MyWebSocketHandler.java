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
 * ����/����/��Ӧ�ͻ���websocket����ĺ���ҵ������
 *
 * @author dajitui
 */

public class MyWebSocketHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketServerHandshaker handshaker;
    private static final String WEB_SOCKET_URL = "ws://localhost:8888/websocket";
    public static ChannelGroup channelGroup;

    //�ͻ��������˴������ӵ�ʱ�����
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //NettyConfig.group.add(ctx.channel());
        System.out.println("��һ���ͻ������������ӿ���...");
    }

    //�ͻ��������˶Ͽ����ӵ�ʱ�����
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
        System.out.println("һ���ͻ������������ӹر�...");
    }

    //����˽��տͻ��˷��͹��������ݽ���֮�����
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    //���̳����쳣��ʱ�����
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    //����˴���ͻ���websocket����ĺ��ķ���
    @Override
    protected void messageReceived(ChannelHandlerContext context, Object msg) throws Exception {
        //����ͻ��������˷���http���������ҵ��
        if (msg instanceof FullHttpRequest) {
            handHttpRequest(context, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) { //����websocket����ҵ��
            handWebsocketFrame(context, (WebSocketFrame) msg);
        }
    }

    /**
     * ����ͻ���������֮ǰ��websocketҵ��
     *
     * @param ctx
     * @param frame
     */
    private void handWebsocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws IOException {
        //�ж��Ƿ��ǹر�websocket��ָ��
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
        }
        //�ж��Ƿ���ping��Ϣ
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        //�ж��Ƿ��Ƕ�������Ϣ������Ƕ�������Ϣ���׳��쳣
        if (!(frame instanceof TextWebSocketFrame)) {
            System.out.println("Ŀǰ���ǲ�֧�ֶ�������Ϣ");
            throw new RuntimeException("��" + this.getClass().getName() + "����֧����Ϣ");
        }
        //����Ӧ����Ϣ
        //��ȡ�ͻ��������˷��͵���Ϣ
        //String request = ((TextWebSocketFrame) frame).text();
        //System.out.println("������յ��ͻ��˵���Ϣ====>>>" + request);
        //TextWebSocketFrame tws = new TextWebSocketFrame(new Date().toString()
        //       + ctx.channel().id()
        //        + " ===>>> "
        //        + request);

        //Ⱥ�����������ÿ�����������Ŀͻ���Ⱥ����Ϣ
        //NettyConfig.group.writeAndFlush(tws);
        String msg = ((TextWebSocketFrame) frame).text();
        Mage mage = new Mage();
        try {
            //�����û����͵���Ϣ
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
            /*System.out.println("���յ���Ϣ�޷�������˽���" + msg);
            ctx.close();
            e.printStackTrace();*/
            byte[] imagedata = DatatypeConverter.parseBase64Binary(msg.substring(msg.indexOf(",") + 1));
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imagedata));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmss");
            String name = formatter.format(new Date()) + "-" + UUID.randomUUID().toString();
            ImageIO.write(bufferedImage, "png", new File("D:\\netty��Ŀ��Ƭ\\" + name + ".png"));
        }
    }

    /**
     * ����ͻ��������˷���http���������ҵ��
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
     * �������ͻ�����Ӧ��Ϣ
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
        //�������ͻ��˷�������
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
