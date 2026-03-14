package com.vke.api.language;

import com.vke.core.VKEngine;
import com.vke.utils.Identifier;
import com.vke.utils.Location;
import com.vke.utils.iter.Iter;

import java.nio.file.Path;

public interface Str {
    String getContents(VKEngine engine);

    default Iter<Character> chars(VKEngine engine) {
        String s = getContents(engine);
        return Iter.of(s.chars().boxed()).cast();
    }

    static Str STATIC(String content) {
        return new StaticString(content);
    }

    static Str MULTILINGUAL(Location key) {
        return new MultilingualString(key);
    }

    static Str ASSET(Identifier identifier) {
        return new AssetString(identifier);
    }
}
