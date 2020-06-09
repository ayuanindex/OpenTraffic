package com.realmax.cameras.tcputil;

public interface CustomerCallback {
   /* void success(EventLoopGroup eventLoopGroup);*/

    void disConnected();

    /*void sendMessage(ChannelHandlerContext handlerContext);*/

    void getResultData(String msg);
}