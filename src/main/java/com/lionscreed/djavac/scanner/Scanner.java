package com.lionscreed.djavac.scanner;

import com.google.gson.JsonObject;
import com.lionscreed.djavac.scanner.model.JavaClass;
import netscape.javascript.JSObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

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
    private ArrayList<JavaClass> javaClasses;

    public Scanner(){
        javaClasses = new ArrayList<JavaClass>();
    }

    public Collection read(){
        File file = new File(System.getProperty("user.dir")+"/src/main/resources");
        Collection files = FileUtils.listFiles(file, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
        return files;
    }

    public void findDependencies(ArrayList<File> files) throws IOException {
        for(File f : files){

            System.out.println(f.getName());
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(f));

            String fileString = FileUtils.readFileToString(f, inputStreamReader.getEncoding());

            JsonObject classJson = this.getStartOfClass(fileString);

                try {
                    System.out.println(classJson.get("classDeclaration").getAsString());
//                    System.out.println(matcher.end());
//                    System.out.println(fileString.charAt(matcher.end() - 1));
                    int endOfClass = this.getEndOfClass(fileString, classJson.get("end").getAsInt() - 1);
                    String str = fileString.substring(classJson.get("end").getAsInt(), endOfClass);
                    JavaClass javaClass = new JavaClass();
                    javaClass.setDeclaration(classJson.get("classDeclaration").getAsString());
                    javaClass.setBody(str);
                    javaClasses.add(javaClass);
                    System.out.println("------------------------------------------------------");
                    System.out.println(str);
                    System.out.println("------------------------------------------------------");
                }catch (IllegalStateException e){
                    System.err.println(f.getName() + " - No match found");
                }


        }
    }

    public JsonObject getStartOfClass(String fileString){

        Pattern pattern = Pattern.compile("(((public|protected|private|)?(\\s+abstract)?(\\s+static)?\\s+class\\s+(\\w+)((\\s+extends\\s+\\w+)|(\\s+implements\\s+\\w+\\s*(,\\s*\\w+\\s*)*)|(\\s+extends\\s+\\w+\\s+implements\\s+\\w+\\s*(,\\s*\\w+\\s*)*))?\\s*\\{)|" +
                "((public|protected)?(\\s+abstract)?(\\s+static)?\\s+interface\\s+(\\w+)(\\s+extends\\s+\\w+\\s*(,\\s*\\w+\\s*)*)?\\s*\\{))");

        Matcher matcher = pattern.matcher(fileString);
        matcher.find();

        JsonObject classJson = new JsonObject();
        classJson.addProperty("end", matcher.end());
        classJson.addProperty("classDeclaration", matcher.group());
        return classJson;
    }

    public ArrayList<JsonObject> getEndOfClass(String fileString, int startOfClass){

        int endOfClass = 0;
        Stack<Character> stack = new Stack<Character>();

        for (int i = startOfClass; i < fileString.length(); i++) {

            char current = fileString.charAt(i);
            if (current == '{') {
                stack.push(current);
            }
            if (current == '}') {
                char last = stack.peek();
                if (current == '}' && last == '{') {
                    stack.pop();
                }
                else{
                    endOfClass = -1;
                }

                if (stack.isEmpty()){
                    endOfClass = i;
                    if (i < fileString.length()){
                        //TODO find the next class
                        String restOfTheString = fileString.substring(i, fileString.length())
                        JsonObject nextClass = this.getStartOfClass(restOfTheString);
                    }
                    break;
                }
            }
        }
        return endOfClass;
    }
}
