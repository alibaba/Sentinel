package com.taobao.middleware.logger.util;

public class MessageUtil {

    public static String formatMessage(String format, Object[] argArray) {
        FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
        return ft.getMessage();
    }

    public static String getMessage(String message) {
        return getMessage(null, message);
    }

    public static String getMessage(String context, String message) {
        return getMessage(context, null, message);
    }

    public static String getMessage(String context, String errorCode, String message) {
        if (context == null) {
            context = "";
        }

        if (errorCode == null) {
            errorCode = "";
        }
        return "[" + context + "] [] [" + errorCode + "] " + message;
    }
}
