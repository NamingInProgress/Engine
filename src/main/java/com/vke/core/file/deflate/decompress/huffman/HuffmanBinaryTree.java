package com.vke.core.file.deflate.decompress.huffman;

import com.carrotsearch.hppc.LongArrayList;

public class HuffmanBinaryTree {
    private final long[] tree;

    public HuffmanBinaryTree(Code[] codes) {
        //TODO: FIX THIS CLASS IS DOESNT SEEM TO WORK WITH THE ADAPTION OF LONG

        LongArrayList buildTree = new LongArrayList();

        for (Code c : codes) {
            int len = c.codeLength();
            if (len == 0) continue;
            int rawCode = c.code();
            int code = Integer.reverse(rawCode) >>> (32 - len);
            int symbol = c.symbol();

            int treeIndex = 0;
            for (int i = 0; i < len; i++) {
                int bit = (code & (1 << i)) >>> i;

                if (treeIndex >= buildTree.size()) {
                    // //node doesnt even exist
                    // //allocated 2 new nodes as children
                    //buildTree.add(0, 0);
                    // //since insert will take up 1 space here, l and r need a + 1 each
                    //int l = buildTree.size() - 1;
                    //int r = buildTree.size();
                    //buildTree.insert(treeIndex, makeNode(l, r));

                    //NEW FIXED VERSION

                    //node doesnt even exist
                    //allocated 2 new nodes as children
                    buildTree.add(0, 0, 0);
                    //since insert will take up 1 space here, l and r need a + 1 each
                    int l = buildTree.size() - 1;
                    int r = buildTree.size();
                    buildTree.insert(treeIndex, makeNode(l, r));
                }

                if (buildTree.buffer[treeIndex] == 0) {
                    //the node does exists but its not initialized yet
                    buildTree.add(0, 0);
                    int l = buildTree.size() - 2;
                    int r = buildTree.size() - 1;
                    buildTree.buffer[treeIndex] = makeNode(l, r);
                }

                long node = buildTree.buffer[treeIndex];

                //nodes should exist in theory
                int oldIndex = treeIndex;
                if (bit == 1) {
                    //go right
                    treeIndex = getRight(node);
                } else {
                    //go left
                    treeIndex = getLeft(node);
                }
            }

            long leafNode = makeLeaf(symbol);
            buildTree.buffer[treeIndex] = leafNode;
        }

        this.tree = buildTree.toArray();
    }

    /**
     * Walks the tree based on the bits from lsb to msb. 1 means go to right and 0 go to left
     * @param bitPath
     * @return
     */
    public int walk(int bitPath) {
        int cursor = 0;
        int index = 0;
        while (true) {
            int bit = (bitPath >>> cursor) & 1;
            long node = tree[index];
            if (isLeaf(node)) {
                //msb set, so this is a leaf
                return getValue(node);
            }
            if (bit == 1) {
                //go right
                index = getRight(node);
            } else {
                //go left
                index = getLeft(node);
            }
            cursor++;
        }
    }

    private int getLeft(long node) {
        return (int) ((node >>> 31) & 0x7FFFFFFF);
    }

    private int getRight(long node) {
        return (int) (node & 0x7FFFFFFF);
    }

    private boolean isLeaf(long node) {
        return (node & 0x8000000000000000L) != 0;
    }

    private int getValue(long node) {
        return (int) (node & 0xFFFFFFFFL);
    }

    private long makeLeaf(int value) {
        return 0x8000000000000000L | (value & 0xFFFFFFFFL);
    }

    private long makeNode(int left, int right) {
        return (((long) (left & 0x7FFFFFFF)) << 31) | ((long) (right & 0x7FFFFFFF));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        printNode(0, 0, builder);
        return builder.toString();
    }

    private void printNode(int nodeIndex, int depth, StringBuilder builder) {
        long node = tree[nodeIndex];
        if (node == 0) {
            builder.append("-".repeat(depth)).append("<invalid>").append(System.lineSeparator());
            return;
        }
        if (isLeaf(node)) {
            builder.append("-".repeat(depth)).append(getValue(node)).append(System.lineSeparator());
        } else {
            printNode(getLeft(node), depth + 1, builder);
            printNode(getRight(node), depth + 1, builder);
        }
    }

    private void testMakeNode(int l, int r) {
        long n = makeNode(l, r);
        int gl = getLeft(n);
        int gr = getRight(n);
        if (gl != l || gr != r) {
            throw new RuntimeException("FAiled!!!!!");
        }
    }
}
