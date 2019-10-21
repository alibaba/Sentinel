package com.taobao.csp.ahas.gw.upstream;

public enum RpcResultCodeEnum {
   RPC_OK(0, "ok"),
   RPC_TIMEOUT(8001, "rpc timeout while waiting the result of request through netty"),
   RPC_CLIENT_VPCID_EMPTY_EXCEPTION(8002, "clientVpcId can not be blank"),
   RPC_GW_HSF_CONSUMER_INIT_WRONG(8003, "the initialization of hsf consumer holding by gateway is wrong"),
   RPC_CLIENT_IP_FORMAT_WRONG(8004, "illegal format error of clientIp"),
   RPC_SERVER_NAME_NULL_EXCEPTION(8005, "serverName can not be blank"),
   RPC_WRITE_INTERRUPTED_EXCEPTION(8006, "if the current thread was interrupted while waiting the response of request"),
   RPC_WRITE_EXECUTION_EXCEPTION(8007, "if the computation threw an exception while waiting the response of request"),
   RPC_CLOSE_EXCEPTION(8008, "the connection of the calling from gateway to client is closed"),
   RPC_BLANK_INTELNAL_REQ_MESSAGE(8009, "internal message can not be blank"),
   RPC_BLANK_INTELNAL_REQ_HEADER(8010, "internal message header can not be blank"),
   RPC_CLIENT_CLOSE_EXCEPTION(8011, "the connection of the calling from client to gateway is closed"),
   RPC_MESSAGE_BUILD_WRONG(8012, "something is wrong while building the agw request in gateway, may be lessing of parameters"),
   RPC_SERVER_HANDLER_NOT_FOUND(8013, "can not get handler by handlerName in server"),
   RPC_SERVER_HANDLER_EXECUTE_WRONG(8014, "an error happens in server while handling the request from client"),
   RPC_APP_TO_GW_CONSUMER_LOST(8015, "the initialization of hsf consumer holding by server is wrong"),
   RPC_APP_TO_GW_CONSUMER_HSF_EXCEPTION(8016, "an hsf exception happens while server's calling gateway"),
   RPC_APP_TO_GW_RESULT_EMPTY_EXCEPTION(8017, "the result in server of the calling from server to client is null"),
   RPC_CLIENT_PROCESS_FLAG_EMPTY_EXCEPTION(8018, "clientProcessFlag can not be blank"),
   RPC_HANDLER_NAME_NULL_EXCEPTION(8019, "handlerName can not be blank"),
   RPC_RPC_METADATA_NULL_EXCEPTION(8020, "rpc metadata can not be blank"),
   RPC_REQUEST_BODY_NULL_EXCEPTION(8021, "request body can not be blank"),
   RPC_GW_CONNECTION_LOST(8022, "gateway lost the connection while calling to client"),
   RPC_BUILD_MESSAGE_WRONG(8023, "something is wrong while building the agw request in server or client, may be lessing of parameters"),
   RPC_USER_TO_GW_RESULT_EMPTY_EXCEPTION(8024, "the result in client of the calling from client to server is null"),
   RPC_APP_TO_GW_MESSAGE_NULL_EXCEPTION(8025, "the agw request message in server is blank"),
   RPC_CAN_NOT_FIND_GW_IP_BY_CONNECTION(8026, "server call client through gateway fails because of not finding the connection"),
   RPC_CLIENT_UNINIT_EXCEPTION(8027, "please init the client firest"),
   RPC_CONNECTION_LOST_IN_REDIS(8028, "can not find the gateway holding the connection that the server want to call"),
   RPC_NO_GATEWAY_EXCEPTION(8029, "server can not find the provider of gateway"),
   RPC_CAN_NOT_FIND_CONNECTION_AFTER_CALCULATING(8030, "the gateway holding the connection that the server want to call is not a hsf provider"),
   RPC_CONFIG_NULL_EXCEPTION(8031, "config needed by agw client can not be null"),
   RPC_HANDLER_NULL_EXCEPTION(8032, "handler can not be null while registering handler"),
   RPC_HANDLER_RETURN_NULL_EXCEPTION(8033, "the response from server is null"),
   RPC_CLIENT_HANDLER_NOT_FOUND(8034, "can not get handler by handlerName in client"),
   RPC_CLIENT_HANDLER_EXECUTE_WRONG(8035, "the handler in client throw an exception"),
   RPC_GW_HSF_GET_CONSUMER_FAIL(8036, "gateway get hsf consumer bean fail"),
   RPC_GW_HSF_CALL_EXCEPTION(8037, "an exception happens while gateway's calling server through hsf"),
   RPC_USER_CONNECTION_LOST(8038, "client can not connect to gateway"),
   RPC_HSF_TIMEOUT(8039, "an hsf timeout exception happens while gateway's calling server or server's calling gateway"),
   RPC_CALLBACK_NULL_EXCEPTION(8040, "the callback within server can not be null"),
   RPC_UNSUPPORT_OPERATION(8041, "unsupport operation"),
   RPC_OUTER_REQ_ID_BLANK_EXCEPTION(8042, "traceId can not be blank"),
   RPC_GW_TO_SERVER_CONSUMER_HSF_EXCEPTION(8043, "an hsf exception happens while gateway's calling server"),
   RPC_GW_THREAD_POLL_FULL_EXCEPTION(8044, "thread poll used by gateway for handling the request from client is full"),
   RPC_GW_NETTY_WRITE_EXCEPTION(8045, "an exceptions happen while writing through netty"),
   RPC_CERTIFICATE_EXCEPTION(8046, "certificate exception"),
   RPC_INTERNAL_ERROR(9998, "internal error"),
   RPC_UNKNOWN_ERROR(9999, "unknown error");

   private int code;
   private String message;

   private RpcResultCodeEnum(int errorCode, String msg) {
      this.code = errorCode;
      this.message = msg;
   }

   public int getCode() {
      return this.code;
   }

   public String getMessage() {
      return this.message;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder(128);
      sb.append("code : ").append(this.code).append(" , message : ").append(this.message);
      return sb.toString();
   }
}
