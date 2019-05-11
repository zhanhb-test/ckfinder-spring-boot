package com.github.zhanhb.ckfinder.download;

import java.util.function.Function;

/**
 *
 * @author zhanhb
 */
@SuppressWarnings("MarkerInterface")
public interface Strategy<T> extends Function<PartialContext, T> {
}
