package com.taobao.csp.ahas.transport.api;

public interface Decoder<I, O> {
   O decode(I var1) throws DecoderException;
}
