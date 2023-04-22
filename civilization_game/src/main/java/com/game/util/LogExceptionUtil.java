package com.game.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogExceptionUtil {
    /**
     *
     * @功能说明:在日志文件中，打印异常堆栈
     * @param e
     * @return:String
     */
    public static String Log (Throwable e) {
        StringWriter errorsWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(errorsWriter));
        return errorsWriter.toString();
    }
}