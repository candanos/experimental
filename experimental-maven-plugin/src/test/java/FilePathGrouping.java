import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FilePathGrouping {

    public static void main(String[] args) {
        String[] filePaths = {
                "/path/to/file1.txt",
                "/path/to/file2.txt",
                "/path/to/other/file3.txt",
                "/path/to/other/file4.txt",
                "/another/path/to/file5.txt"
        };

        Map<String, List<String>> groupedPaths = groupFilePaths(filePaths);

        for (Map.Entry<String, List<String>> entry : groupedPaths.entrySet()) {
            System.out.println("Parent path: " + entry.getKey());
            for (String path : entry.getValue()) {
                System.out.println("- " + path);
            }
        }
        // Convert the map to a JSON string
        String jsonString = convertToJsonString(groupedPaths);
        System.out.println(jsonString);

    }

    private static String convertToJsonString(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, List<String>> groupFilePaths(String[] filePaths) {
        Map<String, List<String>> groupedPaths = new HashMap<>();

        for (String filePath : filePaths) {
            File file = new File(filePath);
            String parentPath = file.getParent();
            if (parentPath != null) {
                if (!groupedPaths.containsKey(parentPath)) {
                    groupedPaths.put(parentPath, new ArrayList<>());
                }
                groupedPaths.get(parentPath).add(filePath);
            }
        }

        return groupedPaths;
    }

}