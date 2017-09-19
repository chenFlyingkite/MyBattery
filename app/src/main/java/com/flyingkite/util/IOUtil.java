package com.flyingkite.util;

import java.io.Closeable;
import java.io.IOException;

public class IOUtil {
    public static void closeIt(Closeable... cs) {
        if (cs == null) return;

        for (Closeable c : cs) {
            if (c == null) continue;

            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
