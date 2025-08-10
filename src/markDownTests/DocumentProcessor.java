package markDownTests;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class DocumentProcessor {
  private final List<Chapter> chapters = new ArrayList<>();
  static List<Path> walk(Path root,String extension){
    try (var s = Files.walk(root)){ return s.filter(ends(extension)).toList(); }
    catch (IOException e){ throw new UncheckedIOException(e); }
  }
  static Predicate<Path> ends(String extension){ return p -> (p+"").endsWith(extension); }

  public List<Chapter> processFiles(Path root){ return processFiles(walk(root,".java")); }
  
  public List<Chapter> processFiles(List<Path> filePaths) {
    filePaths.stream()
      .map(MarkDownTest::new)
      .map(MarkDownTest::extractMarkdownLines)
      .flatMap(List::stream)
      .forEach(this::processLine);
    return Collections.unmodifiableList(chapters);
  }
  private void processLine(String line) {
    if (TextTag.Chapter.match(line)){ newChapter(line); return;}
    if (TextTag.Section.match(line)){ newSection(line); return;}
    var s= chapters.getLast().sections().getLast();
    s.lines().add(line);
  }
  private void newChapter(String line){
    var title= line.replace(TextTag.Chapter.token(),"").trim();
    chapters.add(new Chapter(title));
  }
  private void newSection(String line){
    var title= line.replace(TextTag.Section.token(),"").trim();
    chapters.getLast().sections().add(new Chapter.Section(title));
  }
}