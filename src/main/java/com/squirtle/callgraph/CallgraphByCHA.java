package com.squirtle.callgraph;

import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.CHAOptions;
import soot.options.Options;

import java.io.File;
import java.util.*;

public class CallgraphByCHA {
    public static void main(String[] args) {
        String appClassesDir = "D:\\code\\soot-study\\bin"; // 你的 classes 根目录
        String mainClass = "com.squirtle.callgraph.CallgraphDemo2"; // 目标类

        // 1) 基本配置
        Options.v().set_prepend_classpath(true);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_keep_offset(true);

        // 2) 设置待分析目录
        Options.v().set_process_dir(Collections.singletonList(appClassesDir));
        Options.v().set_soot_classpath(appClassesDir + ";" + Scene.v().defaultClassPath());

        // 3) 启用 CHA，关闭 Spark
        Options.v().setPhaseOption("cg", "enabled:true");     // 确保 cg pack 启动
        Options.v().setPhaseOption("cg.cha", "enabled:true"); // 启用 CHA
        Options.v().setPhaseOption("cg.spark", "enabled:false"); // 禁用 Spark

        // 4) 排除 JDK 包
        List<String> excludes = Arrays.asList("java.", "javax.", "sun.", "jdk.");
        Options.v().set_exclude(excludes);
        Options.v().set_no_bodies_for_excluded(true);

        // 5) 加载类并设置入口点
        Scene.v().loadNecessaryClasses();
        SootClass c = Scene.v().loadClassAndSupport(mainClass);
        c.setApplicationClass();
        Scene.v().setMainClass(c);

        SootMethod mainMethod = c.getMethodByNameUnsafe("main");
        List<SootMethod> entryPoints = new ArrayList<>();
        if (mainMethod != null) {
            entryPoints.add(mainMethod);
        }
        Scene.v().setEntryPoints(entryPoints);

        // 6) 运行 pack 生成调用图
        PackManager.v().getPack("wjtp"); // Whole-program Jimple Transformation Pack
        PackManager.v().runPacks();

        // 7) 获取调用图并打印 应用类→应用类 边
        CallGraph cg = Scene.v().getCallGraph();
        int app2app = 0, total = 0;
        Iterator<Edge> it = cg.iterator();
        while (it.hasNext()) {
            Edge e = it.next();
            total++;
            SootMethod src = e.src();
            SootMethod tgt = e.tgt();
            boolean srcApp = src.getDeclaringClass().isApplicationClass();
            boolean tgtApp = tgt.getDeclaringClass().isApplicationClass();
            app2app++;
            System.out.println("[APP] " + src.getSignature() + "  -->  " + tgt.getSignature());
        }
        System.out.println("Total edges: " + total + ", App->App edges: " + app2app);
    }
}
