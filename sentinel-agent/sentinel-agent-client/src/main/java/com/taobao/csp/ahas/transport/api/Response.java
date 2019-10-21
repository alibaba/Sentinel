package com.taobao.csp.ahas.transport.api;

public class Response<R> {
   private String requestId;
   private int code;
   private boolean success;
   private R result;
   private String error;

   public Response() {
   }

   public Response(String requestId, int code, boolean success, R result, String error) {
      this.requestId = requestId;
      this.code = code;
      this.success = success;
      this.result = result;
      this.error = error;
   }

   private Response(Code code, boolean success, String error, R result) {
      this.code = code.getCode();
      this.success = success;
      this.result = result;
      this.error = error;
   }

   public static <T> Response<T> ofSuccess(T result) {
      return new Response(Code.OK, true, (String)null, result);
   }

   public static <T> Response<T> ofFailure(Code code, String error) {
      return new Response(code, false, error, (Object)null);
   }

   public static <T> Response<T> ofFailure(Code code, String error, T result) {
      return new Response(code, false, error, result);
   }

   public String getRequestId() {
      return this.requestId;
   }

   public void setRequestId(String requestId) {
      this.requestId = requestId;
   }

   public boolean isSuccess() {
      return this.success;
   }

   public void setSuccess(boolean success) {
      this.success = success;
   }

   public R getResult() {
      return this.result;
   }

   public void setResult(R result) {
      this.result = result;
   }

   public String getError() {
      return this.error;
   }

   public void setError(String error) {
      this.error = error;
   }

   public int getCode() {
      return this.code;
   }

   public void setCode(int code) {
      this.code = code;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder("Response{");
      sb.append("requestId='").append(this.requestId).append('\'');
      sb.append(", code=").append(this.code);
      sb.append(", success=").append(this.success);
      sb.append(", result=").append(this.result);
      sb.append(", error='").append(this.error).append('\'');
      sb.append('}');
      return sb.toString();
   }

   public static enum Code {
      OK(200, "success"),
      INVALID_TIMESTAMP(401, "invalid timestamp"),
      FORBIDDEN(403, "forbidden"),
      NOT_FOUND(404, "request handler not found"),
      Token_Not_Found(405, "access token not found"),
      AHAS_SERVICE_NOT_OPENED(410, "ahas service not opened"),
      AHAS_SERVICE_NOT_AUTHORIZED(411, "ahas service not authorized"),
      SERVER_ERROR(500, "server error"),
      Handler_Closed(501, "handler closed"),
      TIMEOUT(510, "timeout"),
      UNINITIALIZED(511, "uninitialized"),
      ENCODE_ERROR(512, "encode error"),
      DECODE_ERROR(513, "decode error"),
      File_Not_Found(514, "file not found"),
      Download_Error(515, "download file error"),
      Deploy_Error(516, "deploy file error"),
      Service_Switch_Error(517, "service switch error"),
      Parameter_Empty(600, "parameter is empty"),
      Parameter_Type_Error(601, "parameter type error"),
      FaultInject_Cmd_Error(701, "cannot handle the faultInject cmd"),
      FaultInject_Execute_Error(702, "execute faultInject error"),
      FaultInject_Not_Support(703, "the inject type not support"),
      JavaAgent_Cmd_Error(704, "cannot handle the javaagent cmd");

      private int code;
      private String msg;

      private Code(int code, String msg) {
         this.code = code;
         this.msg = msg;
      }

      public int getCode() {
         return this.code;
      }

      public String getMsg() {
         return this.msg;
      }
   }
}
