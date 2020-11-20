
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class Test1 {

    @Test
    public void test0CMDClass() throws IOException {
        String myOutput="E:\\Study\\Grade3_I\\自动化测试\\大作业\\经典大作业\\AutomatedTesting\\Demo\\selection-class.txt";
        String refOutput="E:\\Study\\Grade3_I\\自动化测试\\大作业\\经典大作业\\AutomatedTesting\\Data\\0-CMD\\data\\selection-class.txt";
        assertTrue(compareTwoFiles(myOutput,refOutput));
    }

    @Test
    public void test1ALUClass() throws IOException {
        assertTrue(compareTwoFiles(getMyDataClass("1-ALU"),getDataClass("1-ALU")));
    }

    @Test
    public void test2DLClass() throws IOException {
        assertTrue(compareTwoFiles(getMyDataClass("2-DataLog"),getDataClass("2-DataLog")));
    }

    @Test
    public void test3BHClass() throws IOException {
        assertTrue(compareTwoFiles(getMyDataClass("3-BinaryHeap"),getDataClass("3-BinaryHeap")));
    }

    @Test
    public void test4NextDayClass() throws IOException {
        assertTrue(compareTwoFiles(getMyDataClass("4-NextDay"),getDataClass("4-NextDay")));
    }

    @Test
    public void test5MTClass() throws IOException {
        assertTrue(compareTwoFiles(getMyDataClass("5-MoreTriangle"),getDataClass("5-MoreTriangle")));
    }

    @Test
    public void test0CMDMethod()throws IOException{
        assertTrue(compareTwoFiles(getMyDataMethod("0-CMD"),getDataMethod("0-CMD")));
    }

    @Test
    public void test1ALUMethod()throws IOException{
        assertTrue(compareTwoFiles(getMyDataMethod("1-ALU"),getDataMethod("1-ALU")));
    }

    @Test
    public void test2DataLogMethod()throws IOException{
        assertTrue(compareTwoFiles(getMyDataMethod("2-DataLog"),getDataMethod("2-DataLog")));
    }

    @Test
    public void test3BHMethod()throws IOException{
        assertTrue(compareTwoFiles(getMyDataMethod("3-BinaryHeap"),getDataMethod("3-BinaryHeap")));
    }

    @Test
    public void test4NDMethod()throws IOException{
        assertTrue(compareTwoFiles(getMyDataMethod("4-NextDay"),getDataMethod("4-NextDay")));
    }

    @Test
    public void test5MTMethod()throws IOException{
        assertTrue(compareTwoFiles(getMyDataMethod("5-MoreTriangle"),getDataMethod("5-MoreTriangle")));
    }


    public boolean compareTwoFiles(String f1,String f2) throws IOException {
        File myOutputFile=new File(f1);
        File refFile=new File(f2);
        ArrayList<String> myList=new ArrayList<String>();
        ArrayList<String> refList=new ArrayList<String>();
        BufferedReader reader=new BufferedReader(new FileReader(myOutputFile));
        String tmpStr;
        while ((tmpStr=reader.readLine())!=null){
            if (tmpStr.length()>0){
                myList.add(tmpStr);
            }
        }
        reader=new BufferedReader(new FileReader(refFile));
        while ((tmpStr=reader.readLine())!=null){
            if(tmpStr.length()>0){
                refList.add(tmpStr);
            }
        }
        boolean result=true;
        for (String myLine : myList) {
            if (!refList.contains(myLine)) {
                System.out.println("答案没 我有");
                System.out.println(myLine);
                result=false;
            }
        }

        for (String refLine : refList) {
            if (!myList.contains(refLine)) {
                System.out.println("答案有 我没");
                System.out.println(refLine);
                result=false;
            }
        }
        return result;
    }

    String getMyDataClass(String caseName){
        return "E:\\Study\\Grade3_I\\自动化测试\\大作业\\经典大作业\\AutomatedTesting\\Demo\\selection-class.txt";

    }
    String getDataClass(String caseName){
        return "E:\\Study\\Grade3_I\\自动化测试\\大作业\\经典大作业\\AutomatedTesting\\Data\\"+caseName+"\\data\\selection-class.txt";
    }

    String getMyDataMethod(String caseName){
        return "E:\\Study\\Grade3_I\\自动化测试\\大作业\\经典大作业\\AutomatedTesting\\Demo\\selection-method.txt";

    }
    String getDataMethod(String caseName){
        return "E:\\Study\\Grade3_I\\自动化测试\\大作业\\经典大作业\\AutomatedTesting\\Data\\"+caseName+"\\data\\selection-method.txt";

    }

}
