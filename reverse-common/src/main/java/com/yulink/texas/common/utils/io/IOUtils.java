package com.yulink.texas.common.utils.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

/**
 * Need class description here...
 *
 * @Author: liupanpan
 * @Date: 2019/3/27
 * @Copyright (c) 2013, yulink.io All Rights Reserved
 */

@Slf4j
public class IOUtils {
    public static void write(String filename, String content) {
        try {
            FileOutputStream bos = new FileOutputStream(filename);
            OutputStreamWriter osw = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
            osw.write(content);
            osw.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createDirectories(String pathStr) {
        Path path = Paths.get(pathStr);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                log.error("{}|create director error:", "EmailNotification", e);
            }
        }
    }

}
