package com.vke.utils;

import static com.vke.utils.AnsiColors.*;

@SuppressWarnings("all")
public class ColorStringBuilder {



    private StringBuilder builder;

    private String previous;

    public ColorStringBuilder(String message) {
        builder = new StringBuilder(message);
        previous = RESET;
    }

    public ColorStringBuilder() {
        this("");
    }

    public ColorStringBuilder write(Object message) {
        builder.append(message);
        return this;
    }

    public ColorStringBuilder line(String command, Object message) {
        return line(command, message, false);
    }

    public ColorStringBuilder line(String command, Object message, boolean swapToPreviousCommand) {
        builder.append(command).append(message);
        if (swapToPreviousCommand) builder.append(previous);
        previous = command;
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    public ColorStringBuilder reset() {
        builder.append(RESET);
        return this;
    }

    public ColorStringBuilder reset(Object message) {
        builder.append(RESET).append(message);
        return this;
    }

    public ColorStringBuilder defaultColor() {
        builder.append(DEFAULT);
        return this;
    }

    public ColorStringBuilder backgroundDefaultColor() {
        builder.append(BACKGROUND_DEFAULT);
        return this;
    }

    public ColorStringBuilder clear() {
        builder = new StringBuilder();
        previous = RESET;
        return this;
    }

    // region Color Setters
    public ColorStringBuilder rgb(Object message, int r, int g, int b) {
        return line(AnsiColors.rgb(r, g, b), message);
    }

    public ColorStringBuilder black(Object message) {
        return line(BLACK, message);
    }
    public ColorStringBuilder red(Object message) {
        return line(RED, message);
    }
    public ColorStringBuilder green(Object message) {
        return line(GREEN, message);
    }
    public ColorStringBuilder yellow(Object message) {
        return line(YELLOW, message);
    }
    public ColorStringBuilder blue(Object message) {
        return line(BLUE, message);
    }
    public ColorStringBuilder magenta(Object message) {
        return line(MAGENTA, message);
    }
    public ColorStringBuilder cyan(Object message) {
        return line(CYAN, message);
    }
    public ColorStringBuilder white(Object message) {
        return line(WHITE, message);
    }
    public ColorStringBuilder gray(Object message) { return line(GRAY, message); }
    // endregion
    // region Background Color Setters
    public ColorStringBuilder backgroundRGB(Object message, int r, int g, int b) {
        return line(AnsiColors.backgroundRGB(r, g, b), message);
    }

    public ColorStringBuilder backgroundBlack(Object message) {
        return line(BACKGROUND_BLACK, message);
    }
    public ColorStringBuilder backgroundRed(Object message) {
        return line(BACKGROUND_RED, message);
    }
    public ColorStringBuilder backgroundGreen(Object message) {
        return line(BACKGROUND_GREEN, message);
    }
    public ColorStringBuilder backgroundYellow(Object message) {
        return line(BACKGROUND_YELLOW, message);
    }
    public ColorStringBuilder backgroundBlue(Object message) {
        return line(BACKGROUND_BLUE, message);
    }
    public ColorStringBuilder backgroundMagenta(Object message) {
        return line(BACKGROUND_MAGENTA, message);
    }
    public ColorStringBuilder backgroundCyan(Object message) {
        return line(BACKGROUND_CYAN, message);
    }
    public ColorStringBuilder backgroundWhite(Object message) {
        return line(BACKGROUND_WHITE, message);
    }
    // endregion
    // region Graphics Mode Setters
    public ColorStringBuilder bold(boolean on) {
        builder.append(on ? BOLD : RESET_BOLD);
        return this;
    }
    public ColorStringBuilder italic(boolean on) {
        builder.append(on ? ITALIC : RESET_ITALIC);
        return this;
    }
    public ColorStringBuilder underline(boolean on) {
        builder.append(on ? UNDERLINE : RESET_UNDERLINE);
        return this;
    }
    public ColorStringBuilder blinking(boolean on) {
        builder.append(on ? BLINKING : RESET_BLINKING);
        return this;
    }
    public ColorStringBuilder strikethrough(boolean on) {
        builder.append(on ? STRIKETHROUGH : RESET_STRIKETHROUGH);
        return this;
    }
    // endregion

}
