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
import java.util.function.Function;
import org.unbescape.javascript.JavaScriptEscape;

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
    str = MatcherWrapper.matcher("\\\\x[0-9A-Fa-f]{2}", str).replaceAll(esc(2, 16));
    str = MatcherWrapper.matcher("\\\\u[0-9A-Fa-f]{4}", str).replaceAll(esc(2, 16));
    str = MatcherWrapper.matcher("(?-i)\\\\[0-7]{3}", str).replaceAll(esc(1, 8));
    str = MatcherWrapper.matcher("[^\000-\\x7E\u2028\u2029]++", str).replaceAll(matcher -> esc(matcher.group()));
    if (!str.equals(old)) {
      Files.write(path, str.getBytes(charset));
    }
  }

  private static Function<MatcherWrapper, String> esc(int index, int radix) {
    return m -> esc(
            String.valueOf((char) Integer.parseInt(m.group().substring(index), radix)));
  }

  private static String esc(String str) {
    return JavaScriptEscape.escapeJavaScript(str);
  }

}
