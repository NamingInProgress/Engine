package com.vke.utils.fi;

public interface FaultyFunction<ARG, RETURN, EXC extends Throwable> {
    RETURN apply(ARG arg) throws EXC;
}
