package com.piranha.scan;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Padmaka on 2/22/16.
 */
public class ScannerTask implements Runnable {
    private static final Logger log = Logger.getLogger(ScannerTask.class);
    private ScannerX scanner;
    private JsonObject classJson;
    private int no;

    public ScannerTask(int no, ScannerX scanner, JsonObject classJson) {
        this.no = no;
        this.scanner = scanner;
        this.classJson = classJson;
    }

    @Override
    public void run() {
        this.resolveDependency(classJson);
    }

    private void resolveDependency(JsonObject classJson) {

        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        DependencyResolver dependencyResolver = null;
        try {
            dependencyResolver = new DependencyResolver();
        } catch (IOException e) {
            log.error("Unable to initialize Dependency Resolver");
        }

        String className = classJson.get("className").getAsString();

        String classDeclaration = classJson.get("classDeclaration").getAsString();

        String classPackageName = classJson.get("package").getAsString();

        String classString = classJson.get("classString").getAsString();

//        JsonArray innerClasses = classJson.get("innerClasses").getAsJsonArray();

        ArrayList<String> importStatements = gson.fromJson(classJson.get("importStatements").getAsJsonArray(), listType);

        ArrayList<String> importPackages = new ArrayList<>();

        ConcurrentHashMap<String, String> dependencies = new ConcurrentHashMap<>();

        ArrayList<String> dependencyCheckList = new ArrayList<>();

        String fullClassString = classDeclaration + classString;

        fullClassString = scanner.removeCommentsAndStrings(fullClassString);

        fullClassString = fullClassString.replace("#", " ");

        fullClassString = dependencyResolver.removeKeywordsAndOperators(fullClassString);

        fullClassString = fullClassString.replaceAll("\\s+\\.\\w+", " ");

        fullClassString = fullClassString.replace(className, " ");

        String[] checkList = fullClassString.split("\\s+");

        HashSet<String> finalCheckList = new HashSet<>();

        //get all the import statements to a Hashmap
        HashMap<String, String> importStatementsMap = new HashMap<>();

        //Get all the * packages to a separate list
        ArrayList<String> starImportPackages = new ArrayList<>();

        //Removing classNames and '*' symbol from import statements and making a new list called importPackages
        for (String importStatement : importStatements) {
            String[] importStatementParts = importStatement.split("\\.");
            importPackages.add(importStatement.replace(importStatementParts[importStatementParts.length - 1], "").trim());
            importStatementsMap.put(importStatement, importStatement);

            if (importStatement.matches("(\\w+\\.)*\\*$")) {
                starImportPackages.add(importStatement.replace("*", "").trim());
            }
        }

        for (String str : checkList) {
            if (!str.trim().equals("") && !(str.matches("\\d+"))) {
                finalCheckList.add(str.trim());
//                    log.debug(str);
            }
        }

        for (String checkingClass : finalCheckList) {

            if (checkingClass.contains(".")) {

                //If the string contains dots then check in the directory structure
                if (scanner.getDirectory().find(checkingClass) != null) {
                    String dependencyClassName = scanner.getDirectory().find(checkingClass).get("absoluteClassName").getAsString();
                    dependencies.put(dependencyClassName, dependencyClassName);
                } else if (importStatements.size() > 0){
                    String[] parts = checkingClass.split("\\.");

                    for (String part : parts) {
                        //Check whether the potential dependency is mentioned in import statements
                        for (String importPackage : importPackages) {
                            if (importStatementsMap.get(importPackage + part) != null &&
                                    scanner.getClasses().get(importPackage + part) != null) {

                                dependencies.put(importPackage + part, importPackage + part);
                            }
                        }
                    }

                } else {

                    //Checking whether the potential dependency is in * import packages
                    for (String starImportPackage : starImportPackages) {
                        if (scanner.getClasses().get(starImportPackage + checkingClass) != null) {
                            dependencies.put(starImportPackage + checkingClass, starImportPackage + checkingClass);
                        }
                    }

                }


            } else {
                //Check whether the potential dependency is in the class package
                if (scanner.getClasses().get(classPackageName + checkingClass) != null) {

                    dependencies.put(classPackageName + checkingClass, classPackageName + checkingClass);

                } else if (importStatements.size() > 0){

                    //Check whether the potential dependency is mentioned in import statements
                    for (String importPackage : importPackages) {
                        if (importStatementsMap.get(importPackage + checkingClass) != null &&
                                scanner.getClasses().get(importPackage + checkingClass) != null) {

                            dependencies.put(importPackage + checkingClass, importPackage + checkingClass);
                        }
                    }

                } else {

                    //Checking whether the potential dependency is in * import packages
                    for (String starImportPackage : starImportPackages) {
                        if (scanner.getClasses().get(starImportPackage + checkingClass) != null) {
                            dependencies.put(starImportPackage + checkingClass, starImportPackage + checkingClass);
                        }
                    }

                }
            }
        }

        //Adding the dependencies to the class Json
        String dependencyList = gson.toJson(dependencies);
        classJson.addProperty("dependencies", gson.toJson(dependencies));
        log.debug("[" + (no + 1) + "/" + scanner.getClasses().values().size() + "] " + classPackageName + className + " - " + dependencyList);

    }
}
