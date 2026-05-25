package compileHtml;

import java.nio.file.Path;

import mainCoordinator.ResolveResource;

import java.io.IOException;
import markDownTests.DocumentProcessor;
import markDownTests.HtmlCreator;
public class CompileHtml {
  public static void main(String[] args) throws IOException{
    Path root=
            ResolveResource.commonsSrc.getParent().getParent().resolve("FearlessTour","src","chaptersOfZeroToHero"); 
    var dest=     //Path.of("htmlOut");
      root.resolve("..").resolve("..").resolve("..").resolve("ZeroToHero").resolve("src").resolve("assetsGuide");
    var creator=  new HtmlCreator(dest);
    var chapters= new DocumentProcessor().processFiles(root);
    creator.generateHtmlPages(chapters);
    System.out.println("Done");
    }
}