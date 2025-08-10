package markDownTests;

import java.util.ArrayList;
import java.util.List;

record Chapter(String title, List<Section> sections) {
  public Chapter(String title){ this(title, new ArrayList<>()); }
  record Section(String title, List<String> lines) {
    public Section(String title){ this(title, new ArrayList<>()); }
  }
}