package com.taobao.middleware.logger.option;

import com.taobao.middleware.logger.Level;
import com.taobao.middleware.logger.support.LogLog;
import org.apache.log4j.AsyncAppender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by zhuyong on 15/3/3.
 */
public abstract class AbstractActiveOption implements ActivateOption {

    protected String productName;
    protected Level level;

    @Override
    public String getProductName() {
        return productName;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    protected void setProductName(String productName) {
        if (this.productName == null && productName != null) {
            this.productName = productName;
        }
    }

    public static void invokeMethod(Object object, List<Object[]> args) {
        if (args != null && object != null) {
            for (Object[] arg : args) {
                if (arg != null && arg.length == 3) {
                    try {
                        Method m = object.getClass().getMethod((String) arg[0], (Class<?>[]) arg[1]);
                        m.invoke(object, arg[2]);
                    } catch (NoSuchMethodException e) {
                        LogLog.info("Can't find method for " + object.getClass() + " " + arg[0] + " " + arg[2]);
                    } catch (IllegalAccessException e) {
                        LogLog.info("Can't invoke method for " + object.getClass() + " " + arg[0] + " " + arg[2]);
                    } catch (InvocationTargetException e) {
                        LogLog.info("Can't invoke method for " + object.getClass() + " " + arg[0] + " " + arg[2]);
                    } catch (Throwable t) {
                        LogLog.info("Can't invoke method for " + object.getClass() + " " + arg[0] + " " + arg[2]);
                    }
                }
            }
        }
    }
}
