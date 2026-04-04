package com.vke.core.file.deflate.decompress.huffman;

import com.carrotsearch.hppc.IntArrayList;
import com.vke.core.file.deflate.decompress.BitUtils;

public class HuffmanBinaryTree {
    private final int[] tree;

    public HuffmanBinaryTree(Code[] codes) {
        IntArrayList buildTree = new IntArrayList();

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
                    //node doesnt even exist
                    //allocated 2 new nodes as children
                    buildTree.add(0, 0);
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

                int node = buildTree.buffer[treeIndex];

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

            int leafNode = makeLeaf(symbol);
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
            int node = tree[index];
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

    private int getLeft(int node) {
        return (node >>> 15) & 0x7FFF;
    }

    private int getRight(int node) {
        return node & 0x7FFF;
    }

    private boolean isLeaf(int node) {
        return (node & 0x80000000) != 0;
    }

    private int getValue(int node) {
        return node & 0x7FFFFFFF;
    }

    private int makeLeaf(int value) {
        return 0x80000000 | BitUtils.lowBits(value, 31);
    }

    private int makeNode(int left, int right) {
        return ((left & 0x7FFF) << 15) | (right & 0x7FFF);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        printNode(0, 0, builder);
        return builder.toString();
    }

    private void printNode(int nodeIndex, int depth, StringBuilder builder) {
        int node = tree[nodeIndex];
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
        int n = makeNode(l, r);
        int gl = getLeft(n);
        int gr = getRight(n);
        if (gl != l || gr != r) {
            throw new RuntimeException("FAiled!!!!!");
        }
    }
}
