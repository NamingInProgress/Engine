package com.vke;

import com.vke.annotations.Test;
import com.vke.assertions.Assertions;

public class SomeTest {

    @Test
    public void myTest() {
        Assertions.assertEquals(true, true);
    }

}
