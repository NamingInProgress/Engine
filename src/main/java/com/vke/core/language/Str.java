package com.vke.core.language;

import com.vke.core.Context;
import com.vke.core.Identifier;
import com.vke.core.VKEngine;
import com.vke.utils.io.SegmentedPath;
import com.vke.utils.iter.Iter;

public interface Str {
    String getContents(Context context);

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

    static Str MULTILINGUAL(String key) {
        return MULTILINGUAL(new SegmentedPath(key, "."));
    }

    static Str ASSET(Identifier identifier) {
        return new AssetString(identifier);
    }
}
