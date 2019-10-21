package com.taobao.csp.ahas.gw.io.protocol.util;

import com.taobao.csp.ahas.gw.connection.Connection;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class ConnectionUtil {
   private static final AttributeKey<Connection> CONNECTION_KEY = AttributeKey.valueOf(Connection.class.getName());

   public static Connection connectionOfChannel(Channel channel) {
      return (Connection)channel.attr(CONNECTION_KEY).get();
   }

   public static void bindConnection(Channel channel, Connection connection) {
      channel.attr(CONNECTION_KEY).setIfAbsent(connection);
   }
}
