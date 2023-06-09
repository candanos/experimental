package com.candanos.compiler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CobolPackaging {
    private String[] filePaths;
    private File cobolSourceRoot;

    CobolPackaging(File cobolSourceRoot) {
        this.cobolSourceRoot = cobolSourceRoot;
    }

    String[] getFilePaths() {
        return filePaths;
    }

    void setFilePaths(String[] filePaths) {
        this.filePaths = filePaths;
    }

    String getSourceFilesInPackagesAsJson(String[] filePaths)
            throws JsonProcessingException {
        List<String> relativeFilePaths = new ArrayList<String>();
        File root = this.cobolSourceRoot;

        Map<String, List<String>> groupedPaths =
                groupFilePaths(filePaths);

        /* debug
        for (Map.Entry<String, List<String>> entry : groupedPaths.entrySet()) {
            System.out.println("Parent path: " + entry.getKey());
            for (String path : entry.getValue()) {
                System.out.println("- " + path);
            }
        }
        */

        // Convert the map to a JSON string
        String jsonString = convertToJsonString(groupedPaths);
        return jsonString;
    }

    private String convertToJsonString(Object obj)
            throws JsonProcessingException {
        /*
        ObjectMapper objectMapper =
                new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                        .configure(
                                SerializationFeature
                                .WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED,
                                true)
                        .configure(
                                SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS,
                                true);
        ;
        */
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.writeValueAsString(obj);
/*        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
       */
    }

    public Map<String, List<String>> groupFilePaths(String[] filePaths) {
        Map<String, List<String>> groupedPaths = new HashMap<>();
        String fileName = "";
        for (String filePath : filePaths) {
            File file = new File(filePath);
            fileName = file.getName();
            String parentPath = file.getParent();
            File parentDir = new File(parentPath);
            String packageName = this.cobolSourceRoot.toPath()
                    .relativize(parentDir.toPath()).toString();
            if (parentPath != null) {
                if (!groupedPaths.containsKey(packageName)) {
                    groupedPaths.put(packageName, new ArrayList<>());
                }
                groupedPaths.get(packageName).add(fileName);
            }
        }

        return groupedPaths;
    }
}