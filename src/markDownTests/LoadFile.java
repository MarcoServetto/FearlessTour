package markDownTests;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import tools.Fs;

public class LoadFile{
  public List<String> loadLines(Path name){
    return loadText(name).lines().toList();
  }
  public String loadText(Path name){
    //Path p= startPath().resolve(Path.of(name));
    assert Files.exists(name):"Name "+name+" not found. Visible files are \n"
      +DirectoryStructure.of(name.getParent());
    return Fs.readUtf8(name);
    }
}
class DirectoryStructure {
  public static String of(Path startPath){
    try (Stream<Path> paths= Files.walk(startPath)) {
      return paths
        .filter(pi->!pi.equals(startPath))
        .map(pi->startPath.relativize(pi))
        .mapMulti(DirectoryStructure::single)
        .collect(Collectors.joining());
    }
    catch(IOException ioe){ throw new UncheckedIOException(ioe); }
  }
  private static void single(Path rel,Consumer<String> c) {
    int depth = rel.getNameCount() - 1;
    assert depth>=0;
    c.accept("--|".repeat(depth));
    c.accept(rel.getFileName().toString());
    c.accept("  //Path.of(\"");
    c.accept(formatPath(rel));
    c.accept("\")\n");
  }
  private static String formatPath(Path rel) {
    return StreamSupport.stream(rel.spliterator(), false)
      .map(Path::toString)
      .collect(Collectors.joining("\", \"", "", ""));
  }
}