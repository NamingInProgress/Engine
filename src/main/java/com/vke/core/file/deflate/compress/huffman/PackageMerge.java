package com.vke.core.file.deflate.compress.huffman;

import com.carrotsearch.hppc.ObjectArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

//my implementation of the package-merge algorithm to generate length-limited huffman codes
public class PackageMerge {

    //returns the code length for each frequency
    public static int[] perform(int[] frequencies, int maxCodeLength) {
        List<UsedSymbol> usedSymbols = new ArrayList<>();
        for (int i = 0; i < frequencies.length; i++) {
            int f = frequencies[i];
            if (f > 0) {
                usedSymbols.add(new UsedSymbol(i, f));
            }
        }

        //special case when only one symbol is used
        if (usedSymbols.size() == 1) {
            int[] codeLengths = new int[frequencies.length];
            codeLengths[usedSymbols.getFirst().symbol()] = 1; // force minimum length 1
            return codeLengths;
        }

        //this is a mapping to preserve the original oder of the symbol array
        //because after sort and 0 filtering, the order is kinda random and not really usable
        usedSymbols.sort(Comparator.comparingInt(UsedSymbol::frequency));

        int[] symbolMap = new int[usedSymbols.size()];
        for (int i = 0; i < usedSymbols.size(); i++) {
            symbolMap[i] = usedSymbols.get(i).symbol();
        }

        int[] usedFrequencies = new int[usedSymbols.size()];
        Elem[] previousStep = new Elem[usedSymbols.size()];
        for (int i = 0; i < usedSymbols.size(); i++) {
            int v = usedSymbols.get(i).frequency();
            usedFrequencies[i] = v;
            previousStep[i] = new Elem(v, false, i);
        }

        //maxlength -1 iterations needed for package merge
        ObjectArrayList<Elem[]> stages = new ObjectArrayList<>(maxCodeLength - 1);
        for (int i = 0; i < maxCodeLength - 1; i++) {
            stages.add(previousStep.clone());
            previousStep = packageAndMergeOnce(usedFrequencies, previousStep);
        }
        stages.add(previousStep.clone());

        int iterations = 2 * usedFrequencies.length - 2;
        int[] codeLengths = new int[frequencies.length];
        //bottom up iteration of stages
        for (int stageIdx = stages.size() - 1; stageIdx >= 0; stageIdx--) {
            Elem[] stage = stages.get(stageIdx);
            int numMerged = 0;
            for (int i = 0; i < iterations; i++) {
                Elem e = stage[i];
                if (e.wasMerged()) {
                    numMerged++;
                } else {
                    //obtain the original symbol index from the mapping
                    //System.out.println("si: " + e.symbolIndex + ", mapped: " + symbolMap[e.symbolIndex]);
                    codeLengths[symbolMap[e.symbolIndex]]++;
                }
            }

            iterations = 2 * numMerged;
        }

        return codeLengths;
    }

    private static Elem[] packageAndMergeOnce(int[] original, Elem[] step) {
        ObjectArrayList<Elem> out = new ObjectArrayList<>(original.length);
        for (int i = 0; i < original.length; i++) out.add(new Elem(original[i], false, i));
        for (int i = 0; i < step.length; i += 2) {
            //skip last element
            if (i + 1 >= step.length) continue;
            Elem e1 = step[i];
            Elem e2 = step[i + 1];

            Elem merged = new Elem(e1.frequency + e2.frequency, true, -1);
            int insertionPoint = Arrays.binarySearch(out.buffer, 0, out.elementsCount, merged, Comparator.comparingInt(a -> ((Elem) a).frequency));
            if (insertionPoint < 0) {
                //insert where the ordering makes a lot of sense
                insertionPoint = ~insertionPoint;
            } else {
                //insert after the found element of original
                insertionPoint++;
            }
            out.insert(insertionPoint, merged);
        }
        return out.toArray(Elem.class);
    }

    private record Elem(int frequency, boolean wasMerged, int symbolIndex) {
        @Override
        public String toString() {
            return frequency + (wasMerged ? "!" : "");
        }
    }
}
