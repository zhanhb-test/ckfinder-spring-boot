package com.github.zhanhb.ckfinder.download;

import java.nio.file.Path;
import java.util.function.Function;
import javax.annotation.Nullable;

@SuppressWarnings({"WeakerAccess", "MarkerInterface"})
public interface NameMapper extends Function<Path, String> {

  @Nullable
  @Override
  String apply(Path t);

}
