package com.somewan.qiniu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by wan on 2017/2/27.
 */
public class FileService {
    private BufferedReader reader;

    public void initFileReader(File file) throws IOException {
        reader = new BufferedReader(new FileReader(file));
    }

    public String readLine() throws IOException {
        return reader.readLine();
    }

    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }
}
