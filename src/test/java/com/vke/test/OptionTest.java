package com.vke.test;

import com.vke.utils.iter.helpers.Option;

public class OptionTest {

    public static void main(String[] args) {
        String helllo = "hi";
        Option<String> opt = Option.some(helllo);
        System.out.println(opt);
    }

}
