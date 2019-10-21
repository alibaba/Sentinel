package com.alibaba.acm.shaded.org.codehaus.jackson.map.type;

import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.util.ClassUtil;

/**
 * Simple recursive-descent parser for parsing canonical {@link JavaType}
 * representations and constructing type instances.
 * 
 * @author tatu
 * @since 1.5
 */
public class TypeParser
{
    final TypeFactory _factory;
        
    public TypeParser(TypeFactory f) {
        _factory = f;
    }

    public JavaType parse(String canonical)
        throws IllegalArgumentException
    {
        canonical = canonical.trim();
        MyTokenizer tokens = new MyTokenizer(canonical);
        JavaType type = parseType(tokens);
        // must be end, now
        if (tokens.hasMoreTokens()) {
            throw _problem(tokens, "Unexpected tokens after complete type");
        }
        return type;
    }

    protected JavaType parseType(MyTokenizer tokens)
        throws IllegalArgumentException
    {
        if (!tokens.hasMoreTokens()) {
            throw _problem(tokens, "Unexpected end-of-string");
        }
        Class<?> base = findClass(tokens.nextToken(), tokens);
        // either end (ok, non generic type), or generics
        if (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if ("<".equals(token)) {
                return _factory._fromParameterizedClass(base, parseTypes(tokens));
            }
            // can be comma that separates types, or closing '>'
            tokens.pushBack(token);
        }
        return _factory._fromClass(base, null);
    }

    protected List<JavaType> parseTypes(MyTokenizer tokens)
        throws IllegalArgumentException
    {
        ArrayList<JavaType> types = new ArrayList<JavaType>();
        while (tokens.hasMoreTokens()) {
            types.add(parseType(tokens));
            if (!tokens.hasMoreTokens()) break;
            String token = tokens.nextToken();
            if (">".equals(token)) return types;
            if (!",".equals(token)) {
                throw _problem(tokens, "Unexpected token '"+token+"', expected ',' or '>')");
            }
        }
        throw _problem(tokens, "Unexpected end-of-string");
    }

    protected Class<?> findClass(String className, MyTokenizer tokens)
    {
        try {
	    return ClassUtil.findClass(className);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw _problem(tokens, "Can not locate class '"+className+"', problem: "+e.getMessage());
        }
    }

    protected IllegalArgumentException _problem(MyTokenizer tokens, String msg)
    {
        return new IllegalArgumentException("Failed to parse type '"+tokens.getAllInput()
                +"' (remaining: '"+tokens.getRemainingInput()+"'): "+msg);
    }

    final static class MyTokenizer
        extends StringTokenizer
    {
        protected final String _input;

        protected int _index;

        protected String _pushbackToken;
        
        public MyTokenizer(String str) {            
            super(str, "<,>", true);
            _input = str;
        }

        @Override
        public boolean hasMoreTokens() {
            return (_pushbackToken != null) || super.hasMoreTokens();
        }
        
        @Override
        public String nextToken() {
            String token;
            if (_pushbackToken != null) {
                token = _pushbackToken;
                _pushbackToken = null;
            } else {
                token = super.nextToken();
            }
            _index += token.length();
            return token;
        }

        public void pushBack(String token) {
            _pushbackToken = token;
            _index -= token.length();
        }
        
        public String getAllInput() { return _input; }
        public String getUsedInput() { return _input.substring(0, _index); }
        public String getRemainingInput() { return _input.substring(_index); }
    }
}
