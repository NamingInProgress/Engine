package com.vke.core.file.exr;

import com.vke.api.logger.Logger;
import com.vke.core.file.exr.head.ExrHeaderHead;
import com.vke.core.file.exr.head.attr.*;
import com.vke.core.file.utils.DataUtils;
import com.vke.core.logger.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class ExrDecoder {
    public static final Logger LOGGER = LoggerFactory.get("EXR Decoder");

    public final ExrHeaderHead head;

    private final HashMap<String, ExrAttr.Factory> attrFactories;

    public ExrDecoder(InputStream stream) throws IOException {
        this.attrFactories = new HashMap<>();
        registerAttrs();
        this.head = new ExrHeaderHead(stream);
    }

    public final String readExrStringNull(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        int limit = head.hasLongNames ? 255 : 31;
        for (int i = 0; i < limit; i++) {
            int character = DataUtils.readU8(stream);
            if (character == 0) {
                return builder.toString();
            }
            builder.append((char) character);
        }
        throw new IOException("Could not read Exr String (expected a terminator within " + limit + " bytes, still going)");
    }

    public final ExrAttr.Factory getAttrFactory(String type) {
        var fac = attrFactories.get(type);
        if (fac == null) {
            LOGGER.debug("Type '%s' has no factory registered, skipping attribute", type);
        }
        return fac;
    }

    protected final void registerAttr(String type, ExrAttr.Factory factory) {
        this.attrFactories.put(type, factory);
    }

    protected void registerAttrs() {
        registerAttr("int", IntAttr::new);
        registerAttr("float", FloatAttr::new);
        registerAttr("double", DoubleAttr::new);
        registerAttr("half", HalfAttr::new);
        registerAttr("box2i", Box2iAttr::new);
        registerAttr("box2f", Box2fAttr::new);
        registerAttr("bytes", BytesAttr::new);
        registerAttr("chlist", ChlistAttr::new);
        registerAttr("chromaticities", ChromaticitiesAttr::new);
        registerAttr("compression", CompressionAttr::new);
        registerAttr("envmap", EnvMapAttr::new);
        registerAttr("keycode", KeycodeAttr::new);
        registerAttr("lineOrder", LineOrderAttr::new);
        registerAttr("m33f", M33fAttr::new);
        registerAttr("m44f", M44fAttr::new);
        registerAttr("preview", PreviewAttr::new);
        registerAttr("rational", RationalAttr::new);
        registerAttr("string", StringAttr::new);
        registerAttr("stringvector", StringVectorAttr::new);
        registerAttr("tiledesc", TileDescAttr::new);
        registerAttr("timecode", TimecodeAttr::new);
        registerAttr("v2i", V2iAttr::new);
        registerAttr("v2f", V2fAttr::new);
        registerAttr("v3i", V3fAttr::new);
        registerAttr("v3f", V3fAttr::new);
    }
}
