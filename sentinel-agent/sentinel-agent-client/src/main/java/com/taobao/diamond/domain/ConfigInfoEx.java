package com.taobao.diamond.domain;

/**
 * ConfigInfo的扩展类, 用于批量处理
 * 
 * @author leiwen.zh
 * 
 */
public class ConfigInfoEx extends ConfigInfo {

    private static final long serialVersionUID = -1L;

    // 批量查询时, 单条数据的状态码, 具体的状态码在Constants.java中
    private int status;
    // 批量查询时, 单条数据的信息
    private String message;

    public ConfigInfoEx() {
        super();
    }

    public ConfigInfoEx(String dataId, String group, String content) {
        super(dataId, group, content);
    }

    public ConfigInfoEx(String dataId, String group, String content, int status, String message){
        super(dataId, group, content);
        this.status = status;
        this.message = message;
    }


    public int getStatus() {
        return status;
    }


    public void setStatus(int status) {
        this.status = status;
    }


    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }


	@Override
	public String toString() {
		return "ConfigInfoEx [status=" + status + ", message=" + message
				+ ", dataId=" + getDataId() + ", group=" + getGroup()
				+ ", appName=" + getAppName() + ", content=" + getContent()
				+ "]";
	}

}
