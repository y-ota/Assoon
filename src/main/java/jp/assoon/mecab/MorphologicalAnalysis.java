package jp.assoon.mecab;

import java.nio.file.Path;

public interface MorphologicalAnalysis extends AutoCloseable{

   void execute(Path inputFilePath, Path outputFilePath) ;
}
