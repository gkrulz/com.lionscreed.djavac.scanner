package com.piranha.scan;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Padmaka on 2/18/16.
 */
public class DependencyResolver {
    private static final Logger log = Logger.getLogger(Compiler.class);
    private String[] keywords;
    private String[] operators;

    public DependencyResolver() throws IOException {
        File file = new File("src/main/resources/keywords.csv");
        FileInputStream in = new FileInputStream(file);

        String fileString = IOUtils.toString(in);
        fileString = fileString.replaceAll("\\s+", "");
        keywords = fileString.split(",");

        File file2 = new File("src/main/resources/operators.csv");
        FileInputStream in2 = new FileInputStream(file2);

        String fileString2 = IOUtils.toString(in2);
        fileString2 = fileString2.replaceAll("\\s+", "");
        operators = fileString2.split(",");
    }

    public String removeKeywordsAndOperators(String classString) {
        String newString = classString;

        for (int i = 0; i < this.keywords.length; i++) {
            newString = newString.replaceAll("(\\W+)?" + keywords[i] + "(\\W+)", " ");
        }

        for (int x = 0; x < this.operators.length; x++) {
            newString = newString.replace(this.operators[x], " ");
        }

        newString = newString.replace(",", " ");

        return newString;
    }

    public static void main(String[] args) {
        DependencyResolver dependencyResolver = null;
        Scanner scanner = new Scanner();

        try {
            dependencyResolver = new DependencyResolver();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String classString = "";

        classString = scanner.removeCommentsAndStrings(classString);

        classString = classString.replace("#", " ");

        classString = dependencyResolver.removeKeywordsAndOperators(classString);

        classString = classString.replaceAll("\\s+\\.\\w+", " ");

        String[] checkList = classString.split("\\s+");

        log.debug(classString);

        HashSet<String> checkListFinal = new HashSet<>();

        for (String str : checkList) {
            if (!str.trim().equals("") && !(str.matches("\\d+"))) {
                checkListFinal.add(str.trim());
                log.debug(str);
            }
        }

        Gson gson = new Gson();
        log.debug(gson.toJson(checkListFinal));
        log.debug(checkListFinal.size());
    }
}
