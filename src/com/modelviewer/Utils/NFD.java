package com.modelviewer.Utils;

import org.lwjgl.PointerBuffer;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.util.nfd.NativeFileDialog.*;

public class NFD {
    private static String fileList = "";

    public static String openSingle() {
        PointerBuffer outPath = memAllocPointer(1);
        String filePath;

        try {
            filePath = checkResult(
                    NFD_OpenDialog(fileList, null, outPath),
                    outPath
            );
        } finally {
            memFree(outPath);
        }

        return filePath;
    }

    public static String openFolder() {
        PointerBuffer outPath = memAllocPointer(1);
        String filePath;

        try {
            filePath = checkResult(
                    NFD_PickFolder((ByteBuffer)null, outPath),
                    outPath
            );
        } finally {
            memFree(outPath);
        }

        return filePath;
    }

    private static String checkResult(int result, PointerBuffer path) {
        if(result == NFD_OKAY) {
            String filePath = path.getStringUTF8(0);
            nNFD_Free(path.get(0));
            return filePath;
        } else if(result == NFD_CANCEL) {
            return null;
        } else {
            System.err.format("Error: %s\n", NFD_GetError());
        }

        return null;
    }
}
