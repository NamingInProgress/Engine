package com.vke.test;

import com.vke.utils.iter.helpers.Option;

public class OptionFlattenTest {
    public static void main(String[] args) {
        Option<Option<Option<Integer>>> opt = Option.some(Option.some(Option.some(5)));

        Option<Integer> flat = opt.flatten();

        System.out.println(flat);
    }
}
