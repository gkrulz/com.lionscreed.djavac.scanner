package com.lionscreed.djavac.scanner;

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

    public Collection read(){
        File file = new File(System.getProperty("user.dir")+"\\src\\main\\resources");
        Collection files = FileUtils.listFiles(file, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
        return files;
    }

    public void findDependencies(ArrayList<File> files) throws IOException {
        for(File f : files){

            System.out.println(f.getName());
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(f));

            String fileString = FileUtils.readFileToString(f, inputStreamReader.getEncoding());

            Pattern pattern = Pattern.compile("(((public|protected|private|)?(\\s+abstract)?(\\s+static)?\\s+class\\s+(\\w+)((\\s+extends\\s+\\w+)|(\\s+implements\\s+\\w+\\s*(,\\s*\\w+\\s*)*)|(\\s+extends\\s+\\w+\\s+implements\\s+\\w+\\s*(,\\s*\\w+\\s*)*))?\\s*\\{)|" +
                    "((public|protected)?(\\s+abstract)?(\\s+static)?\\s+interface\\s+(\\w+)(\\s+extends\\s+\\w+\\s*(,\\s*\\w+\\s*)*)?\\s*\\{))");

            Matcher matcher = pattern.matcher(fileString);
            matcher.find();
                try {
                    System.out.println(matcher.group());
//                    System.out.println(matcher.end());
//                    System.out.println(fileString.charAt(matcher.end() - 1));
                    int endOfClass = this.getEndOfClass(fileString, matcher.end() - 1);
                    String str = fileString.substring(matcher.end(), endOfClass);
                    System.out.println("------------------------------------------------------");
                    System.out.println(str);
                    System.out.println("------------------------------------------------------");
                }catch (IllegalStateException e){
                    System.err.println(f.getName() + " - No match found");
                }


        }
    }

    public int getEndOfClass(String fileString, int startOfClass){

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
                }
            }
        }
        return endOfClass;
    }
}
