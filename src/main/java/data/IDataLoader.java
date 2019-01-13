package data;

import java.util.List;

public interface IDataLoader {
    List<Doc> loadDataFromCorpusFolder(String corpusPath);
}
