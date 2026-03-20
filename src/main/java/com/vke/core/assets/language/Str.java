package com.vke.core.assets.language;

import com.vke.core.VKEngine;
import com.vke.utils.io.Identifier;
import com.vke.utils.io.SegmentedPath;
import com.vke.utils.iter.Iter;

public interface Str {
    String getContents(VKEngine engine);

    default Iter<Character> chars(VKEngine engine) {
        String s = getContents(engine);
        return Iter.of(s.chars().boxed()).cast();
    }

    static Str STATIC(String content) {
        return new StaticString(content);
    }

    static Str MULTILINGUAL(SegmentedPath key) {
        return new MultilingualString(key);
    }

    static Str ASSET(Identifier identifier) {
        return new AssetString(identifier);
    }
}
