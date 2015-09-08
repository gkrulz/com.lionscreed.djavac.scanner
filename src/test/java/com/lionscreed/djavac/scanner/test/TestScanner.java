package com.lionscreed.djavac.scanner.test;

import com.lionscreed.djavac.scanner.Scanner;
import org.apache.commons.io.FileUtils;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Padmaka on 9/8/2015.
 */
public class TestScanner {
    public static void main(String[] args) {
        Scanner scanner = new Scanner();
        Collection fileCollection = scanner.scan();
        ArrayList<File> files = new ArrayList<File>();
        TestScanner testScanner = new TestScanner();

        for(Object obj :fileCollection){
            File f = (File)obj;
            files.add(f);
        }

        try {
            testScanner.findDependencies(files);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void findDependencies(ArrayList<File> files) throws IOException {
        for(File f : files){
            System.out.println("------------------------------------------------------");
            System.out.println(f.getName());
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(f));

            String fileString = FileUtils.readFileToString(f, inputStreamReader.getEncoding());
            Pattern pattern = Pattern.compile("(public|private|abstract)\\s+(class|interface)\\s+\\w+((\\s+extends\\s+\\w+)|(\\s+implements\\s+\\w+( ,\\w+)*)|(\\s+extends\\s+\\w+\\s+implements\\s+\\w+( ,\\w+)*))?\\s*\\{");

            Matcher matcher = pattern.matcher(fileString);
            System.out.println(matcher.find());
            try {
                System.out.println(matcher.group());
                System.out.println("------------------------------------------------------");
            }catch (IllegalStateException e){
                e.printStackTrace();
            }

        }
    }
}
