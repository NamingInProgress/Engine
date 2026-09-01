package com.vke.core.file.deflate.decompress.huffman;

import com.vke.core.file.io.bit.BitOrdering;

import java.util.Arrays;

public class HuffmanBinaryTree {
    private int[] value;
    private int[] left;
    private int[] right;

    private int nextNode = 1;

    public HuffmanBinaryTree(Code[] codes, BitOrdering ordering) {
        this.value = new int[1];
        this.left = new int[1];
        this.right = new int[1];

        for (Code code : codes) {
            int nodeIndex = 0;

            int codebits = code.code();
            int codelen = code.codeLength();
            if (ordering == BitOrdering.LSB_FIRST) {
                codebits = Integer.reverse(codebits) >>> (32 - codelen);
            }

            //l = 0, r = 1
            for (int ci = 0; ci < codelen; ci++) {
                int bit = extractBit(codebits, ci);
                int newIndex;
                if (bit == 1) {
                    newIndex = goRight(nodeIndex);
                } else {
                    newIndex = goLeft(nodeIndex);
                }
                nodeIndex = newIndex;
                if (ci == codelen - 1) {
                    setValue(newIndex, code.symbol());
                }
            }
        }
    }

    private int goLeft(int index) {
        if (index >= left.length || left[index] == 0) {
            int newIdx = nextNode++;
            grow(newIdx + 1);
            left[index] = newIdx;
        }
        return left[index];
    }

    private int goRight(int index) {
        if (index >= right.length || right[index] == 0) {
            int newIdx = nextNode++;
            grow(newIdx + 1);
            right[index] = newIdx;
        }
        return right[index];
    }

    private void setValue(int index, int value) {
        if (index >= this.value.length) {
            grow(index + 1);
        }
        this.value[index] = value;
    }

    private void grow(int minimum) {
        this.value = Arrays.copyOf(this.value, minimum);
        this.left = Arrays.copyOf(this.left, minimum);
        this.right = Arrays.copyOf(this.right, minimum);
    }

    /**
     * Walks the tree based on the specified bit ordering. 1 means go to right and 0 go to left
     */
    public int walk(int bits) {
        int index = 0;
        int bitIndex = 0;
        while (true) {
            if (isLeaf(index)) {
                return value[index];
            }
            int bit = extractBit(bits, bitIndex++);
            if (bit == 1) {
                index = right[index];
            } else {
                index = left[index];
            }

            if (index == 0) {
                throw new IllegalStateException("Invalid bit sequence: non-existent path at index " + index);
            }
        }
    }

    private boolean isLeaf(int index) {
        return index < left.length && left[index] == 0 && right[index] == 0;
    }

    private int extractBit(int bits, int index) {
        return (bits >>> index) & 1;
    }
}
