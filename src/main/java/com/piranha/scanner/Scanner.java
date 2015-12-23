package com.piranha.scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Padmaka on 9/8/2015.
 */
public class Scanner {
    private static final Logger log = Logger.getLogger(Scanner.class);
    private ArrayList<JsonObject> classes;
    private ArrayList<String> classNames;

    public Scanner() {
        classes = new ArrayList<JsonObject>();
    }

    public Collection read() {
        log.debug(System.getProperty("user.dir"));
        File file = new File(System.getProperty("user.dir") + "/src/main/resources");
        Collection files = FileUtils.listFiles(file, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
        return files;
    }

    public void findDependencies(ArrayList<File> files) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        for (File f : files) {

//            log.debug(f.getName());
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(f));

            String fileString = FileUtils.readFileToString(f, inputStreamReader.getEncoding());

            JsonObject classJson = this.getClass(fileString);

            try {
                if (classJson != null) {
                    this.findOutterClasses(f, classJson.get("classDeclaration").getAsString(), fileString, classJson.get("end").getAsInt() - 1);
                }
            } catch (IllegalStateException e) {
                log.error(f.getName() + " - No match found");
            }


        }
        log.debug(gson.toJson(classes));
    }

    public JsonObject getClass(String fileString) {

        Pattern pattern = Pattern.compile("(((public|protected|private|)?(\\s+abstract)?(\\s+static)?\\s+class\\s+(\\w+)((\\s+extends\\s+\\w+)|(\\s+implements\\s+\\w+\\s*(,\\s*\\w+\\s*)*)|(\\s+extends\\s+\\w+\\s+implements\\s+\\w+\\s*(,\\s*\\w+\\s*)*))?\\s*\\{)|" +
                "((public|protected)?(\\s+abstract)?(\\s+static)?\\s+interface\\s+(\\w+)(\\s+extends\\s+\\w+\\s*(,\\s*\\w+\\s*)*)?\\s*\\{))");

        Matcher matcher = pattern.matcher(fileString);

        JsonObject classJson = null;
        try {
            matcher.find();
            String classDeclaration = matcher.group();

            classJson = new JsonObject();
            classJson.addProperty("end", matcher.end());
            classJson.addProperty("classDeclaration", classDeclaration);
            String className = classDeclaration.replace("public", "");
            className = className.replace("protected", "");
            className = className.replace("private", "");
            className = className.replace("abstract", "");
            className = className.replace("static", "");
            className = className.replace("class", "");
            className = className.replace("interface", "");
            className = className.replace("extends", "");
            className = className.replace("implements", "");
            className = className.replace("{", "");
            className = className.trim();
            String[] classNameParts = className.split(" ");
            className = classNameParts[0];
            log.debug("\""+className+"\"");
//            classNames.add();
        } catch (IllegalStateException e) {
            return null;
        }


        return classJson;
    }

    public void findOutterClasses(File file, String className, String fileString, int startOfClass) {

        int endOfClass = 0;
        Stack<Character> stack = new Stack<Character>();

//        log.debug(fileString);

        for (int i = startOfClass; i < fileString.length(); i++) {

            char current = fileString.charAt(i);
            if (current == '{') {
                stack.push(current);
            }
            if (current == '}') {
                char last = stack.peek();
                if (current == '}' && last == '{') {
                    stack.pop();
                } else {
                    endOfClass = -1;
                }

                if (stack.isEmpty()) {
//                    endOfClass = i;
                    String classString = fileString.substring(startOfClass + 1, i);
                    JsonObject classJson = new JsonObject();
                    classJson.addProperty("file", file.getName());
                    classJson.addProperty("filePath", file.getPath());
                    classJson.addProperty("className", className);
                    classJson.addProperty("classSting", classString);
                    classes.add(classJson);

                    if (i + 1 < fileString.length()) {
                        String restOfTheString = fileString.substring(i, fileString.length());
                        JsonObject nextClass = this.getClass(restOfTheString);
                        if (nextClass != null) {
                            this.findOutterClasses(file, nextClass.get("classDeclaration").getAsString(), restOfTheString, nextClass.get("end").getAsInt() - 1);
                        }
                    }
                    break;
                }
            }
        }
    }
}
