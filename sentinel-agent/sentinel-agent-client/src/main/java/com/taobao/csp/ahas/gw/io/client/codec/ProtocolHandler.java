package com.taobao.csp.ahas.gw.io.client.codec;

import com.taobao.csp.ahas.gw.compress.CompressUtil;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessage;
import com.taobao.csp.ahas.gw.io.protocol.AgwMessageHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.nio.charset.Charset;
import java.util.List;

public class ProtocolHandler extends ByteToMessageCodec<AgwMessage> {
   public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

   protected void encode(ChannelHandlerContext ctx, AgwMessage msg, ByteBuf out) throws Exception {
      AgwMessageHeader header = msg.getHeader();
      String bodyStr = msg.getBody();
      if (header != null) {
         if (bodyStr != null) {
            byte[] body = null;
            int compress = header.getVersion() & 7;
         //   byte[] body;
            if (compress == 2) {
               body = CompressUtil.compress(bodyStr);
            } else if (compress == 3 && header.getMessageDirection() == 1) {
               body = CompressUtil.compress(bodyStr);
            } else if (compress == 4 && header.getMessageDirection() == 2) {
               body = CompressUtil.compress(bodyStr);
            } else {
               body = bodyStr.getBytes(CHARSET_UTF8);
            }

            out.writeInt(body.length);
            out.writeLong(header.getReqId());
            out.writeByte(header.getMessageType());
            out.writeByte(header.getMessageDirection());
            out.writeByte(header.getCaller());
            out.writeLong(header.getClientIp());
            out.writeInt(header.getClientVpcId().getBytes(CHARSET_UTF8).length);
            out.writeBytes(header.getClientVpcId().getBytes(CHARSET_UTF8));
            out.writeInt(header.getServerName().getBytes(CHARSET_UTF8).length);
            out.writeBytes(header.getServerName().getBytes(CHARSET_UTF8));
            out.writeInt(header.getTimeoutMs());
            out.writeInt(header.getClientProcessFlag().getBytes(CHARSET_UTF8).length);
            out.writeBytes(header.getClientProcessFlag().getBytes(CHARSET_UTF8));
            out.writeInt(header.getInnerCode());
            out.writeInt(header.getInnerMsg().getBytes(CHARSET_UTF8).length);
            out.writeBytes(header.getInnerMsg().getBytes(CHARSET_UTF8));
            out.writeInt(header.getConnectionId());
            out.writeInt(header.getHandlerName().getBytes(CHARSET_UTF8).length);
            out.writeBytes(header.getHandlerName().getBytes(CHARSET_UTF8));
            out.writeInt(header.getOuterReqId().getBytes(CHARSET_UTF8).length);
            out.writeBytes(header.getOuterReqId().getBytes(CHARSET_UTF8));
            out.writeInt(header.getVersion());
            out.writeBytes(body);
         }
      }
   }

   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
      int originPos = in.readerIndex();
      if (in.readableBytes() >= 27) {
         int bodyLength = in.readInt();
         long reqId = in.readLong();
         byte messageType = in.readByte();
         byte messageDirection = in.readByte();
         byte caller = in.readByte();
         long userIp = in.readLong();
         int userVpcIdLength = in.readInt();
         if (in.readableBytes() >= userVpcIdLength) {
            byte[] userVpcId = new byte[userVpcIdLength];
            in.readBytes(userVpcId);
            if (in.readableBytes() >= 4) {
               int appNameLength = in.readInt();
               if (in.readableBytes() >= appNameLength) {
                  byte[] appName = new byte[appNameLength];
                  in.readBytes(appName);
                  if (in.readableBytes() >= 4) {
                     int timeoutMs = in.readInt();
                     if (in.readableBytes() >= 4) {
                        int userProcessFlagLength = in.readInt();
                        if (in.readableBytes() >= userProcessFlagLength) {
                           byte[] userProcessFlag = new byte[userProcessFlagLength];
                           in.readBytes(userProcessFlag);
                           if (in.readableBytes() >= 4) {
                              int innerCode = in.readInt();
                              if (in.readableBytes() >= 4) {
                                 int innerMsgLength = in.readInt();
                                 if (in.readableBytes() >= innerMsgLength) {
                                    byte[] innerMsg = new byte[innerMsgLength];
                                    in.readBytes(innerMsg);
                                    if (in.readableBytes() >= 4) {
                                       int connectionId = in.readInt();
                                       if (in.readableBytes() >= 4) {
                                          int handlerNameLength = in.readInt();
                                          if (in.readableBytes() >= handlerNameLength) {
                                             byte[] handlerName = new byte[handlerNameLength];
                                             in.readBytes(handlerName);
                                             if (in.readableBytes() >= 4) {
                                                int outerReqIdLength = in.readInt();
                                                if (in.readableBytes() >= outerReqIdLength) {
                                                   byte[] outerReqId = new byte[outerReqIdLength];
                                                   in.readBytes(outerReqId);
                                                   if (in.readableBytes() >= 4) {
                                                      int version = in.readInt();
                                                      if (in.readableBytes() >= bodyLength) {
                                                         byte[] body = new byte[bodyLength];
                                                         in.readBytes(body);
                                                         AgwMessageHeader header = new AgwMessageHeader();
                                                         header.setBodyLength(bodyLength);
                                                         header.setReqId(reqId);
                                                         header.setMessageType(messageType);
                                                         header.setMessageDirection(messageDirection);
                                                         header.setCaller(caller);
                                                         header.setClientIp(userIp);
                                                         header.setClientVpcIdLength(userVpcIdLength);
                                                         header.setClientVpcId(new String(userVpcId, CHARSET_UTF8));
                                                         header.setServerNameLength(appNameLength);
                                                         header.setServerName(new String(appName, CHARSET_UTF8));
                                                         header.setTimeoutMs(timeoutMs);
                                                         header.setClientProcessFlagLength(userProcessFlagLength);
                                                         header.setClientProcessFlag(new String(userProcessFlag, CHARSET_UTF8));
                                                         header.setInnerCode(innerCode);
                                                         header.setInnerMsgLength(innerMsgLength);
                                                         header.setInnerMsg(new String(innerMsg, CHARSET_UTF8));
                                                         header.setConnectionId(connectionId);
                                                         header.setHandlerName(new String(handlerName, CHARSET_UTF8));
                                                         header.setOuterReqId(new String(outerReqId, CHARSET_UTF8));
                                                         header.setVersion(version);
                                                         AgwMessage message = new AgwMessage();
                                                         message.setHeader(header);
                                                         int compress = header.getVersion() & 7;
                                                         if (compress == 2) {
                                                            message.setBody(CompressUtil.uncompress(body));
                                                         } else if (compress == 3 && header.getMessageDirection() == 1) {
                                                            message.setBody(CompressUtil.uncompress(body));
                                                         } else if (compress == 4 && header.getMessageDirection() == 2) {
                                                            message.setBody(CompressUtil.uncompress(body));
                                                         } else {
                                                            message.setBody(new String(body, CHARSET_UTF8));
                                                         }

                                                         if (header.getMessageDirection() == 1) {
                                                            message.init(message.getBody().getBytes("UTF-8").length, body.length, compress);
                                                         }

                                                         out.add(message);
                                                         return;
                                                      }
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      in.setIndex(originPos, in.writerIndex());
   }
}
