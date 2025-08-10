package markDownTests;

import java.util.Optional;
import java.util.stream.Stream;

public enum TextTag {
  Chapter("--CHAPTER--"),
  Section("--SECTION--"),
  Start("/*START"),
  End("END*/"),
  OmitStart("OMIT_START"),
  OmitEnd("OMIT_END"),
  CodeStart("-----*/"),
  CodeEnd("/*-----");

  private final String token;
  TextTag(String token){ this.token = token; }
  public String token(){ return token; }
  public boolean match(String line){ return line.contains(token); }
  public boolean notMatch(String line){ return !line.contains(token); }
  public static Optional<TextTag> fromLine(String line) {
    return Stream.of(TextTag.values())
      .filter(t->line.contains(t.token)).findFirst();
  }
}
