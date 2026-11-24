package com.vke.core.file.deflate.decompress.huffman;

import com.vke.api.rendering.vulkan.buffer.CpuBuffer;
import com.vke.core.file.io.bit.BitOrdering;
import com.vke.utils.IntUtils;

import java.util.Arrays;

public class HuffmanBinaryTree {
    private int[] value;
    private int[] left;
    private int[] right;
    private int length;

    private int nextNode = 1;

    public HuffmanBinaryTree(Code[] codes, BitOrdering ordering) {
        this.value = new int[1];
        this.left = new int[1];
        this.right = new int[1];

        this.length = 1;

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
        if (index >= length || left[index] == 0) {
            int newIdx = nextNode++;
            grow(newIdx + 1);
            left[index] = newIdx;
        }
        return left[index];
    }

    private int goRight(int index) {
        if (index >= length || right[index] == 0) {
            int newIdx = nextNode++;
            grow(newIdx + 1);
            right[index] = newIdx;
        }
        return right[index];
    }

    private void setValue(int index, int value) {
        if (index >= length) {
            grow(index + 1);
        }
        this.value[index] = value;
    }

    private void grow(int minimum) {
        int cap = value.length + 1;

        while (cap < minimum) {
            cap = IntUtils.mulByDouble(cap, CpuBuffer.GROWTH_FAC);
        }

        this.value = Arrays.copyOf(this.value, cap);
        this.left = Arrays.copyOf(this.left, cap);
        this.right = Arrays.copyOf(this.right, cap);
        this.length = minimum;
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
        return index < length && left[index] == 0 && right[index] == 0;
    }

    private int extractBit(int bits, int index) {
        return (bits >>> index) & 1;
    }
}
