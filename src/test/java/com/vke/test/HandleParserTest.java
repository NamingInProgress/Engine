package com.vke.test;

import com.vke.api.rendering.vulkan.descriptors.handles.parsing.HandleParser;
import com.vke.api.rendering.vulkan.descriptors.handles.parsing.node.BaseNode;

public class HandleParserTest {

    public static void main(String[] args) {
        BaseNode node = new HandleParser().parse("something[4].idk.somethingElse[2].final");
        System.out.println(node);
    }

}
