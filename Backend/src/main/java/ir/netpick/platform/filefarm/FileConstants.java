package ir.netpick.platform.filefarm;

/**
 * FileFarm - Documents and File Management Constants
 */
public class FileConstants {
    public static final long MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024; // 50MB
    public static final int MAX_FILE_NAME_LENGTH = 255;
    public static final int MAX_FOLDER_DEPTH = 10;
    public static final String[] ALLOWED_EXTENSIONS = {
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
        "txt", "jpg", "jpeg", "png", "gif", "zip", "rar"
    };
}