package org.sk.fxcss;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.*;
import java.util.List;

public class SnippetManager {
    private static final SnippetManager _instance=new SnippetManager();
    private final File SNIPPETS_FILE=new File("snippets.json");
    private SnippetManager(){
        if(!SNIPPETS_FILE.exists()) {
            try {
                SNIPPETS_FILE.createNewFile();

            } catch (IOException e) {
                System.out.println("Cannot create snippets file");

                System.exit(1);
                throw new RuntimeException(e);
            }
        }
    }
    public static SnippetManager getInstance(){
        return _instance;
    }

    public void saveSnippet(Snippet snippet) throws IOException {
        ObjectMapper mapper=new ObjectMapper();
        if(SNIPPETS_FILE.exists()&&SNIPPETS_FILE.length()>0){
            JsonNode jsonNode = mapper.readTree(SNIPPETS_FILE);
            if(jsonNode.isArray()){//as it should be
                ((ArrayNode)jsonNode).addPOJO(snippet);
                mapper.writeValue(SNIPPETS_FILE,jsonNode);
            }
        }else{
            ArrayNode rootArray = mapper.createArrayNode();
            rootArray.addPOJO(snippet);
            mapper.writeValue(SNIPPETS_FILE,rootArray);
        }

    }

    public List<Snippet> findSnippet(String name, String elementType) throws IOException {
        ObjectMapper mapper=new ObjectMapper();

        List<Snippet> snippets= List.of(mapper.readValue(SNIPPETS_FILE, Snippet[].class));
        return snippets.stream().filter((s) -> s.name().contains(name)
                || s.elementType().contains(elementType)).toList();
    }
    public Snippet getSnippet(String uuid) throws IOException {
        ObjectMapper mapper=new ObjectMapper();
        List<Snippet> snippets= List.of(mapper.readValue(SNIPPETS_FILE, Snippet[].class));
        List<Snippet> list = snippets.stream().filter((s) -> s.uuid().equals(uuid)).toList();
        if(!list.isEmpty())
            return list.get(0);
        return null;
    }
}
