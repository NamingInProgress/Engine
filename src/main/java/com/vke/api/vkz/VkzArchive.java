package com.vke.api.vkz;

import com.vke.api.serializer.Serializer;
import com.vke.core.vkz.VkzObjLoader;
import com.vke.core.vkz.types.imm.VkzImmediateArchive;
import com.vke.core.vkz.types.lo.VkzListOnlyArchive;
import com.vke.utils.iter.Iter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public interface VkzArchive {
    VkzFileHandle file(CharSequence path);

    VkzDirectoryHandle directory(CharSequence path);

    VkzDirectoryHandle root();

    Iter<VkzFileHandle> iterateFiles();

    default void writeOut(OutputStream stream) throws IOException {
        writeOut(stream, ProgressReport.Listener.silent());
    }

    void writeOut(OutputStream stream, ProgressReport.Listener progressListener) throws IOException;

    default WalkingTree tree() {
        return new WalkingTree(root());
    }

    class WalkingTree implements Iterator<Element> {

        private static class Frame {
            VkzDirectoryHandle dir;
            Iterator<VkzDirectoryHandle> dirs;
            Iterator<VkzFileHandle> files;
            int depth;

            Frame(VkzDirectoryHandle dir, int depth) {
                this.dir = dir;
                this.depth = depth;
                this.dirs = dir.iterateDirectories().iterator();
                this.files = dir.iterateFiles().iterator();
            }
        }

        private final Deque<Frame> stack = new ArrayDeque<>();
        private Element next;

        public WalkingTree(VkzDirectoryHandle root) {
            stack.push(new Frame(root, 0));
            advance();
        }

        private void advance() {
            next = null;

            while (!stack.isEmpty()) {
                Frame frame = stack.peek();

                if (frame.dirs.hasNext()) {
                    VkzDirectoryHandle dir = frame.dirs.next();
                    stack.push(new Frame(dir, frame.depth + 1));
                    next = new Element(dir, frame.depth + 1);
                    return;
                }

                if (frame.files.hasNext()) {
                    VkzFileHandle file = frame.files.next();
                    next = new Element(file, frame.depth + 1);
                    return;
                }
                stack.pop();
            }
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Element next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            Element result = next;
            advance();
            return result;
        }
    }

    class Element {
        private final int depth;
        private final Object object;

        private Element(Object object, int depth) {
            this.object = object;
            this.depth = depth;
        }

        public boolean isDir() {
            return object instanceof VkzDirectoryHandle;
        }

        public boolean isFile() {
            return object instanceof VkzFileHandle;
        }

        public VkzDirectoryHandle asDir() {
            return (VkzDirectoryHandle) object;
        }

        public VkzFileHandle asFile() {
            return (VkzFileHandle) object;
        }

        public int getDepth() {
            return depth;
        }

        public String getName() {
            if (isDir()) return asDir().getName();
            if (isFile()) return asFile().getName();
            return "<unknown element>";
        }
    }
}
