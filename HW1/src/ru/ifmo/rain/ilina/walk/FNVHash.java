package ru.ifmo.rain.ilina.walk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

class FNVHash {
    //private static final int HVAL = 0x811c9dc5;
    private static final int HVAL = 0x811c9dc5;
    private static final int PRIME = 0x01000193;
    private static final int MAKE_UNSIGNED = 0xff;

    static int getFNVByBytes(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            int res = HVAL;
            int c;
            while ((c = is.read()) >= 0) {
                res *= PRIME;
                res ^= c & MAKE_UNSIGNED;
            }
            return res;
        }
    }

    static int getFNVByBlocks(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            byte[] b = new byte[1024];
            int res = HVAL;
            int c;
            while ((c = is.read(b)) >= 0) {
                for (int i = 0; i < c; i++) {
                    res *= PRIME;
                    res ^= b[i] & MAKE_UNSIGNED;
                }
            }
            return res;
        }
    }
}