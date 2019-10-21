package com.taobao.diamond.client;

import java.util.ArrayList;
import java.util.List;



public class BatchHttpResult<T> {

    // �����Ƿ�ɹ�
    private boolean success = true;
    // ���󷵻ص�״̬��
    private int statusCode;
    // �û��ɶ��ķ�����Ϣ
    private String statusMsg;
    // response�е�Ԫ��Ϣ
    private String responseMsg;
    // ���صĽ��
    private List<T> result;

    public BatchHttpResult() {
        this.result = new ArrayList<T>();
    }

    public BatchHttpResult(boolean success, int statusCode, String statusMsg, String responseMsg) {
        this.result = new ArrayList<T>();
        this.success = success;
        this.statusCode = statusCode;
        this.statusMsg = statusMsg;
        this.responseMsg = responseMsg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public List<T> getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "BatchHttpResult [success=" + success + ", statusCode=" + statusCode
                + ", statusMsg=" + statusMsg + ", result=" + result + "]";
    }

}
