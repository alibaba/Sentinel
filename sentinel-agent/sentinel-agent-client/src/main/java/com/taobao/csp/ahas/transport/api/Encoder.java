package com.taobao.csp.ahas.transport.api;

public interface Encoder<I, O> {
   O encode(I var1) throws EncoderException;
}
