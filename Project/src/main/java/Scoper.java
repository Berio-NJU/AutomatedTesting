import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.AnalysisScopeReader;

import java.io.File;
import java.io.IOException;

public class Scoper {
    AnalysisScope analysisScope;

    Scoper(String scopePath, String exPath, ClassLoader classLoader) {
        try {
            analysisScope = AnalysisScopeReader.readJavaScope(scopePath, new File(exPath), classLoader);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * @param path
     */
    public void addClassFileByPath(String path) {
        File targetFile = new File(path);
        if (!targetFile.exists()) {
            return;
        }
        File[] tempList = targetFile.listFiles();
        if (tempList != null) {
            for (File file : tempList) {
                if (file.isFile()) {
                    // 若为class文件，添加到分析域中
                    if (file.getName().endsWith("class")) {
                        try {
                            analysisScope.addClassFileToScope(ClassLoaderReference.Application, file);
                        } catch (InvalidClassFileException invalidClassFileException) {
                            invalidClassFileException.printStackTrace();
                        }
                    }
                } else if (file.isDirectory()) {
                    addClassFileByPath(file.getAbsolutePath());
                }
            }
        }
    }

    public AnalysisScope getAnalysisScope() {
        return analysisScope;
    }

    public void printScope(){
        System.out.println("--------------Scope-------------");
        System.out.println(analysisScope);
        System.out.println("--------------Scope-------------");
    }
}
