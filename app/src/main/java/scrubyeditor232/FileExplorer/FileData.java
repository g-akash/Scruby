package scrubyeditor232.FileExplorer;

public class FileData {

    // stores data for file or directory item
    private String filename = "newfile.txt";
    private String filePath = "/";
    private boolean isFile = true;

    public FileData() {

    }

    public FileData(String filename, String filePath, boolean isFile) {
        this.filename = filename;
        this.filePath = filePath;
        this.isFile = isFile;
    }

    public String getname() {
        return filename;
    }

    public String getpath() {
        return filePath;
    }

    public boolean getIsFile() {
        return isFile;
    }

    public void setname(String filename) {
        this.filename = filename;
    }

    public void setpath(String filePath) {
        this.filePath = filePath;
    }

    public void setIsFile(boolean isFile) {
        this.isFile = isFile;
    }

}

