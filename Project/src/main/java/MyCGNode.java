import com.ibm.wala.classLoader.IClass;

import java.util.ArrayList;

public class MyCGNode {
    boolean isTest;
    ArrayList<MyCGNode> succNodes=new ArrayList<MyCGNode>();
    ArrayList<MyCGNode> predNodes=new ArrayList<MyCGNode>();
    String classStr;
    String methodStr;

    public MyCGNode(String c,String m,boolean it){
        isTest=it;
        classStr=c;
        methodStr=m;
    }

    public String getClassStr() {
        return classStr;
    }

    public String getMethodStr() {
        return methodStr;
    }
    public void addPredNode(MyCGNode predNode){
        predNodes.add(predNode);
    }
    public void addSuccNode(MyCGNode succNode){
        succNodes.add(succNode);
    }
    public boolean isATest(){
        return isTest;
    }

    public ArrayList<MyCGNode> getPredNodes() {
        return predNodes;
    }

    public ArrayList<MyCGNode> getSuccNodes() {
        return succNodes;
    }
}
