package com.vke.core.assets;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.Bundle;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.core.VKEngine;
import com.vke.utils.Identifier;
import com.vke.utils.Utils;
import com.vke.utils.exception.Unreachable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class AssetUtils {

    public static Bundle getBundle(VKEngine engine, Identifier ident) {
        Bundle bundle = new Bundle(engine);
        Identifier bundleXMLIdent = ident.extend("bundle.xml");
        if (!bundleXMLIdent.existsFile()) {
            bundleXMLIdent = ident.extend("bundle.json");
        }

        if (bundleXMLIdent.existsFile()) {
            try {
                InputStream xmlStream = bundleXMLIdent.asInputStream();
                ConfigParser parser = ConfigParser.forFileType(bundleXMLIdent.getPath());
                if (parser == null) {
                    throw new Unreachable();
                }
                parser.setSource(Utils.readCharsFromInputStream(xmlStream));
                ConfigDocument document = parser.parse(ConfigParser.ATTRIBS_TO_FIELDS | ConfigParser.PARSE_LITERALS);

                readBundleXml(bundle, document);
            } catch (ConfigParser.ConfigParseException | IOException e) {
                engine.throwException(e, "BundleXML");
                System.exit(67);
            }
        }

        for (Identifier file : ident.walkFiles()) {
            if (file.equals(bundleXMLIdent)) continue;

            Identifier id = file.strip();
            AssetHandle<?> handle = AssetHandle.ofFile(file);
            bundle.addAsset(id, handle);
        }

        return bundle;
    }

    public static Bundle collectGlobalBundles(VKEngine engine) {
        Bundle globalBundle = new Bundle(engine);

        for (String namespace : engine.getAllNamespaces()) {
            Identifier globalBundleIdent = new Identifier(namespace, "assets/global");
            if (globalBundleIdent.existsFile()) {
                Bundle thisOne = getBundle(engine, globalBundleIdent);
                globalBundle.extendBundle(thisOne);
            }
        }

        return globalBundle;
    }

    public static void collectBundles(VKEngine engine, VKEAssetManager manager) {
        for (String namespace : engine.getAllNamespaces()) {
            Identifier ident = new Identifier(namespace, "assets");
            for (Identifier dir : ident.walkDirectories(1)) {
                if (dir.equals(new Identifier(namespace, "assets/global"))) continue;
                Bundle bundle = getBundle(engine, dir);
                manager.addBundle(dir.strip(), bundle);
            }
        }
    }

    private static void readBundleXml(Bundle bundle, ConfigDocument xml) {

    }
}
