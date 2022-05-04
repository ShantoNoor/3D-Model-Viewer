package com.modelviewer.Window.Input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileReader {
    public static String read(String filePath) {
        String fileText = null;

        try {
            fileText = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            System.out.println("Error unable to read file: " + filePath);
            e.printStackTrace();
            System.exit(-1);
        }

        return fileText;
    }
}
