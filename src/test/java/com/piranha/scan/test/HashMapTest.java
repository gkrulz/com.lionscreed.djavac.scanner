package com.piranha.scan.test;

import java.util.HashMap;

/**
 * Created by Padmaka on 2/17/16.
 */
public class HashMapTest {

    public static void main(String[] args) {
        String one = "1";
        String two = "2";
        String three = "3";

        HashMap<String, String> mappedList = new HashMap<>();
        mappedList.put(one, one);
        mappedList.put(two, two);
        mappedList.put(three, three);

        System.out.println(mappedList.get("1"));
        System.out.println(mappedList.get("2"));
        System.out.println(mappedList.get("3"));
    }
}
