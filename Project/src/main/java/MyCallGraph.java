import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class MyCallGraph {
    HashMap<Pair<String,String>,MyCGNode> CGHashMap=new HashMap<Pair<String, String>, MyCGNode>();
    ArrayList<Pair<MyCGNode,MyCGNode>> CGEdges=new ArrayList<Pair<MyCGNode, MyCGNode>>();
    ArrayList<Pair<String,String>> CGClassEdges=new ArrayList<Pair<String, String>>();

    public void addCGNode(MyCGNode node){
        CGHashMap.put(new Pair<String, String>(node.getClassStr(),node.getMethodStr()),node);
    }
    public void addCGEdge(MyCGNode predNode,MyCGNode succNode){
        Pair<MyCGNode,MyCGNode> CGEdge=new Pair<MyCGNode, MyCGNode>(predNode,succNode);
        CGEdges.add(CGEdge);
    }
    public void addCGClassEdge(String predClass,String thisClass){
        Pair<String,String> CGClassEdge=new Pair<String, String>(predClass,thisClass);
        CGClassEdges.add(CGClassEdge);
    }

    public boolean contains(String c,String m){
        Pair<String,String> nodeName=new Pair<String, String>(c,m);
        return CGHashMap.containsKey(nodeName);
    }
    public MyCGNode getCGNode(String c,String m){
        Pair<String,String> nodeName=new Pair<String, String>(c,m);
        return CGHashMap.get(nodeName);

    }

    public MyCGNode getCGNode(Pair<String,String> nodeName){
        return CGHashMap.get(nodeName);
    }
    public int getNodeNum(){
        return CGHashMap.size();
    }

    public ArrayList<Pair<MyCGNode, MyCGNode>> getCGEdges() {
        return CGEdges;
    }

    public ArrayList<MyCGNode> getTestCGNodes(){
        ArrayList<MyCGNode> testNodes=new ArrayList<MyCGNode>();
        for(MyCGNode node:CGHashMap.values()){
            if(node.isATest()&&!node.getMethodStr().contains("init")){
                testNodes.add(node);
            }
        }
        return testNodes;
    }

    public boolean containsMethodEdge(MyCGNode sourceNode,MyCGNode targetNode){
        boolean result=false;
        for(int i=0;i<sourceNode.getSuccNodes().size();i++){
            MyCGNode succNode=sourceNode.getSuccNodes().get(i);
            if(succNode==targetNode){
                return true;
            }
            if(containsMethodEdge(sourceNode.getSuccNodes().get(i),targetNode)){
                return true;
            }
        }
        return result;
    }
    public boolean containsClassEdge(MyCGNode sourceNode,String targetClassInnerName){
        boolean result=false;
        for(int i=0;i<sourceNode.getSuccNodes().size();i++){
            MyCGNode succNode=sourceNode.getSuccNodes().get(i);
            if(succNode.getClassStr().equals(targetClassInnerName)){
                return true;
            }
            if(containsClassEdge(succNode,targetClassInnerName)){
                return true;
            }
        }
        return result;
    }

    public ArrayList<String> getChangeClassesName(ArrayList<MyCGNode> CINodes){
        ArrayList<String> changeClassesNameList=new ArrayList<String>();
        for (MyCGNode ciNode : CINodes) {
            String ciClassName=ciNode.getClassStr();
            getOneChangeClassesName(changeClassesNameList, ciClassName);
        }
        return changeClassesNameList;
    }

    void getOneChangeClassesName(ArrayList<String> changeClassesNameList,String className){
        if(!changeClassesNameList.contains(className)){
            changeClassesNameList.add(className);
            for (Pair<String, String> cgClassEdge : CGClassEdges) {
                if (cgClassEdge.getValue().equals(className)) {
                    getOneChangeClassesName(changeClassesNameList, cgClassEdge.getKey());
                }
            }
        }

    }


}
