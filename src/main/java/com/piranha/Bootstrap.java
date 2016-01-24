package com.piranha;

import com.google.gson.JsonObject;
import com.piranha.dist.Scheduler;
import com.piranha.scan.Scanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Padmaka on 8/25/2015.
 */
public class Bootstrap {
    public static void main(String[] args) {
        ArrayList<JsonObject> classes;
        ArrayList<JsonObject> detailedClassList;

        Scanner scanner = new Scanner();
        Collection fileCollection = scanner.readFiles();
        ArrayList<File> files = new ArrayList<File>();

        for(Object obj :fileCollection){
            File f = (File)obj;
            files.add(f);
        }

        try {
            classes = scanner.scan(files);
        } catch (IOException e) {
            e.printStackTrace();
        }

        classes = scanner.findDependencies();
        detailedClassList = scanner.getDetailedClassList();

        Scheduler scheduler = new Scheduler();
        scheduler.makeSchedule(classes, detailedClassList);
    }
}