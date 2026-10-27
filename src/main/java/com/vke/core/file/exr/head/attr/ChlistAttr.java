package com.vke.core.file.exr.head.attr;

import com.vke.core.file.exr.head.ExrChannel;
import com.vke.core.file.utils.DataUtils;
import com.vke.utils.io.BytesInFrontOfInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class ChlistAttr implements ExrAttr {
    public final HashMap<String, ExrChannel> channels;

    public ChlistAttr(InputStream stream, int ignoredSize) throws IOException {
        this.channels = new HashMap<>();
        int next = DataUtils.readU8(stream);
        while (next > 0) {
            InputStream channelStream = new BytesInFrontOfInputStream(stream, (byte) next);
            ExrChannel channel = new ExrChannel(channelStream);
            channels.put(channel.name(), channel);
            next = DataUtils.readU8(stream);
        }
    }
}
