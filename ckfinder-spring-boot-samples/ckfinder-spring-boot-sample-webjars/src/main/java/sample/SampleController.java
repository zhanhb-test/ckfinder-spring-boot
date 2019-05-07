package sample;

import com.github.zhanhb.ckfinder.connector.api.BasePathBuilder;
import com.github.zhanhb.ckfinder.connector.support.DefaultPathBuilder;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.download.ContentDispositionStrategy;
import com.github.zhanhb.ckfinder.download.PathPartial;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.HandlerMapping;

/**
 *
 * @author zhanhb
 */
@Controller
public class SampleController {

  private static final Logger log = LoggerFactory.getLogger(SampleController.class);

  private final Path directory = Jimfs.newFileSystem(Configuration.unix()).getRootDirectories().iterator().next();
  private final AntPathMatcher matcher = new AntPathMatcher();
  private final PathPartial viewer = PathPartial.builder()
          .contentDisposition(ContentDispositionStrategy.inline())
          .build();

  // this bean is not required, here we declare it for presentation.
  @Bean
  public BasePathBuilder basePathBuilder(ApplicationContext context) {
    return DefaultPathBuilder.builder()
            .basePath(directory)
            .baseUrl(context.getApplicationName() + "/uploads/")
            .build();
  }

  @GetMapping("uploads/{type}/**")
  public void serveResponse(
          HttpServletRequest request, HttpServletResponse response,
          @PathVariable("type") String type) throws IOException, ServletException {
    final String uri = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
    final String pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
    String more = matcher.extractPathWithinPattern(pattern, uri);
    String pathName = StringUtils.isEmpty(more) ? type : type + "/" + more;
    log.info("serve file {}", pathName);

    Path path = null;
    try {
      // Ignore all dot files and dot directories
      boolean valid = Arrays.stream(pathName.split("[/\\\\]")).allMatch(part -> !part.startsWith("."));
      if (valid) {
        path = FileUtils.resolve(directory, pathName);
      }
    } catch (IllegalArgumentException ex) {
      // special character on windows
    }
    viewer.service(request, response, path);
  }

  @GetMapping("/")
  public String index() {
    return "redirect:/webjars/ckfinder/2.6.2.1/ckfinder.html";
  }

}
