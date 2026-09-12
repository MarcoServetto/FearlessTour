package testHelpers;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

import java.nio.file.Files;

import resources.ResolveResource;
import markDownTests.TextTag;
import tools.Fs;
import tools.JavacTool;
import utils.Err;

public class TourHelper {
  static{
    Err.setUp(AssertionFailedError.class, Assertions::assertEquals, Assertions::assertTrue);
    System.setProperty(JavacTool.appDirKey, appDir().toString());
  }
  private static Path appDir(){
    var artefactRoot= ResolveResource.stLibPath.getParent().resolve("fearlessArtefact");
    var found= Fs.walk(artefactRoot, s->s
      .filter(Files::isDirectory)
      .filter(p->p.getFileName().toString().equals(JavacTool.deployedModsDirName))
      .filter(p->!Fs.walk(p, s2->s2.filter(f->f.toString().endsWith(".jar")).toList()).isEmpty())
      .toList());
    assert found.size()==1: "Expected exactly one non-empty '"+JavacTool.deployedModsDirName+"' dir under "+artefactRoot+", found: "+found;
    return found.getFirst().getParent();
  }
  protected static void strCmp(String expected, String got){ Err.strCmp(expected, got); }
  static public final Path stdBase= ResolveResource.stLibPath;
  static public final Path stdRt= ResolveResource.stLibRTPath;
  static public final Path out= Path.of("tmpOut");

  public static void run(String code){
    String expectedPrint= printRequirement(code);
    String expectedErr= errRequirement(code);
    if (noMain(code)){ code += "\nSomeAnonMain:base.Main{s->base.Void}\n"; }
    var m= new mainCoordinator.ProgrammaticMain(
      new StringBuilder(),new StringBuilder(),
      "_test/_rank_app111.fear",code,
      stdBase,stdRt,out
      );
    m.runFearless();
    System.err.println("Err was: "+m.err());
    System.out.println("Out was: "+m.out());
    //var a=strView(expectedPrint+"\n");
    //var b=strView(m.out().toString());
    //assertEquals(a,b);
    strCmp(expectedErr, m.err().toString());
    //assertEquals(expectedPrint.length(), m.out().length());//+1 for new line
    strCmp("||"+expectedPrint+"||", "||"+m.out().toString()+"||");
  }
  static List<Byte> strView(String str){
    byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
    return IntStream.range(0, bytes.length)
      .mapToObj(i -> bytes[i]).toList();
  }
  static boolean noMain(String content){
    return List.of(":Main",": Main",":TestMain",": TestMain")
      .stream().noneMatch(e->content.contains(e));
  }
  private static String printRequirement(String text){ return tagRequirement(text,TextTag.PrintReq); }
  private static String errRequirement(String text){ return tagRequirement(text,TextTag.ErrorReq); }

  private static String tagRequirement(String text,TextTag tag){
    return text.lines()
      .filter(l->l.startsWith(tag.token()))
      .map(l->l.substring(tag.token().length())+"\n")
      .collect(Collectors.joining());
  }
  public static void run(String pkgName, String rank, String code){
    var m= new mainCoordinator.ProgrammaticMain(
      new StringBuilder(),new StringBuilder(),
      "_"+pkgName+"/"+rank+".fear",code,
      stdBase,stdRt,out
      );
    m.runFearless();
  }
}
