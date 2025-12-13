package com.yulink.texas.common.utils.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Need class description here...
 *
 * @Author: liupanpan
 * @Date: 2019/3/27
 * @Copyright (c) 2013, yulink.io All Rights Reserved
 */

public class GzipUtils {
    public static void gzipFile(String sourceFilePath, String destZipFilepath) {

        byte[] buffer = new byte[1024];
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(destZipFilepath);
            GZIPOutputStream gzipOuputStream = new GZIPOutputStream(fileOutputStream);
            FileInputStream fileInput = new FileInputStream(sourceFilePath);
            int bytes_read;

            while ((bytes_read = fileInput.read(buffer)) > 0) {
                gzipOuputStream.write(buffer, 0, bytes_read);
            }

            fileInput.close();

            gzipOuputStream.finish();
            gzipOuputStream.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
