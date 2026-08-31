package com.vke.core.color;

import com.carrotsearch.hppc.IntStack;
import com.vke.api.assets.AssetHandle;
import com.vke.core.Context;
import com.vke.core.assets.service.AssetManager;
import com.vke.core.geom.GeomUtils;
import com.vke.core.services2.Services;
import com.vke.impl.color.HslColor;

import java.io.IOException;
import java.util.Arrays;

public abstract class Color {
    public Color(RgbColor rgbSource) {}
    public Color(float[] components) {
        setComponents(components);
    }

    public abstract float[] getComponents();
    public abstract void setComponents(float[] components);
    public abstract Color copy();

    public abstract RgbColor toRgb();

    @SuppressWarnings("all")
    //idk i challenged myself to do this in 1 function lmao enjoy this beautiful code
    public static Color parse(Context ctx, String value) throws IllegalArgumentException {
        int task = 0; //1 = parse float into target, 2 = parse string into return2, 3 = skip whitespace, 4 = return asset color from return2
        int state = 0; //0=parsing 1=task 2=taskreturn
        int target = 0; //0=return1 10..=20 = digits
        IntStack caller = new IntStack();
        float percentMax = 0;

        float return1 = 0;
        String return2 = null;

        float[] digits = null;
        boolean floatMode = false;
        int rawRgbIndex = -1;

        char[] chars = value.toCharArray();

        top:
        for (int i = 0; i < chars.length || state == 2; i++) {
            int currentCaller = state == 2 ? caller.peek() : 0;

            if (i == chars.length && state == 2) {
                if (currentCaller == 2) {
                    task = 4;
                    state = 1;
                    i -= 2;
                    caller.push(-1);
                    continue top;
                }
                if (currentCaller == 10) {
                    break top;
                }
            }

            if (state == 1 || currentCaller == 1) {
                if (task == 1 || currentCaller == 1) {
                    if (state == 1) {
                        task = 2;
                        caller.push(1);
                        state = 1;
                        i--;
                        continue top;
                    } else {
                        caller.discard();
                        try {
                            float parsed = Float.parseFloat(return2);
                            if (i != chars.length) {
                                char c = chars[i];
                                if (c == '%') {
                                    i++;
                                    parsed = (parsed / 100f) * percentMax;
                                } else {
                                    if (parsed < 1 && parsed != 0) {
                                        floatMode = true;
                                    }
                                }
                            } else {
                                if (parsed < 1 && parsed != 0) {
                                    floatMode = true;
                                }
                            }

                            if (target == 0) {
                                return1 = parsed;
                            } else if (target >= 10 && target <= 20) {
                                digits[target - 10] = parsed;
                            }
                            state = 2;
                        } catch (Exception e) {
                            throw new IllegalArgumentException(String.format("illegal color: %s -> illegal number -> %s", value, e.getMessage()));
                        }

                        if (chars.length != i && chars[i] == 'f') {
                            i++;
                            floatMode = true;
                        }

                        i--;
                        continue top;
                    }
                } else if (task == 2) {
                    char c = chars[i];
                    StringBuilder builder = new StringBuilder();
                    for (; i < chars.length; i++) {
                        c = chars[i];
                        if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.' || c == ':') {
                            builder.append(c);
                        } else {
                            break;
                        }
                    }
                    return2 = builder.toString();
                    i--;
                    state = 2;
                    continue top;
                } else if (task == 3) {
                    if (chars.length == i) continue top;
                    char c = chars[i];
                    if (!Character.isWhitespace(c)) i++;
                    for (; i < chars.length && Character.isWhitespace(c); i++) {
                        c = chars[i];
                    }
                    i -= 2;
                    state = 2;
                    continue top;
                } else if (task == 4) {
                    String word = return2;
                    System.out.println(word);
                    AssetManager assets = ctx.service(Services.ASSET_MANAGER);
                    AssetHandle<Color> color = assets.getAsset(word);
                    try {
                        return color.acquire(ctx);
                    } catch (IOException e) {
                        throw new IllegalArgumentException(String.format("illegal color: %s -> color asset was not found!", value));
                    }
                }
            }

            char c = chars[i];
            boolean neg = false;

            if (c == '#' || (c == '0' && i + 1 != chars.length && Character.toLowerCase(chars[i + 1]) == 'x')) {
                if (c == '0') {
                    i++;
                }
                int hexlen = chars.length - i - 1;
                int[] hexdigits = new int[hexlen % 4 == 0 ? 4 : 3];
                boolean two = hexlen > 4;
                int buf = -1;

                int d = 0;
                for (++i; i < chars.length; i++) {
                    c = Character.toLowerCase(chars[i]);

                    char base;
                    if (c >= '0' && c <= '9') {
                        base = '0';
                    } else if (c >= 'a' && c <= 'f') {
                        base = 'a' - 10;
                    } else {
                        throw new IllegalArgumentException(String.format("illegal color: %s -> char %s is NOT a hex literal \uD83E\uDD40 ✌\uFE0F", value, c));
                    }

                    if (two) {
                        if (buf == -1) {
                            buf = (c - base) * 16;
                            continue;
                        }
                        hexdigits[d++] = buf + (c - base);
                        buf = -1;
                    } else {
                        int v = (c - base);
                        hexdigits[d++] = v * 16 + v;
                    }
                }

                if (hexdigits.length == 4) {
                    return new RgbColor(hexdigits[0] / 255f, hexdigits[1] / 255f, hexdigits[2] / 255f, hexdigits[3] / 255f);
                } else {
                    return new RgbColor(hexdigits[0] / 255f, hexdigits[1] / 255f, hexdigits[2] / 255f);
                }
            } else if (c == '-') {
                neg = true;
            } else if (Character.isDigit(c) || currentCaller == 10 || currentCaller == 11) {
                if (rawRgbIndex == -1) {
                    rawRgbIndex = 0;
                }

                if (state == 0) {
                    task = 1;
                    state = 1;
                    caller.push(10);
                    target = 10 + rawRgbIndex++;
                    if (digits == null) {
                        digits = new float[1];
                    } else {
                        digits = Arrays.copyOf(digits, rawRgbIndex + 1);
                    }
                    i--;
                    continue top;
                } else if (state == 2 && currentCaller == 10) {
                    task = 3;
                    caller.discard();
                    state = 1;
                    caller.push(11);
                    i--;
                    continue top;
                } else if (currentCaller == 11) {
                    caller.discard();
                    state = 0;
                    i--;
                }
            } else if (Character.isLetter(c) || currentCaller == 2 || currentCaller == 3 || currentCaller == 4) {
                if (state == 0) {
                    task = 2;
                    caller.push(2);
                    state = 1;
                    i--;
                    continue top;
                } else {
                    if (currentCaller == 4) {
                        caller.discard();
                        if (c != ')') {
                            throw new IllegalArgumentException(String.format("illegal color: %s -> unclosed function", value));
                        }

                        int function = caller.pop();
                        switch (function) {
                            case 1 -> {
                                if (!floatMode) {
                                    for (int j = 0; j < digits.length; j++) {
                                        digits[j] /= 255f;
                                    }
                                }
                                return new RgbColor(digits[0], digits[1], digits[2]);
                            }
                            case 2 -> {
                                if (!floatMode) {
                                    for (int j = 0; j < digits.length; j++) {
                                        digits[j] /= 255f;
                                    }
                                }
                                return new RgbColor(digits);
                            }
                            case 3 -> {
                                float h = (digits[0] / 360f) * GeomUtils.PI2F;
                                float s = digits[1];
                                float l = digits[2];
                                return new HslColor(new float[] { h, s, l, 1 });
                            }
                            case 4 -> {
                                float h = (digits[0] / 360f) * GeomUtils.PI2F;
                                float s = digits[1];
                                float l = digits[2];
                                float a = digits[3];
                                return new HslColor(new float[] { h, s, l, a });
                            }
                            case 5 -> {
                                float gray = digits[0];
                                return new RgbColor(gray, gray, gray);
                            }
                        }
                    } else if (state == 2) {
                        if (c == '(' || currentCaller == 3 || currentCaller == 4) {
                            int call = 0;
                            int index = 0;
                            if (currentCaller == 3) {
                                caller.discard();
                                call = caller.pop();
                                index = caller.pop();
                            }

                            int n;
                            if (call == 1 || "rgb".equals(return2)) {
                                n = 3;
                                call = 1;
                            } else if (call == 2 || "rgba".equals(return2)) {
                                n = 4;
                                call = 2;
                            } else if (call == 3 || "hsl".equals(return2)) {
                                n = 3;
                                call = 3;
                            } else if (call == 4 || "hsla".equals(return2)) {
                                n = 4;
                                call = 4;
                            } else if (call == 5 || "grayscale".equals(return2)) {
                                n = 1;
                                call = 5;
                            } else {
                                throw new IllegalArgumentException(String.format("illegal color: %s -> function %s is not a color function \uD83D\uDC80", value, return2));
                            }

                            if (currentCaller == 2) {
                                caller.discard();
                                digits = new float[n];
                                caller.push(1, call, 3);
                                task = 1;
                                state = 1;
                                target = 10;
                                percentMax = 255f;
                                if (call == 2 || call == 3) {
                                    percentMax = 360f;
                                }
                                continue top;
                            } else {
                                if (index == n) {
                                    caller.push(call, 4);
                                    task = 3;
                                    state = 1;
                                    i--;
                                    continue top;
                                } else {
                                    for (; i < chars.length && !Character.isDigit(c) && c != '-'; i++) {
                                        c = chars[i];
                                    }
                                    i--;

                                    caller.push(index + 1, call, 3);
                                    task = 1;
                                    state = 1;
                                    i--;
                                    target = 10 + index;
                                    percentMax = floatMode ? 1f : 255f;
                                    if (call == 2 || call == 3) {
                                        percentMax = 1f;
                                    }
                                    continue top;
                                }
                            }
                        } else {
                            task = 4;
                            caller.push(-1);
                            continue top;
                        }
                    }
                }
            }
        }

        if (!floatMode) {
            for (int j = 0; j < digits.length; j++) {
                digits[j] /= 255f;
            }
        }

        if (digits.length == 4) {
            return new RgbColor(digits[0], digits[1], digits[2], digits[3]);
        } else if (digits.length == 3) {
            return new RgbColor(digits[0], digits[1], digits[2]);
        }
        return null;
    }
}
