package testHelpers;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

import markDownTests.TextTag;
import utils.Err;

public class TourHelper {
  static{ Err.setUp(AssertionFailedError.class, Assertions::assertEquals, Assertions::assertTrue); }
  protected static void strCmp(String expected, String got){ Err.strCmp(expected, got); }
  private static Path prefix=Path.of("C:\\").resolve("Users","Lardo","OneDrive","Documents","GitHub");
  static public final Path std= prefix.resolve("StandardLibrary","fearlessArtefact","fearless","app","stdLib");
  static public final Path stdBase= std.resolve("base");
  static public final Path stdRt= std.resolve("rt");
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
