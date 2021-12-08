import groovy.transform.CompileStatic
import org.unbescape.javascript.JavaScriptEscape

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes

@CompileStatic
static def clean(Path path, Charset charset) {
    def str = charset.newDecoder().decode(ByteBuffer.wrap(Files.readAllBytes(path))).toString()
    def old = str
    str[0] == "\ufeff" && (str = str.substring(1))
    str = str.replaceAll($/(?:\\x[0-9A-Fa-f]{2}|\\u[0-9A-Fa-f]{4}|\\[0-7]{3})+/$) {
        String[] it -> JavaScriptEscape.escapeJavaScript(JavaScriptEscape.unescapeJavaScript(it[0]))
    }
    str = str.replaceAll($/[^\u0000-\x7E\u2028\u2029]++/$) {
        String[] it -> JavaScriptEscape.escapeJavaScript(it[0])
    }
    if (str != old) {
        def bfa = Files.readAttributes(path, BasicFileAttributes)
        Files.write(path, str.getBytes(charset))
        Files.setLastModifiedTime(path, bfa.lastModifiedTime())
    }
}

@CompileStatic
static def cleanAll(Path dir) {
    def charset = StandardCharsets.UTF_8
    Files.walk(dir)
            .filter { Files.isRegularFile(it) }
            .filter { it.fileName.toString().endsWith('.js') }
            .forEach { clean(it, charset) }
}

def
        log = log,
        project = project

try {
    String directory = project.build.directory
    cleanAll(Paths.get(directory))
} catch (Throwable t) {
    log.error(t)
    throw t
}
