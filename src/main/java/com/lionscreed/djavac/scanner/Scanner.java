package com.lionscreed.djavac.scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.File;
import java.util.Collection;

/**
 * Created by Padmaka on 9/8/2015.
 */
public class Scanner {
    public Collection scan(){
        File file = new File(System.getProperty("user.dir")+"\\src\\main\\resources");
        System.out.println(System.getProperty("user.dir")+"\\src\\main\\resources");
        System.out.println(file.exists() ? "yes" : "NO");
        Collection files = FileUtils.listFiles(file, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
        return files;
    }
}
