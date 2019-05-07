/*
 * Copyright 2014 zhanhb.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Character.toChars;
import static java.lang.Integer.parseInt;
import static java.util.regex.Matcher.quoteReplacement;
import static org.unbescape.javascript.JavaScriptEscape.escapeJavaScript;

/**
 *
 * Created on 2014-12-25 12:38:12
 *
 * @author zhanhb
 */
public class JSEscaper {

  public static void main(String[] args) throws IOException {
    Path parent = Paths.get(args[0]);
    Files.walk(parent).filter(Files::isRegularFile).filter(f -> f.getFileName().toString().endsWith(".js")).forEach(path -> {
      try {
        resolve(path);
      } catch (IOException ex) {
        throw new UncheckedIOException(ex);
      }
    });
  }

  private static void resolve(Path path) throws IOException {
    Charset charset = StandardCharsets.UTF_8;
    String str = charset.newDecoder().decode(ByteBuffer.wrap(Files.readAllBytes(path))).toString();
    String old = str;
    if (str.startsWith("\ufeff")) {
      str = str.substring(1);
    }
    str = replaceAll("\\\\x[0-9A-Fa-f]{2}", str, esc(2, 16));
    str = replaceAll("\\\\u[0-9A-Fa-f]{4}", str, esc(2, 16));
    str = replaceAll("(?-i)\\\\[0-7]{3}", str, esc(1, 8));
    str = replaceAll("[^\000-\\x7E\u2028\u2029]++", str, matcher -> esc(matcher.group()));
    if (!str.equals(old)) {
      BasicFileAttributes bfa = Files.readAttributes(path, BasicFileAttributes.class);
      Files.write(path, str.getBytes(charset));
      Files.setLastModifiedTime(path, bfa.lastModifiedTime());
    }
  }

  private static Function<Matcher, String> esc(int index, int radix) {
    return m -> esc(new String(toChars(parseInt(m.group().substring(index), radix))));
  }

  private static String esc(String str) {
    return escapeJavaScript(str);
  }

  private static String replaceAll(String pat, String text, Function<Matcher, String> replaceFunction) {
    Objects.requireNonNull(replaceFunction);
    Matcher matcher = Pattern.compile(pat).matcher(text);

    boolean result = matcher.find();
    if (result) {
      @SuppressWarnings("StringBufferWithoutInitialCapacity")
      StringBuffer sb = new StringBuffer();
      do {
        String replacement = Objects.requireNonNull(replaceFunction.apply(matcher));
        result = matcher.appendReplacement(sb, quoteReplacement(replacement)).find();
      } while (result);
      return matcher.appendTail(sb).toString();
    }
    return text;
  }

}
