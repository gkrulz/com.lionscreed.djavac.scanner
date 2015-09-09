package com.lionscreed.djavac.scanner.test;

import com.lionscreed.djavac.scanner.Scanner;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Padmaka on 9/8/2015.
 */
public class TestScanner {
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
