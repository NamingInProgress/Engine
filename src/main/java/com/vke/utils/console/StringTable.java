package com.vke.utils.console;

import com.vke.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringTable {
    private final List<List<String>> columns;
    private List<String> currentRow;

    public StringTable() {
        columns = new ArrayList<>();
    }
    
    public void tr() {
        List<String> l = new ArrayList<>();
        columns.add(l);
        currentRow = l;
    }

    public void td(Object content) {
        td(content, 1);
    }

    public void td(Object content, int colspan) {
        currentRow.add(content.toString());
        for (int i = 1; i < colspan; i++) {
            currentRow.add("");
        }
    }

    public String construct() {
        return construct(null);
    }

    public String construct(char[] paddingDirs) {
        StringBuilder result = new StringBuilder();
        String crlf = System.lineSeparator();

        int[] columnWidths = new int[0];
        for (List<String> column : columns) {
            for (int i = 0; i < column.size(); i++) {
                if (!Utils.verifyArrayIndex(i, columnWidths.length)) {
                    columnWidths = Arrays.copyOf(columnWidths, i + 1);
                }

                int cw = columnWidths[i];
                String entry = column.get(i);
                int nowLength = entry.length();
                if (nowLength > cw) {
                    columnWidths[i] = nowLength;
                }
            }
        }

        outer:
        for (List<String> column : columns) {
            for (int i = 0; i < columnWidths.length; i++) {
                int width = columnWidths[i];
                if (!Utils.verifyArrayIndex(i, column.size())) {
                    continue outer;
                }
                char paddingDir = paddingOrDefault(paddingDirs, i);
                String colValue;
                if (paddingDir == 'l') {
                    colValue = Utils.lpad(column.get(i), ' ', width + 1); //+1 cuz of space betweeen things
                } else {
                    colValue = Utils.rpad(column.get(i), ' ', width + 1); //+1 cuz of space betweeen things
                }

                result.append(colValue);

                if (i + 1 == columnWidths.length) {
                    result.append(crlf);
                }
            }
        }
        
        return result.toString();
    }

    private char paddingOrDefault(char[] paddings, int column) {
        if (paddings != null && Utils.verifyArrayIndex(column, paddings.length)) return paddings[column];
        return 'r';
    }
}
