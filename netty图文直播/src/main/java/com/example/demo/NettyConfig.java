package com.example.demo;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储整个工程的全局配置
 * @author dajitui
 *
 */
public class NettyConfig {
	
	/**
	 * 存储每一个客户端接入进来时的channel对象
	 */
	public static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	/**
	 * 储存所有用户姓名对应的channel对象
	 */
	public static ConcurrentHashMap<String, ChannelHandlerContext> map=new ConcurrentHashMap<>();

	/**
	 * 儲存直播间对应channel对象
	 */
	public static ConcurrentHashMap<String, ChannelGroup> homeMap=new ConcurrentHashMap<>();


}
