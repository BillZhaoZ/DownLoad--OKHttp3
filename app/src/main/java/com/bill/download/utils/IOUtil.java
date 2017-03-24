package com.bill.download.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * 关流工具类
 */
public class IOUtil {

    public static void closeAll(Closeable... closeables) {

        if (closeables == null) {
            return;
        }

        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
