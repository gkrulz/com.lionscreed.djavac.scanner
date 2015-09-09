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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Padmaka on 9/8/2015.
 */
public class Scanner {
    public Collection read(){
        File file = new File(System.getProperty("user.dir")+"\\src\\main\\resources");
        System.out.println(System.getProperty("user.dir")+"\\src\\main\\resources");
        System.out.println(file.exists() ? "yes" : "NO");
        Collection files = FileUtils.listFiles(file, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
        return files;
    }

    public void findDependencies(ArrayList<File> files) throws IOException {
        for(File f : files){
            System.out.println("------------------------------------------------------");
            System.out.println(f.getName());
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(f));

            String fileString = FileUtils.readFileToString(f, inputStreamReader.getEncoding());
//            Pattern pattern = Pattern.compile("(public|private|abstract)\\s+(class|interface)\\s+\\w+((\\s+extends\\s+\\w+)|(\\s+implements\\s+\\w+( ,\\w+)*)|(\\s+extends\\s+\\w+\\s+implements\\s+\\w+( ,\\w+)*))?\\s*\\{");
//            Pattern pattern = Pattern.compile("((public|private|abstract)\\s+(class|interface)\\s+\\w+((\\s+extends\\s+\\w+)|(\\s+implements\\s+\\w+( ,\\w+)*)|(\\s+extends\\s+\\w+\\s+implements\\s+\\w+( ,\\w+)*))?\\s*\\{)|((class|interface)\\s+\\w+((\\s+extends\\s+\\w+)|(\\s+implements\\s+\\w+( ,\\w+)*)|(\\s+extends\\s+\\w+\\s+implements\\s+\\w+( ,\\w+)*))?\\s*\\{)");
            Pattern pattern = Pattern.compile("((public|protected|private)?\\s*class\\s+(\\w+)((\\s+extends\\s+\\w+)|(\\s+implements\\s+\\w+\\s*(,\\s*\\w+)*))?\\s*)?\\{([^{}]|(\\{([^{}])*\\}))*\\}");
            Matcher matcher = pattern.matcher(fileString);
            while(matcher.find()){
                try {
                    System.out.println(matcher.group());
                }catch (IllegalStateException e){
                    System.err.println(f.getName() + " - No match found");
                }
            }
            System.out.println("------------------------------------------------------");
        }
    }
}
