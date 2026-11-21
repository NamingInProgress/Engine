package com.vke.impl.ecs.text;

import com.vke.api.assets.r.R;
import com.vke.api.font.FontFace;
import com.vke.core.Identifier;
import com.vke.core.assets.handles.LazyAssetHandle;
import com.vke.core.color.RgbColor;
import com.vke.core.ecs.component.Component;
import com.vke.core.rendering.DefaultRenderAssets;
import pl.epsi.EcsComponent;

@EcsComponent
public class BasicTextC implements Component {

    public String[] text;
    public FontFace[] font;
    public int[] selector;
    public RgbColor[] color;
    public int[] fontScale;

    public void setText(int i, String text) {
        this.text[i] = text;
    }

    public void setColor(int i, RgbColor color) {
        this.color[i] = color;
    }

    public void setFontScale(int i, int scale) {
        this.fontScale[i] = scale;
    }

    public void setFont(int i, LazyAssetHandle<FontFace> font) {
        this.font[i] = font.assume(DefaultRenderAssets.getRenderSystem());
    }

    public void setFont(int i, FontFace font) {
        this.font[i] = font;
    }

    public void setSelector(int i, String selector) {
        setSelector(i, font[i].toFlags(selector));
    }

    public void setSelector(int i, int selector) {
        this.selector[i] = selector;
    }

    @Override
    public void initialize(int i) {
        text[i] = "";
        color[i] = RgbColor.WHITE;
        fontScale[i] = 42;
        font[i] = R.fonts.get(Identifier.of("vke:fonts/JetBrainsMono")).assume(DefaultRenderAssets.getRenderSystem());
        selector[i] = font[i].toFlags("R");
    }

}
