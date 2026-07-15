package com.vke.test.parsing;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.parsing.config.utils.stringify.JsonStringifier;
import com.vke.core.parsing.config.utils.stringify.XmlStringifier;
import com.vke.utils.io.Identifier;

import java.io.IOException;

public class Transpile {
    public static void main(String[] args) throws IOException {
        Identifier identifier = new Identifier("assets/global/pipelines/demo.pipeline.json");
        ConfigDocument document = ConfigDocument.parseIdentifier(identifier);
        ConfigNode node = document.getRoot();
        boolean toXml = true;
        if (toXml) {
            System.out.println(XmlStringifier.stringify(node, "root"));
        } else {
            System.out.println(JsonStringifier.stringify(node));
        }
    }
}
