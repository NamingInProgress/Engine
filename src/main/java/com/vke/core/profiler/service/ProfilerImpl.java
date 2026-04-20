package com.vke.core.profiler.service;

import com.vke.api.logger.Logger;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.VKEngine;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.profiler.ProfilerPrinter;
import com.vke.core.services2.Services;
import com.vke.utils.console.AnsiColors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import static com.vke.core.profiler.ProfilerPrinter.Type.*;
import static com.vke.core.profiler.ProfilerPrinter.Type;
import static com.vke.core.profiler.ProfilerPrinter.Settings;

public class ProfilerImpl extends ServiceImpl implements Profiler {

    public static final Logger logger = LoggerFactory.get("Profiler");

    private int enabledDisplayTypes;
    private final HashMap<Type, Settings> settings = new HashMap<>();

    private Node frame;
    private final Stack<Stack<Node>> doubleStack = new Stack<>();
    private Stack<Node> stack = new Stack<>();

    public ProfilerImpl() {
        super(Services.PROFILER);

        VKEngine.profiler = this;

        enabledDisplayTypes = TABLE.asInt() | TREE.asInt();
        settings.put(TABLE, Settings.defaultTable());
        settings.put(TREE, Settings.defaultTree());
    }

    public void beginFrame() {
        stack.clear();
        frame = new Node("Frame", AnsiColors.GOLD);
        stack.push(frame);
    }

    public void endFrame() {
        end();
        if (!stack.isEmpty()) logger.warn("Unclosed Profiler Object!");

        printData(frame, enabledDisplayTypes, settings);
    }

    public void push() {
        doubleStack.push((Stack<Node>) stack.clone());
    }

    public void closeStack() {
        Stack<Node> s = doubleStack.peek();
        for (int i = s.size(); i < stack.size(); i++) {
            end();
        }
    }

    public void pop() {
        stack = doubleStack.pop();
    }

    public void begin(String name) {
        begin(name, stack.peek().color);
    }

    public void begin(String name, String color) {
        Node n = new Node(name, color);
        stack.peek().addChild(n);
        stack.push(n);
    }

    public void end() {
        stack.pop().end();
    }

    private void printData(Node master, int enabledDisplayTypes, HashMap<Type, Settings> settings) {
        StringBuilder sb = new StringBuilder("Performance Statistics: \n");

        for (Type type : Type.values()) {
            if (type.isInMask(enabledDisplayTypes)) sb.append(ProfilerPrinter.getPrinter(master, type, settings.get(type)).format()).append("\n");
        }

        logger.info(sb.toString());
    }

    // region Display type customizers
    public void withDisplayTypes(Type... type) {
        for (Type t : type) {
            enabledDisplayTypes |= t.asInt();
        }
    }

    public void disableDisplayTypes(Type... type) {
        for (Type t : type) {
            enabledDisplayTypes ^= t.asInt();
        }
    }

    public void setSettingsForType(Type type, Settings settings) {
        this.settings.put(type, settings);
    }

    public void defaultSettings(Type type) {
        this.settings.put(type, Settings.fromTypeDefault(type));
    }
    // endregion

    public static class Node {

        public String name, color;
        public long startTime, totalTime;
        public List<Node> children;

        public Node(String name, String color) {
            this.name = name;
            this.color = color;
            this.startTime = System.nanoTime();
        }

        public void end() {
            totalTime = System.nanoTime() - startTime;
        }

        public void addChild(Node n) {
            if (this.children == null) this.children = new ArrayList<>();
            this.children.add(n);
        }

        public long getSelfTime() {
            if (children == null) return totalTime;
            return totalTime - children.stream().mapToLong(Node::getTotalTime).sum();
        }

        public long getTotalTime() { return this.totalTime; }

        public String getObjectType() { return this.children == null ? "OBJ" : "CAT"; }

    }

    @Override
    protected List<String> dependencies() {
        return List.of();
    }

    @Override
    public void free() {}

}
