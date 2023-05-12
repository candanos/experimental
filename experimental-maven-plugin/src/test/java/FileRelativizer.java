import java.io.File;
import java.nio.file.Path;

public class FileRelativizer {

    public static void main(String[] args) {
        File root = new File("/Users/username");
        File file = new File("/Users/username/Documents/myfile.txt");
        Path relativePath = root.toPath().relativize(file.toPath());
        System.out.println(relativePath.toString());
    }
}