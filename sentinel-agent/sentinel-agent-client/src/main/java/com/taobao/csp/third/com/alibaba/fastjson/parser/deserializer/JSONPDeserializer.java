package com.taobao.csp.third.com.alibaba.fastjson.parser.deserializer;

import com.taobao.csp.third.com.alibaba.fastjson.JSONException;
import com.taobao.csp.third.com.alibaba.fastjson.JSONPObject;
import com.taobao.csp.third.com.alibaba.fastjson.parser.DefaultJSONParser;
import com.taobao.csp.third.com.alibaba.fastjson.parser.JSONLexerBase;
import com.taobao.csp.third.com.alibaba.fastjson.parser.JSONToken;
import com.taobao.csp.third.com.alibaba.fastjson.parser.SymbolTable;
import com.taobao.csp.third.com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

import java.lang.reflect.Type;

/**
 * Created by wenshao on 21/02/2017.
 */
public class JSONPDeserializer implements ObjectDeserializer {
    public static final com.taobao.csp.third.com.alibaba.fastjson.parser.deserializer.JSONPDeserializer instance = new com.taobao.csp.third.com.alibaba.fastjson.parser.deserializer.JSONPDeserializer();

    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        JSONLexerBase lexer = (JSONLexerBase) parser.getLexer();

        SymbolTable symbolTable = parser.getSymbolTable();

        String funcName = lexer.scanSymbolUnQuoted(symbolTable);
        lexer.nextToken();

        int tok = lexer.token();

        if (tok == JSONToken.DOT) {
            String name = lexer.scanSymbolUnQuoted(parser.getSymbolTable());
            funcName += ".";
            funcName += name;
            lexer.nextToken();
            tok = lexer.token();
        }

        JSONPObject jsonp = new JSONPObject(funcName);

        if (tok != JSONToken.LPAREN) {
            throw new JSONException("illegal jsonp : " + lexer.info());
        }
        lexer.nextToken();
        for (;;) {
            Object arg = parser.parse();
            jsonp.addParameter(arg);

            tok = lexer.token();
            if (tok == JSONToken.COMMA) {
                lexer.nextToken();
            } else if (tok == JSONToken.RPAREN) {
                lexer.nextToken();
                break;
            } else {
                throw new JSONException("illegal jsonp : " + lexer.info());
            }
         }
        tok = lexer.token();
        if (tok == JSONToken.SEMI) {
            lexer.nextToken();
        }

        return (T) jsonp;
    }

    public int getFastMatchToken() {
        return 0;
    }
}
