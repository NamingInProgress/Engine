package com.vke.test.parsing;

import com.vke.core.parsing.config.schema.vks.doc.VksDocument;
import com.vke.core.parsing.config.utils.stringify.XmlStringifier;

import java.io.IOException;

public class VksTypeTest {
    public static void main(String[] args) throws IOException {
        Identifier identifier = new Identifier("assets/global/schema/asset-meta.vks");
        VksDocument schema = new VksDocument(identifier);
        System.out.println(schema);
        System.out.println(XmlStringifier.stringify(schema.getRoot(), "root"));
    }
}
