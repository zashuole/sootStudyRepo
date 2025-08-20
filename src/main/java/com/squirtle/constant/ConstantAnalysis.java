package com.squirtle.constant;

import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.ExceptionalGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.List;
//常量传播，注意这里只关注int类型的数据
public class ConstantAnalysis {
    public static void main(String[] args) {
        BuildSootMethod buildSootMethod = new BuildSootMethod();
        List<SootMethod> sootMethod = buildSootMethod.getSootMethod("com.squirtle.intraprocedural.ConstantDemo");
        for(SootMethod method : sootMethod) {
            Body body = method.retrieveActiveBody();
            ExceptionalUnitGraph graph = new ExceptionalUnitGraph(body);
            Analysis analysis = new Analysis(graph, method);
            System.out.println("method:" + method.getName()+"--------------------------------");
            analysis.showResult(graph);
        }
    }
}
