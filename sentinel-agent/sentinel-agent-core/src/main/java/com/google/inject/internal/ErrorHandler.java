package com.google.inject.internal;

import com.google.inject.spi.Message;

interface ErrorHandler {
   void handle(Object var1, Errors var2);

   void handle(Message var1);
}
