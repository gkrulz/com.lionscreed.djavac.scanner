package com.piranha.scan;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.piranha.util.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Padmaka on 9/8/2015.
 */
public class Scanner {
    private static final Logger log = Logger.getLogger(Scanner.class);
    private ArrayList<JsonObject> classes;
    private ArrayList<JsonObject> detailedClassList;

    /***
     * overloaded constructor
     */
    public Scanner() {
        classes = new ArrayList<>();
    }

    /***
     * The method to read all the files found in the default location and input them to the system.
     *
     * @return collection of all the files found in the given location.
     */
    public Collection readFiles() {
        File file = new File(Constants.SOURCE_PATH);
        log.debug(Constants.SOURCE_PATH);
        return FileUtils.listFiles(file, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
    }

    /***
     * The method to read all the files found in the given location and input them to the system.
     *
     * @param path
     * @return collection of all the files found in the given location.
     */
    public Collection readFiles(String path) {
        File file = new File(path);
        return FileUtils.listFiles(file, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
    }

    /***
     * The method to scan through all the source files found and find classes, interfaces, enums and inner classes.
     *
     * @param files file list
     * @return a Json containing a list of all the classes, interfaces, enums and their details.
     * @throws IOException
     */
    public ArrayList<JsonObject> scan(ArrayList<File> files) throws IOException {
//        Gson gson = new Gson();
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        //Iterate through all the files
        for (File f : files) {

            if (!(f.getName().equals(".DS_Store"))) {
                log.debug(f.getAbsolutePath());

                if (f.getName().equals("CondVar.java")) {
                    log.debug("here");
                }

                InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(f));

                //getting file to a string
                String fileString = FileUtils.readFileToString(f, inputStreamReader.getEncoding());

                //Finding import statements
                String cleanedUpFileString = this.removeCommentsAndStrings(fileString);
                JsonArray importStatements = this.findImportStatements(cleanedUpFileString);
//            log.debug(importStatements);

                JsonObject classJson = this.getClass(fileString);

                try {
                    if (classJson != null) {
                        this.findOuterClasses(f, classJson.get("classDeclaration").getAsString(),
                                fileString, classJson.get("end").getAsInt() - 1, importStatements);
                    }
                } catch (IllegalStateException e) {
                    log.error(f.getName() + " - No match found");
                }

                //TODO
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

//        log.debug(gson.toJson(classes));
        detailedClassList = this.getFullClassList();

        return classes;
    }

    public ArrayList<JsonObject> removeUnnecessaryImportStatements() {
        Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();

        for (JsonObject classJson : classes) {
            ArrayList<String> dependencies = gson.fromJson(classJson.get("dependencies").getAsJsonArray(), listType);
            ArrayList<String> importStatements = gson.fromJson(classJson.get("importStatements").getAsJsonArray(), listType);
            ArrayList<Integer> removableIndexes = new ArrayList<>();

            for (int i = 0; i < importStatements.size(); i++) {
                String importStatement = importStatements.get(i);
                boolean withinSourceCode = false;
                boolean withinDependencies = false;

                for (JsonObject classDetail : detailedClassList) {
                    if (importStatement.contains(classDetail.get("packageName").getAsString())) {
                        withinSourceCode = true;
                    }
                }

                for (String dependency : dependencies) {
                    String packageName = dependency.substring(0, dependency.lastIndexOf('.'));
                    if (importStatement.contains(packageName)) {
                        withinDependencies = true;
                    }
                }

                if (withinSourceCode && !withinDependencies) {
                    removableIndexes.add(i);
//                    log.debug("class - " + classJson.get("absoluteClassName").getAsString() + " - " + importStatement);
                }
            }

            for (int index : removableIndexes) {
                importStatements.remove(index);
            }

            classJson.add("importStatements", parser.parse(gson.toJson(importStatements)));
        }

        return classes;
    }

    /***
     * The method to find a class declaration in a given String
     *
     * @param fileString File string
     * @return Json with class details
     */
    public JsonObject getClass(String fileString) {
        String cleanedUpFileString = this.removeCommentsAndStrings(fileString);

        Pattern pattern = Pattern.compile("(((public|protected|private|)?(\\s+abstract)?(\\s+static)?\\s+class\\s+(\\w+)" +
                "((\\s+extends\\s+(\\w+\\.)*?\\w+)|(\\s+implements\\s+(\\w+\\.)*?\\w+\\s*(,\\s*\\w+\\s*)*)|" +
                "(\\s+extends\\s+(\\w+\\.)*?\\w+\\s+implements\\s+(\\w+\\.)*?\\w+\\s*(,\\s*\\w+\\s*)*))?\\s*\\{)|" +
                "((public|protected)?(\\s+abstract)?(\\s+static)?\\s+interface\\s+(\\w+)(\\s+extends\\s+" +
                "(\\w+\\.)*?\\w+\\s*(,\\s*\\w+\\s*)*)?\\s*\\{)|" +
                "((public|protected|private)?(\\s+static)?\\s+enum\\s+(\\w+)" +
                "(\\s+implements\\s+(\\w+\\.)*?\\w+\\s*(,\\s*\\w+\\s*)*)?\\s*\\{))");

        Matcher matcher = pattern.matcher(cleanedUpFileString);

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

    /***
     * The method to find all outer classes in a given .java file string.
     *
     * @param file
     * @param className
     * @param fileString
     * @param startOfClass
     * @param importStatements
     */
    public void findOuterClasses(File file, String className, String fileString, int startOfClass,
                                 JsonArray importStatements) {
        String cleanedUpFileString = this.removeCommentsAndStrings(fileString);

        String rootPath = Constants.SOURCE_PATH + Constants.PATH_SEPARATOR;

        String classDeclaration = className;
//        int endOfClass = 0;
        Stack<Character> stack = new Stack<Character>();

//        log.debug(className);

        for (int i = startOfClass; i < cleanedUpFileString.length(); i++) {

            char current = cleanedUpFileString.charAt(i);
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
                    classJson.add("importStatements", importStatements);
                    String packageName = classJson.get("filePath").getAsString().replace(rootPath, "");
                    packageName = packageName.replace("/", ".");
                    packageName = packageName.replace(classJson.get("file").getAsString(), "");
                    classJson.addProperty("package", packageName);
                    classJson.addProperty("absoluteClassName", packageName + className);
                    classes.add(classJson);

                    if (i + 1 < fileString.length()) {
                        String restOfTheString = fileString.substring(i, fileString.length());
                        JsonObject nextClass = this.getClass(restOfTheString);
                        if (nextClass != null) {
                            log.debug(file.getAbsolutePath());
                            this.findOuterClasses(file, nextClass.get("classDeclaration").getAsString(),
                                    restOfTheString, nextClass.get("end").getAsInt() - 1, importStatements);
                        }
                    }
                    break;
                }
            }
        }
    }

    /***
     * The method to find all the names of inner classes in a given class body string
     *
     * @param classString
     * @return List of strings containing the names of inner classes.
     */
    public ArrayList<String> findInnerClasses(String classString) {
        ArrayList<String> innerClasses = new ArrayList<>();
        JsonObject innerClass = new JsonObject();
        String classDeclaration = null;
        int end = 0;

        while (innerClass != null) {
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
                classDeclaration = classDeclaration.trim();
                String[] classNameParts = classDeclaration.split(" ");
                classDeclaration = classNameParts[0];

                innerClasses.add(classDeclaration.trim());
                classString = classString.substring(end, classString.length() - 1);
            }
        }

        return innerClasses;
    }

    /***
     * The method to get a full list of names of the classes, interfaces, enums and inner classes with their details
     *
     * @return list of Json objects with full list of all the classes, interfaces, enums, and inners classes along
     * with outer class name and package name.
     */
    public ArrayList<JsonObject> getFullClassList() {
        ArrayList<JsonObject> classList = new ArrayList<>();
        String rootPath = Constants.SOURCE_PATH + Constants.PATH_SEPARATOR;

        for (JsonObject classJson : classes) {
            String packageName = classJson.get("filePath").getAsString().replace(rootPath, "");
            packageName = packageName.replace("/", ".");
            packageName = packageName.replace(classJson.get("file").getAsString(), "");
//            log.debug(packageName);

            JsonObject tempClassJson = new JsonObject();
            tempClassJson.addProperty("packageName", packageName);
            tempClassJson.addProperty("className", classJson.get("className").getAsString());
            tempClassJson.addProperty("outerClass", classJson.get("className").getAsString());
            classList.add(tempClassJson);
            if (classJson.get("innerClasses").getAsJsonArray().size() > 0) {
                for (JsonElement innerClass : classJson.get("innerClasses").getAsJsonArray()) {
                    JsonObject tempInnerClassJson = new JsonObject();
                    tempInnerClassJson.addProperty("packageName", packageName);
                    tempInnerClassJson.addProperty("className", innerClass.getAsString());
                    tempInnerClassJson.addProperty("outerClass", classJson.get("className").getAsString());
                    classList.add(tempInnerClassJson);
                }
            }
        }

        return classList;
    }

    /***
     * The method to find the import statements in a given .java file string.
     *
     * @param fileString
     * @return Json array of all the import statements.
     */
    public JsonArray findImportStatements(String fileString) {
        Pattern pattern = Pattern.compile("import(\\s+static)?\\s+(\\w+\\.)*(\\w+|\\*)");

        Matcher matcher = pattern.matcher(fileString);
        JsonArray importStatemetns = new JsonArray();

        try {
            while (matcher.find()) {
                importStatemetns.add(new JsonPrimitive(matcher.group().replace("import", "").trim()));
            }
        } catch (IllegalStateException e) {
            return null;
        }

        return importStatemetns;
    }

    /***
     * The method to remove any comments or String literals in a given class string.
     *
     * @param classString
     * @return class string without any comments or string literals.
     */
    public String removeCommentsAndStrings(String classString) {
//        log.debug(classString.length());
        StringBuilder result = new StringBuilder();
        Pattern pattern = Pattern.compile("((['\"])(?:(?!\\2|\\\\).|\\\\.)*\\2)|\\/\\/[^\\n]*|\\/\\*(?:[^*]|\\*(?!\\/))*\\*\\/");
        Matcher matcher = pattern.matcher(classString);
        int previous = 0;

//        try {
        while (matcher.find()) {
            result.append(classString.substring(previous, matcher.start()));

            result.append(buildStringWithDots(matcher.end() - matcher.start()));

            previous = matcher.end();
        }
//        } catch (StackOverflowError e){
//            log.error("", e);
//        }

        result.append(classString.substring(previous, classString.length()));
//        log.debug(result.length());

        return result.toString();

//        return classString.replaceAll("((['\"])(?:(?!\\2|\\\\).|\\\\.)*\\2)|\\/\\/[^\\n]*|\\/\\*(?:[^*]|\\*(?!\\/))*\\*\\/", "");
    }

    public String buildStringWithDots(int size) {
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < size; i++) {
            str.append("#");
        }

        return str.toString();
    }

    /***
     * The method to find the dependencies for all the classes in the source code.
     *
     * @return list of json objects which contain all class details along with dependencies.
     */
    public ArrayList<JsonObject> findDependencies() {

        for (JsonObject classJson : classes) {
            String className = classJson.get("className").getAsString();
            String classDeclaration = classJson.get("classDeclaration").getAsString();

            //Getting the package name of the current class
            String rootPath = Constants.SOURCE_PATH + Constants.PATH_SEPARATOR;
            String classPackageName = classJson.get("filePath").getAsString().replace(rootPath, "");
            classPackageName = classPackageName.replace("/", ".");
            classPackageName = classPackageName.replace(classJson.get("file").getAsString(), "");

            String classString = classJson.get("classString").getAsString();
            JsonArray innerClasses = classJson.get("innerClasses").getAsJsonArray();

            JsonArray importStatements = classJson.get("importStatements").getAsJsonArray();

            Set<String> dependencies = new HashSet<>();

            for (JsonObject classDetailsJson : getDetailedClassList()) {
                String packageName = classDetailsJson.get("packageName").getAsString();
                String checkingClassName = classDetailsJson.get("className").getAsString();
                String outerClass = classDetailsJson.get("outerClass").getAsString();

                //checking for dependencies in the class declaration
                //------------------------------------------------------------------------------------------------------

                classDeclaration = classDeclaration.replace(className, "");

                if (classDeclaration.contains(" " + checkingClassName)) {

                    String matchingImportStatement = null;
                    ArrayList<String> importStatementsWithoutClassNames = new ArrayList<>();

                    for (JsonElement importStatement : importStatements) {
                        String importString = importStatement.getAsString();

                        if (importString.matches("(\\w+\\.)*" + checkingClassName + "$")) {
                            matchingImportStatement = importString;
                            break;
                        } else if (importString.matches("(\\w+\\.)*\\*$")) {
                            importStatementsWithoutClassNames.add(importString);
                        }
                    }

                    if (matchingImportStatement != null) {

                        if (matchingImportStatement.equals(packageName + checkingClassName)) {
                            dependencies.add(packageName + outerClass);
                            classDeclaration = classDeclaration.replace(checkingClassName, "");
                            log.debug(className + " is dependent on " + packageName + outerClass);
                        }

                    } else {
                        boolean dependencyFound = false;

                        for (String importStatementWithoutClassName : importStatementsWithoutClassNames) {
                            String importPackageName = importStatementWithoutClassName.replace("*", "");

                            for (JsonObject classDetailsJsonLocal : getDetailedClassList()) {
                                String packageNameLocal = classDetailsJsonLocal.get("packageName").getAsString();
                                String checkingClassNameLocal = classDetailsJsonLocal.get("className").getAsString();
                                String outerClassLocal = classDetailsJsonLocal.get("outerClass").getAsString();

                                if ((importPackageName + checkingClassName).equals(packageNameLocal + checkingClassNameLocal)) {
                                    dependencies.add(packageNameLocal + outerClassLocal);
                                    classDeclaration = classDeclaration.replace(checkingClassName, "");
                                    dependencyFound = true;
                                    log.debug(className + " is dependent on " + packageNameLocal + outerClassLocal);
                                }
                            }
                        }

                        if (dependencyFound == false) {
                            for (JsonObject classDetailsJsonLocal : getDetailedClassList()) {
                                String packageNameLocal = classDetailsJsonLocal.get("packageName").getAsString();
                                String classNameLocal = classDetailsJsonLocal.get("className").getAsString();
                                String outerClassLocal = classDetailsJsonLocal.get("outerClass").getAsString();

                                if (classPackageName.equals(packageNameLocal) && classNameLocal.equals(checkingClassName)) {
                                    dependencies.add(packageNameLocal + outerClassLocal);
                                    classDeclaration = classDeclaration.replace(checkingClassName, "");
                                    log.debug(className + " is dependent on " + packageNameLocal + outerClassLocal);
                                }
                            }
                        }
                    }
                }

                if (classDeclaration.contains(packageName + checkingClassName)) {
                    dependencies.add(packageName + outerClass);
                    classDeclaration = classDeclaration.replace(packageName + checkingClassName, "");
                    log.debug(className + " is dependent on " + packageName + outerClass);
                }

                //------------------------------------------------------------------------------------------------------

                //checking for dependencies in the class body
                //------------------------------------------------------------------------------------------------------

                String cleanedUpClassString = this.removeCommentsAndStrings(classString);
//                log.debug(classJson.get("className").getAsString());
//                log.debug(cleanedUpClassString);
                cleanedUpClassString = cleanedUpClassString.replace(className, "");

                for (JsonElement innerClass : innerClasses) {
                    cleanedUpClassString = cleanedUpClassString.replace(innerClass.getAsString(), "");
                }
//                log.debug(cleanedUpClassString);
//
                if (cleanedUpClassString.contains(" " + checkingClassName)) {

                    String matchingImportStatement = null;
                    ArrayList<String> importStatementsWithoutClassNames = new ArrayList<>();

                    for (JsonElement importStatement : importStatements) {
                        String importString = importStatement.getAsString();

                        if (importString.matches("(\\w+\\.)*" + checkingClassName + "$")) {
                            matchingImportStatement = importString;
                            break;
                        } else if (importString.matches("(\\w+\\.)*\\*$")) {
                            importStatementsWithoutClassNames.add(importString);
                        }
                    }

                    if (matchingImportStatement != null) {

                        if (matchingImportStatement.equals(packageName + checkingClassName)) {
                            dependencies.add(packageName + outerClass);
                            cleanedUpClassString = cleanedUpClassString.replace(checkingClassName, "");
                            log.debug(className + " is dependent on " + packageName + outerClass);
                        }

                    } else {
                        boolean dependencyFound = false;

                        for (String importStatementWithoutClassName : importStatementsWithoutClassNames) {
                            String importPackageName = importStatementWithoutClassName.replace("*", "");

                            for (JsonObject classDetailsJsonLocal : getDetailedClassList()) {
                                String packageNameLocal = classDetailsJsonLocal.get("packageName").getAsString();
                                String checkingClassNameLocal = classDetailsJsonLocal.get("className").getAsString();
                                String outerClassLocal = classDetailsJsonLocal.get("outerClass").getAsString();

                                if ((importPackageName + checkingClassName).equals(packageNameLocal + checkingClassNameLocal)) {
                                    dependencies.add(packageNameLocal + outerClassLocal);
                                    cleanedUpClassString = cleanedUpClassString.replace(checkingClassName, "");
                                    dependencyFound = true;
                                    log.debug(className + " is dependent on " + packageNameLocal + outerClassLocal);
                                }
                            }
                        }

                        if (dependencyFound == false) {
                            for (JsonObject classDetailsJsonLocal : getDetailedClassList()) {
                                String packageNameLocal = classDetailsJsonLocal.get("packageName").getAsString();
                                String classNameLocal = classDetailsJsonLocal.get("className").getAsString();
                                String outerClassLocal = classDetailsJsonLocal.get("outerClass").getAsString();

                                if (classPackageName.equals(packageNameLocal) && classNameLocal.equals(checkingClassName)) {
                                    dependencies.add(packageNameLocal + outerClassLocal);
                                    cleanedUpClassString = cleanedUpClassString.replace(checkingClassName, "");
                                    log.debug(className + " is dependent on " + packageNameLocal + outerClassLocal);
                                }
                            }
                        }
                    }
                }

                if (cleanedUpClassString.contains(packageName + checkingClassName)) {
                    dependencies.add(packageName + outerClass);
                    log.debug(className + " is dependent on " + packageName + outerClass);
                }

                //------------------------------------------------------------------------------------------------------


                //In the end add dependencies to classes json
                JsonArray dependencyJsonArray = new JsonArray();

                for (String dependency : dependencies) {
                    dependencyJsonArray.add(new JsonPrimitive(dependency));
                }

                classJson.add("dependencies", dependencyJsonArray);
            }
        }

        this.findInheritanceTreeDependencies();

        this.addInheritedClassesToDependencies();

        return classes;
    }

    private void findInheritanceTreeDependencies(/*ArrayList<String> inheritanceTreeDependencies*/) {
        Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();

        for (JsonObject classJson : classes) {
            ArrayList<String> dependencies = gson.fromJson(classJson.get("dependencies").getAsJsonArray(), listType);
            ArrayList<String> importStatements = gson.fromJson(classJson.get("importStatements").getAsJsonArray(), listType);
            String classDeclaration = classJson.get("classDeclaration").getAsString();
            ArrayList<String> declarationDependencyNames = this.findInheritedClasses(classDeclaration);
            ArrayList<String> declarationDependencyFullNames = new ArrayList<>();

//            log.debug(classJson.get("absoluteClassName").getAsString() + " - " + declarationDependencyNames);

            for (int i = 0; i < declarationDependencyNames.size(); i++) {
                String declarationDependencyName = declarationDependencyNames.get(i);
                ArrayList<String> importStatementsWithoutClassNames = new ArrayList<>();
                boolean foundDependency = false;

                for (String importStatement : importStatements) {
                    if (importStatement.equals(declarationDependencyName)) {
                        declarationDependencyFullNames.add(importStatement);
                        foundDependency = true;
                        break;
                    } else if (importStatement.matches("(\\w+\\.)*" + declarationDependencyName + "$")) {
                        declarationDependencyFullNames.add(importStatement);
                        foundDependency = true;
                        break;
                    } else if (importStatement.matches("(\\w+\\.)*\\*$")) {
                        importStatementsWithoutClassNames.add(importStatement);
                    }
                }

                if (foundDependency == false) {
                    for (String importStatementWithoutClassName : importStatementsWithoutClassNames) {
                        for (JsonObject classDetails : detailedClassList) {
                            String declarationDependency = importStatementWithoutClassName.replace("*", "")
                                    + declarationDependencyName;
                            String checkingClassName = classDetails.get("packageName").getAsString()
                                    + classDetails.get("className");

                            if (declarationDependency.equals(checkingClassName)) {
                                declarationDependencyFullNames.add(checkingClassName);
                                foundDependency = true;
                            }
                        }
                    }
                }

                if (foundDependency == false) {
                    for (JsonObject classDetails : detailedClassList) {
                        String checkingClass = classDetails.get("packageName").getAsString()
                                + classDetails.get("className").getAsString();

                        String dependencyName = classJson.get("package").getAsString() + declarationDependencyName;

                        if (dependencyName.equals(checkingClass)) {
                            declarationDependencyFullNames.add(checkingClass);
                        }
                    }
                }
            }

            JsonArray superClasses = parser.parse(gson.toJson(declarationDependencyFullNames)).getAsJsonArray();
            classJson.add("superClasses", superClasses);
            log.debug(declarationDependencyFullNames);
        }
    }

    private void addInheritedClassesToDependencies() {
        Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        ArrayList<String> classNames = new ArrayList<>();

        for (JsonObject classJson : classes) {
            classNames.add(classJson.get("absoluteClassName").getAsString());
        }

        for (JsonObject classJson : classes) {
            HashSet<String> completeDependencyList = new HashSet<>();

            ArrayList<String> dependencies = gson.fromJson(classJson.get("dependencies").getAsJsonArray(), listType);

            for (String dependency : dependencies) {
                this.findInheritedDependencies(completeDependencyList, dependency, classNames);
            }

            completeDependencyList.addAll(dependencies);
            JsonArray newDependencyList = parser.parse(gson.toJson(completeDependencyList)).getAsJsonArray();
            classJson.add("dependencies", newDependencyList);
            log.debug(classJson.get("absoluteClassName").getAsString() + " - " + completeDependencyList);
        }
    }

    public void findInheritedDependencies(HashSet<String> completeDependencyList, String dependency, ArrayList<String> classNames) {
        Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();
        Gson gson = new Gson();

        for (JsonObject classJson : classes) {
            String className = classJson.get("absoluteClassName").getAsString();
            ArrayList<String> superClasses = gson.fromJson(classJson.get("superClasses").getAsJsonArray(), listType);

            if (className.equals(dependency) && superClasses.size() > 0) {
//                completeDependencyList.addAll(superClasses);

                for (String superClass : superClasses) {
                    if (classNames.contains(superClass)) {
                        this.findInheritedDependencies(completeDependencyList, superClass, classNames);
                        completeDependencyList.add(superClass);
                    }
                }
            }
        }
    }

    public ArrayList<String> findInheritedClasses(String classDeclaration) {
        ArrayList<String> dependencyNames = new ArrayList<>();
        StringBuilder result = new StringBuilder();
        Pattern pattern = Pattern.compile("(extends\\s+((\\w+\\.)*)?\\w+\\s*(,\\s*\\w+\\s*)*)|(implements\\s+((\\w+\\.)*)?\\w+\\s*(,\\s*((\\w+\\.)*)?\\w+\\s*)*)");
        Matcher matcher = pattern.matcher(classDeclaration);

        while (matcher.find()) {
            String match = matcher.group().replace("extends", "").replace("implements", "");
            String[] classNames = match.split(",");
            for (String className : classNames) {
                dependencyNames.add(className.trim());
            }
        }

        return dependencyNames;
    }

    public ArrayList<JsonObject> getDetailedClassList() {
        return detailedClassList;
    }
}