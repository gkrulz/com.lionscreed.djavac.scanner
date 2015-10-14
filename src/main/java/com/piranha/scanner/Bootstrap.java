package com.piranha.scanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Padmaka on 8/25/2015.
 */
public class Bootstrap {
    public static void main(String[] args) {
        Scanner scanner = new Scanner();
        Collection fileCollection = scanner.read();
        ArrayList<File> files = new ArrayList<File>();

        for(Object obj :fileCollection){
            File f = (File)obj;
            files.add(f);
        }

        try {
            scanner.findDependencies(files);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
