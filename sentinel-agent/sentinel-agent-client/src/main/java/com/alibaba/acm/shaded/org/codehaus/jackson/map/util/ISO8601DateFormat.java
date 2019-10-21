package com.alibaba.acm.shaded.org.codehaus.jackson.map.util;

import java.text.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Provide a fast thread-safe formatter/parser DateFormat for ISO8601 dates ONLY.
 * It was mainly done to be used with Jackson JSON Processor.
 * <p/>
 * Watch out for clone implementation that returns itself.
 * <p/>
 * All other methods but parse and format and clone are undefined behavior.
 *
 * @see ISO8601Utils
 */
public class ISO8601DateFormat extends DateFormat
{
    private static final long serialVersionUID = 1L;

    // those classes are to try to allow a consistent behavior for hascode/equals and other methods
    private static Calendar CALENDAR = new GregorianCalendar();
    private static NumberFormat NUMBER_FORMAT = new DecimalFormat();

    public ISO8601DateFormat() {
        this.numberFormat = NUMBER_FORMAT;
        this.calendar = CALENDAR;
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition)
    {
        String value = ISO8601Utils.format(date);
        toAppendTo.append(value);
        return toAppendTo;
    }

    @Override
    public Date parse(String source, ParsePosition pos)
    {
        // index must be set to other than 0, I would swear this requirement is not there in
        // some version of jdk 6.
        pos.setIndex(source.length());
        return ISO8601Utils.parse(source);
    }

    @Override
    public Object clone() {
        return this;    // jackson calls clone everytime. We are threadsafe so just returns the instance
    }
}