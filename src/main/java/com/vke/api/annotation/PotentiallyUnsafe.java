package com.vke.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** The user has to fully understand the annotated object because wrong usage will most likely crash the program.
 */
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface PotentiallyUnsafe {
}
