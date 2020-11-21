import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws ClassHierarchyException, CancelException, InvalidClassFileException, IOException {
        String param = args[0];
        String projectTarget = args[1];
        String changeInfo = args[2];

        //构建分析域
        String scopePath = "scope.txt";
        String exPath = "exclusion.txt";
        ClassLoader classLoader = Main.class.getClassLoader();
        Scoper scoper = new Scoper(scopePath, exPath, classLoader);
        File file = new File(projectTarget);
        if (!file.getName().equals("target")) {
            return;
        }
        File parentFile = file.getParentFile();
        File myDataFile = new File("..\\Report");
        if (!myDataFile.exists()) {
            myDataFile.mkdirs();
        }
        File methodDot = new File(myDataFile + "\\method-" + parentFile.getName() + ".dot");
        createFile(methodDot);
        File classDot = new File(myDataFile + "\\class-" + parentFile.getName() + ".dot");
        createFile(classDot);

        FileOutputStream methodDotFop = new FileOutputStream(methodDot);
        OutputStreamWriter methodDotWriter = new OutputStreamWriter(methodDotFop, "UTF-8");
        FileOutputStream classDotFop = new FileOutputStream(classDot);
        OutputStreamWriter classDotWriter = new OutputStreamWriter(classDotFop, "UTF-8");

        methodDotWriter.append("digraph ").append("method_dot {\n");
        classDotWriter.append("digraph ").append("class_dot {\n");

        scoper.addClassFileByPath(projectTarget);
        AnalysisScope scope = scoper.getAnalysisScope();

        //生成类层次
        ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope);

        //生成进入点
        Iterable<Entrypoint> eps = new AllApplicationEntrypoints(scope, cha);

        //利用CHA算法构建调用图
//        CHACallGraph cg = new CHACallGraph(cha);
//
//        cg.init(eps);

        AnalysisOptions option = new AnalysisOptions(scope, eps);
        SSAPropagationCallGraphBuilder builder = Util.makeZeroCFABuilder(
                Language.JAVA, option, new AnalysisCacheImpl(), cha, scope
        );
        CallGraph cg = builder.makeCallGraph(option);

        // 4.遍历cg中所有的节点
        ArrayList<Pair<String, String>> classEdgesList = new ArrayList<Pair<String, String>>();
        MyCallGraph myCallGraph = new MyCallGraph();
        for (CGNode node : cg) {

            if (node.getMethod() instanceof ShrikeBTMethod) {
                // 一般地，本项目中所有和业务逻辑相关的方法都是ShrikeBTMethod对象
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();

                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {

                    // 获取声明该方法的类的内部表示
                    String classInnerName = method.getDeclaringClass().getName().toString();

                    // 获取方法签名
                    String signature = method.getSignature();

                    // 创建或获取当前CG节点
                    MyCGNode myThisNode;
                    if (!myCallGraph.contains(classInnerName, signature)) {
                        myThisNode = new MyCGNode(classInnerName, signature, signature.contains("Test"));
                        myCallGraph.addCGNode(myThisNode);
                    } else {
                        myThisNode = myCallGraph.getCGNode(classInnerName, signature);

                    }

                    Iterator<CGNode> cgNodeIterator = cg.getPredNodes(node);
                    while (cgNodeIterator.hasNext()) {
                        CGNode predNode = cgNodeIterator.next();
                        // 筛选所需的节点
                        if (!(predNode.getMethod() instanceof ShrikeBTMethod)) {
                            continue;
                        }
                        if (!"Application".equals(predNode.getMethod().getDeclaringClass().getClassLoader().toString())) {
                            continue;
                        }
                        String predSignature = ((ShrikeBTMethod) predNode.getMethod()).getSignature();
                        String predClassInnerName = ((ShrikeBTMethod) predNode.getMethod()).getDeclaringClass().getName().toString();


                        // 创建或获取当前CG节点的前置节点
                        MyCGNode myPredNode;
                        if (!myCallGraph.contains(predClassInnerName, predSignature)) {
                            myPredNode = new MyCGNode(predClassInnerName, predSignature, predClassInnerName.contains("Test"));
                            myCallGraph.addCGNode(myPredNode);
                        } else {
                            myPredNode = myCallGraph.getCGNode(predClassInnerName, predSignature);
                        }

                        // 在myCallGraph里连接两节点
                        connect(myCallGraph, myPredNode, myThisNode);


                        // 输出到方法级别的dot文件
                        String oneEdgeInDot = "\t" + "\"" + signature + "\"" + " -> " + "\"" + predSignature + "\"" + ";\n";
                        methodDotWriter.append(oneEdgeInDot);

                        // 类级别dot文件
                        String predNodesClassInnerName = predNode.getMethod().getDeclaringClass().getName().toString();
                        Pair<String, String> classEdge = new Pair<String, String>(classInnerName, predNodesClassInnerName);
                        if (!classEdgesList.contains(classEdge)) {
                            classEdgesList.add(classEdge);

                        }
                    }

                }
            }
        }

        // 输出到类级别dot文件
        for (Pair<String, String> stringStringPair : classEdgesList) {
            String leftClass = stringStringPair.getKey();
            String rightClass = stringStringPair.getValue();
            String oneEdgeInDot = "\t" + "\"" + leftClass + "\"" + " -> " + "\"" + rightClass + "\"" + ";\n";
            myCallGraph.addCGClassEdge(rightClass, leftClass);
            classDotWriter.append(oneEdgeInDot);

        }

        methodDotWriter.append("}\n");
        methodDotWriter.close();
        methodDotFop.close();

        classDotWriter.append("}\n");
        classDotWriter.close();
        classDotFop.close();

        //测试选择阶段
        File changeInfoFile = new File(changeInfo);

        // 读取变更信息
        BufferedReader reader = null;
        reader = new BufferedReader(new FileReader(changeInfoFile));
        String tmpStr;
        String[] tmpStrList;
        ArrayList<MyCGNode> CINodes = new ArrayList<MyCGNode>();
        while ((tmpStr = reader.readLine()) != null) {
            tmpStrList = tmpStr.split(" ");
            String className = tmpStrList[0];
            String methodName = tmpStrList[1];
            Pair<String, String> CIRecord = new Pair<String, String>(className, methodName);
            CINodes.add(myCallGraph.getCGNode(CIRecord));
        }

        //获取测试用例的方法节点集
        ArrayList<MyCGNode> testNodes = myCallGraph.getTestCGNodes();

        // 结果集
        ArrayList<MyCGNode> selectedMethodTestNodes = new ArrayList<MyCGNode>();
        ArrayList<MyCGNode> selectedClassTestNodes = new ArrayList<MyCGNode>();

        // 方法粒度
        for (MyCGNode testNode : testNodes) {
            for (MyCGNode ciNode : CINodes) {
                // myCallGraph里有test节点和ChangeInfo节点的单向连线，则选择该test节点
                if (myCallGraph.containsMethodEdge(testNode, ciNode)) {
                    selectedMethodTestNodes.add(testNode);
                    break;
                }
            }
        }

        // 类粒度
        // 获取受改变的类的名称（其中包含测试类）
        ArrayList<String> changeClassesNameList = myCallGraph.getChangeClassesName(CINodes);

        // 挑选包含在测试类中的测试方法
        for (MyCGNode testNode : testNodes) {
            if (changeClassesNameList.contains(testNode.getClassStr())) {
                selectedClassTestNodes.add(testNode);
            }
        }


        if (param.equals("-m")) {
            outputSelectedText(selectedMethodTestNodes, new File("selection-method.txt"));
        } else if (param.equals("-c")) {
            outputSelectedText(selectedClassTestNodes, new File("selection-class.txt"));
        }


    }

    static void createFile(File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        } else {
            file.delete();
            file.createNewFile();
        }
    }

    static void connect(MyCallGraph myCallGraph, MyCGNode predNode, MyCGNode succNode) {
        myCallGraph.addCGEdge(predNode, succNode);
        predNode.addSuccNode(succNode);
        succNode.addPredNode(predNode);
    }

    // 写出selection-class/method.txt
    static void outputSelectedText(ArrayList<MyCGNode> selectedNodes, File outputFile) throws IOException {
        createFile(outputFile);
        FileOutputStream fop = new FileOutputStream(outputFile);
        OutputStreamWriter writer = new OutputStreamWriter(fop, "UTF-8");
        for (MyCGNode selectedNode : selectedNodes) {
            String selectedStr = selectedNode.getClassStr() + " " + selectedNode.getMethodStr();
            writer.append(selectedStr).append("\n");
        }
        writer.close();
        fop.close();
    }
}
