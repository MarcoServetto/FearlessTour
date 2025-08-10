package markDownTests;

import java.nio.file.Path;
import java.util.List;

public class MarkDownTest {
  public final Path path;
  public final List<String> content;
  private boolean omitting= false;
  private boolean notOmit(String line) {
    if (TextTag.OmitStart.match(line)){ omitting = true; }
    try{ return !omitting; }
    finally{ if (TextTag.OmitEnd.match(line)){ omitting = false; } }
  }

  public MarkDownTest(Path path) {
    this.path = path;
    this.content = new LoadFile().loadLines(path);
  }
  public List<String> extractMarkdownLines() {
    return content.stream()
      .dropWhile(TextTag.Start::notMatch)
      .skip(1)
      .map(this::lineToCode)
      .filter(this::notOmit)
      .takeWhile(TextTag.End::notMatch)
      .toList();
  }
  String lineToCode(String line){
    line= line.replace("*|/", "*/");
    boolean code= TextTag.CodeStart.match(line) || TextTag.CodeEnd.match(line);
    if (code){ return "````"; }
    return line;
  }
}