package com.squirtle.veryBusyExprExample;

import soot.*;
import soot.jimple.Expr;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;

import java.io.File;

public class VeryBusyExprAnalysis {

    public static void main(String[] args) {
        // class 文件目录
        String appClassDir = "D:\\code\\soot-study\\bin";

        // JDK 核心库路径
        String javaHome = System.getProperty("java.home");
        File jdkRoot = new File(javaHome).getParentFile();
        String rtJar = new File(jdkRoot, "jre/lib/rt.jar").getAbsolutePath();

        // 构建 classpath
        StringBuilder classPath = new StringBuilder();
        classPath.append(appClassDir)
                .append(File.pathSeparator)
                .append(rtJar)
                .append(File.pathSeparator)
                .append(System.getProperty("java.class.path"));

        System.out.println("Soot ClassPath: " + classPath);

        // 配置 Soot
        Options.v().set_soot_classpath(classPath.toString());
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);

        // 先把目标类提升到 SIGNATURES 层级
        Scene.v().addBasicClass("com.squirtle.veryBusyExprExample.VeryBusyExample", SootClass.SIGNATURES);

        // 加载必要的类
        Scene.v().loadNecessaryClasses();

        // 获取 SootClass
        SootClass c = Scene.v().getSootClass("com.squirtle.veryBusyExprExample.VeryBusyExample");
        c.setApplicationClass();

        // 升级到 BODIES 层级
        c.setResolvingLevel(SootClass.BODIES);

        // 获取方法体
        SootMethod m = c.getMethodByName("compute");
        //获取目标方法的Jimple方法体
        Body b = m.retrieveActiveBody();

        // 构建控制流图并运行分析
        UnitGraph g = new ExceptionalUnitGraph(b);  // 用带异常流的CFG
        Analysis an = new Analysis(g);  // 初始化分析

        for (Unit u : g) {
            FlowSet<Expr> in = an.getFlowBefore(u);
            FlowSet<Expr> out = an.getFlowAfter(u);
            System.out.println("Unit: " + u);
            System.out.println("  IN: " + in);
            System.out.println("  OUT: " + out);
        }
    }
}
