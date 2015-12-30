package com.piranha.scan.test;

import com.piranha.scan.Scanner;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Padmaka on 9/8/2015.
 */
public class TestScanner {
    public static void main(String[] args) {
        Scanner scanner = new Scanner();
        Collection fileCollection = scanner.readFiles();
        ArrayList<File> files = new ArrayList<File>();

        for(Object obj :fileCollection){
            File f = (File)obj;
            files.add(f);
        }

        try {
            scanner.scan(files);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
