package com.squirtle.callgraph;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

import java.io.File;
import java.util.Arrays;

public class CallgraphByCHA {
    public static void main(String[] args) {
        // class 文件目录
        String appClassDir = "D:\\code\\soot-study\\bin";

        // JDK 核心库路径
        String javaHome = System.getProperty("java.home");
        File jdkRoot = new File(javaHome).getParentFile();
        String rtJar = new File(jdkRoot, "jre/lib/rt.jar").getAbsolutePath();

        // 构建 classpath
        String classPath = appClassDir + File.pathSeparator + rtJar + File.pathSeparator + System.getProperty("java.class.path");
        System.out.println("Soot ClassPath: " + classPath);

        // 配置 Soot
        Options.v().set_soot_classpath(classPath);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_exclude(Arrays.asList("java.", "javax.", "sun.", "jdk."));
        Options.v().set_app(true); // 应用类
        Options.v().set_no_bodies_for_excluded(true);

        // 设置入口类
        SootClass sc = Scene.v().loadClassAndSupport("com.squirtle.callgraph.CallgraphDemo2");
        sc.setApplicationClass();
        Scene.v().setMainClass(sc);

        // 加载必要类
        Scene.v().loadNecessaryClasses();

        //  构建调用图
        CHATransformer.v().transform();

        //  获取调用图
        CallGraph cg = Scene.v().getCallGraph();

        // 遍历调用图，只输出应用类之间的调用
        for (Edge edge : cg) {
            SootMethod src = edge.getSrc().method();
            SootMethod tgt = edge.getTgt().method();
            if (src.getDeclaringClass().getPackageName().startsWith("com.squirtle") &&
                    tgt.getDeclaringClass().getPackageName().startsWith("com.squirtle")) {
                System.err.println("调用方: " + src.getSignature());
                System.err.println("被调用方: " + tgt.getSignature());
                System.out.println("---");
            }else if (src.getDeclaringClass().getPackageName().startsWith("com.squirtle")) {
                System.out.println("调用方: " + src.getSignature());
                System.out.println("被调用方: " + tgt.getSignature());
                System.out.println("---");
            }
        }
    }
}
