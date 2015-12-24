package com.piranha.scanner;

import com.google.gson.*;
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

    public Scanner() {
        classes = new ArrayList<>();
    }

    public Collection readFiles() {
        log.debug(System.getProperty("user.dir"));
        File file = new File(System.getProperty("user.dir") + "/src/main/resources");
        Collection files = FileUtils.listFiles(file, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
        return files;
    }

    public ArrayList<JsonObject> scan(ArrayList<File> files) throws IOException {
        Gson gson = new Gson();
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        for (File f : files) {

//            log.debug(f.getName());
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(f));

            String fileString = FileUtils.readFileToString(f, inputStreamReader.getEncoding());

            JsonObject classJson = this.getClass(fileString);

            try {
                if (classJson != null) {
                    this.findOuterClasses(f, classJson.get("classDeclaration").getAsString(), fileString, classJson.get("end").getAsInt() - 1);
                }
            } catch (IllegalStateException e) {
                log.error(f.getName() + " - No match found");
            }

        }

        //finding inner classes
        ArrayList<String> innerClasses;

        for (JsonObject classJson : classes) {
            innerClasses = this.findInnerClasses(classJson.get("classString").getAsString());

            JsonArray innerClassesJsonArray = new JsonArray();
            for (String innerClass : innerClasses) {
                innerClassesJsonArray.add(new JsonPrimitive(innerClass));
            }
            classJson.add("innerClasses", innerClassesJsonArray);
        }

        log.debug(gson.toJson(classes));
        log.debug(this.getFullClassList());
        return classes;
    }

    public JsonObject getClass(String fileString) {

//        Pattern pattern = Pattern.compile("(((public|protected|private|)?(\\s+abstract)?(\\s+static)?\\s+class\\s+(\\w+)((\\s+extends\\s+\\w+)|(\\s+implements\\s+\\w+\\s*(,\\s*\\w+\\s*)*)|(\\s+extends\\s+\\w+\\s+implements\\s+\\w+\\s*(,\\s*\\w+\\s*)*))?\\s*\\{)|" +
//                "((public|protected)?(\\s+abstract)?(\\s+static)?\\s+interface\\s+(\\w+)(\\s+extends\\s+\\w+\\s*(,\\s*\\w+\\s*)*)?\\s*\\{))");

        Pattern pattern = Pattern.compile("(((public|protected|private|)?(\\s+abstract)?(\\s+static)?\\s+class\\s+(\\w+)((\\s+extends\\s+\\w+)|(\\s+implements\\s+\\w+\\s*(,\\s*\\w+\\s*)*)|(\\s+extends\\s+\\w+\\s+implements\\s+\\w+\\s*(,\\s*\\w+\\s*)*))?\\s*\\{)|" +
                "((public|protected)?(\\s+abstract)?(\\s+static)?\\s+interface\\s+(\\w+)(\\s+extends\\s+\\w+\\s*(,\\s*\\w+\\s*)*)?\\s*\\{)|" +
                "((public|protected|private)?(\\s+static)?\\s+enum\\s+(\\w+)(\\s+implements\\s+\\w+\\s*(,\\s*\\w+\\s*)*)?\\s*\\{))");

        Matcher matcher = pattern.matcher(fileString);

        JsonObject classJson = null;
        try {
            matcher.find();
            String classDeclaration = matcher.group();

            classJson = new JsonObject();
            classJson.addProperty("end", matcher.end());
            classJson.addProperty("classDeclaration", classDeclaration);
        } catch (IllegalStateException e) {
            return null;
        }


        return classJson;
    }

    public void findOuterClasses(File file, String className, String fileString, int startOfClass) {
        String classDeclaration = className;
//        int endOfClass = 0;
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
//                    endOfClass = -1;
                }

                if (stack.isEmpty()) {
//                    endOfClass = i;
                    String classString = fileString.substring(startOfClass + 1, i);
                    JsonObject classJson = new JsonObject();
                    classJson.addProperty("file", file.getName());
                    classJson.addProperty("filePath", file.getPath());

                    className = className.replace("public", "");
                    className = className.replace("protected", "");
                    className = className.replace("private", "");
                    className = className.replace("abstract", "");
                    className = className.replace("static", "");
                    className = className.replace("class", "");
                    className = className.replace("interface", "");
                    className = className.replace("enum", "");
                    className = className.replace("{", "");
                    className = className.trim();
                    String[] classNameParts = className.split(" ");
                    className = classNameParts[0];

                    classJson.addProperty("className", className);
                    classJson.addProperty("classDeclaration", classDeclaration.trim());
                    classJson.addProperty("classString", classString.trim());
                    classes.add(classJson);

                    if (i + 1 < fileString.length()) {
                        String restOfTheString = fileString.substring(i, fileString.length());
                        JsonObject nextClass = this.getClass(restOfTheString);
                        if (nextClass != null) {
                            this.findOuterClasses(file, nextClass.get("classDeclaration").getAsString(), restOfTheString, nextClass.get("end").getAsInt() - 1);
                        }
                    }
                    break;
                }
            }
        }
    }

    public ArrayList<String> findInnerClasses(String classString) {
        ArrayList<String> innerClasses = new ArrayList<>();
        JsonObject innerClass = new JsonObject();
        String classDeclaration = null;
        int end = 0;

        while (innerClass != null){
            innerClass = this.getClass(classString);
            if (innerClass != null) {
                classDeclaration = innerClass.get("classDeclaration").getAsString();
                end = innerClass.get("end").getAsInt();

                classDeclaration = classDeclaration.replace("public", "");
                classDeclaration = classDeclaration.replace("protected", "");
                classDeclaration = classDeclaration.replace("private", "");
                classDeclaration = classDeclaration.replace("abstract", "");
                classDeclaration = classDeclaration.replace("static", "");
                classDeclaration = classDeclaration.replace("class", "");
                classDeclaration = classDeclaration.replace("interface", "");
                classDeclaration = classDeclaration.replace("enum", "");
                classDeclaration = classDeclaration.replace("{", "");
//                classDeclaration = classDeclaration.replace("\n", "");
                classDeclaration = classDeclaration.trim();
                String[] classNameParts = classDeclaration.split(" ");
                classDeclaration = classNameParts[0];

                log.debug("\""+classDeclaration+"\"");
                innerClasses.add(classDeclaration.trim());
                classString = classString.substring(end, classString.length() - 1);
            }
        }

        return innerClasses;
    }

    public ArrayList<JsonObject> getFullClassList(){
        ArrayList<JsonObject> classList = new ArrayList<>();
        String rootPath = System.getProperty("user.dir")+"/src/main/resources/";

        for (JsonObject classJson : classes) {
            String packageName = classJson.get("filePath").getAsString().replace(rootPath, "");
            packageName = packageName.replace("/", ".");
            packageName = packageName.replace(classJson.get("file").getAsString(), "");
//            log.debug(packageName);

            JsonObject tempClassJson = new JsonObject();
            tempClassJson.addProperty("class", packageName+classJson.get("className").getAsString());
            tempClassJson.addProperty("outerClass", packageName+classJson.get("className").getAsString());
            classList.add(tempClassJson);
            if(classJson.get("innerClasses").getAsJsonArray().size() > 0) {
                for (JsonElement innerClass : classJson.get("innerClasses").getAsJsonArray()){
                    JsonObject tempInnerClassJson = new JsonObject();
                    tempInnerClassJson.addProperty("class", packageName+innerClass.getAsString());
                    tempInnerClassJson.addProperty("outerClass", packageName+classJson.get("className").getAsString());
                    classList.add(tempInnerClassJson);
                }
            }
        }

        return classList;
    }
}
