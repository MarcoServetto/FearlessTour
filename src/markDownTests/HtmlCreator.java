package markDownTests;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.ext.tables.TablesExtension;

public record HtmlCreator(Path rootPath){
  private static final String HTML_HEADER = """
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="UTF-8">
      <title>%s</title>
      <link rel="stylesheet" href="styles.css">
    </head>
    <body>
      <div id="sidebar">
      %s
      </div>
      <div id="content" class="markdown-content">
    """;
  private static final String HTML_FOOTER = """
          <div class="nav-links">
          %s %s
          </div>
        </div>
      </body>
    </html>
    """;
  private List<SectionInfo> collectSections(List<Chapter> chapters) {
    List<SectionInfo> sections= new ArrayList<>();
    int chapterNum= 1;
    for (var chapter : chapters){
      int sectionNum= 1;
      for (var section : chapter.sections()) {
        sections.add(new SectionInfo(chapter.title(), section.title(), chapterNum, sectionNum, section.lines()));
        sectionNum++;
      }
      chapterNum++;
    }
    return sections;
  }
  private String generateSidebar(List<SectionInfo> sections) {
    StringBuilder sideBar= new StringBuilder();
    for (var s : sections){ sideBar.append(s.sideBarEntry()); }
    return sideBar.toString();
  }
  public void generateHtmlPages(List<Chapter> chapters) {
    List<SectionInfo> allSections = collectSections(chapters);
    String sideBar = generateSidebar(allSections);
    for(int i=0;i<allSections.size();i++){ generatePage(i,sideBar,allSections);}
  }
  private void generatePage(int i, String sideBar, List<SectionInfo> allSections){
    var title=   allSections.get(i).pageTitle();
    var fName=   allSections.get(i).fileName();
    var current= allSections.get(i).generatePage();
    var prev=    Optional.ofNullable(i==0?null:allSections.get(i-1));
    var next=    Optional.ofNullable(i+1==allSections.size()?null:allSections.get(i+1));
    var prevL=   prev.map(SectionInfo::asPrevLink).orElse("<span class=\"disabled\">Previous</span>");
    var nextL=   next.map(SectionInfo::asNextLink).orElse("<span class=\"disabled\">Next</span>");
    var all=     new StringBuilder();
    all.append(String.format(HTML_HEADER, title, sideBar));
    all.append(current);
    all.append(String.format(HTML_FOOTER, prevL, nextL));
    writeFile(fName, all.toString());
  }
  private void writeFile(String fileName, String content){
    Path filePath= rootPath.resolve(fileName);
    try {
      Files.createDirectories(filePath.getParent());
      Files.writeString(filePath, content);
    }
    catch (IOException e){ throw new UncheckedIOException(e); }
  }
}
record SectionInfo(
  String chapterTitle, String sectionTitle,
  int chapterNumber, int sectionNumber, List<String> lines){
  SectionInfo{
    assert chapterNumber <= 99;
    assert sectionNumber <= 99;
  }
  public String fileName(){ return String.format("%02d_%02d.html", chapterNumber, sectionNumber); }
  public String pageTitle(){ return chapterTitle + " :: " + sectionTitle; }
  public String sideBarEntry() {
    String displayName = pageTitle();
    String fileName = fileName();
    String sideBar= "<div class=\"sidebar-entry\"><a href=\"%s\">%s</a></div>\n";
    return String.format(sideBar, fileName, displayName);
  }
  public String asPrevLink(){ return "<a href=\""+fileName()+"\">Previous</a>"; }
  public String asNextLink(){ return "<a href=\""+fileName()+"\">Next</a>"; }

  public String generatePage(){
    String text= lines.stream().collect(Collectors.joining("\n"));
    List<Parser.ParserExtension> extensions = List.of(TablesExtension.create());
    Parser parser = Parser.builder().extensions(extensions).build();
    HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
    return renderer.render(parser.parse(text));    
  }

}
