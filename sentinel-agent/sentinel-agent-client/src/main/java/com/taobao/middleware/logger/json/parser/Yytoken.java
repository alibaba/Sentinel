/*
 * $Id: Yytoken.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-15
 */
package com.taobao.middleware.logger.json.parser;

/**
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class Yytoken {
	public static final int TYPE_VALUE=0;//JSON primitive value: string,number,boolean,null
	public static final int TYPE_LEFT_BRACE=1;
	public static final int TYPE_RIGHT_BRACE=2;
	public static final int TYPE_LEFT_SQUARE=3;
	public static final int TYPE_RIGHT_SQUARE=4;
	public static final int TYPE_COMMA=5;
	public static final int TYPE_COLON=6;
	public static final int TYPE_EOF=-1;//end of file
	
	public int type=0;
	public Object value=null;
	
	public Yytoken(int type,Object value){
		this.type=type;
		this.value=value;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		switch(type){
		case TYPE_VALUE:
			sb.append("VALUE(").append(value).append(")");
			break;
		case TYPE_LEFT_BRACE:
			sb.append("LEFT BRACE({)");
			break;
		case TYPE_RIGHT_BRACE:
			sb.append("RIGHT BRACE(})");
			break;
		case TYPE_LEFT_SQUARE:
			sb.append("LEFT SQUARE([)");
			break;
		case TYPE_RIGHT_SQUARE:
			sb.append("RIGHT SQUARE(])");
			break;
		case TYPE_COMMA:
			sb.append("COMMA(,)");
			break;
		case TYPE_COLON:
			sb.append("COLON(:)");
			break;
		case TYPE_EOF:
			sb.append("END OF FILE");
			break;
		}
		return sb.toString();
	}
}
