package com.squirtle.callgraph;

import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CallgraphByRTA {
    public static void main(String[] args) {
        String appClassesDir = "D:\\code\\soot-study\\bin";
        String mainClass = "com.squirtle.callgraph.CallgraphDemo";

        Options.v().set_prepend_classpath(true);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_keep_line_number(true);

        // 设置分析目录
        Options.v().set_process_dir(Collections.singletonList(appClassesDir));
        Options.v().set_soot_classpath(appClassesDir + ";" + Scene.v().defaultClassPath());

        // 4) 排除 JDK 包
        List<String> excludes = Arrays.asList("java.", "javax.", "sun.", "jdk.");
        Options.v().set_exclude(excludes);
        Options.v().set_no_bodies_for_excluded(true);

        // 开启 Spark，并启用 RTA
        Options.v().setPhaseOption("cg.spark", "enabled:true");
        Options.v().setPhaseOption("cg.spark", "rta:true");
        Options.v().setPhaseOption("cg.spark", "on-fly-cg:false");

        // 加载类并设置入口
        Scene.v().loadNecessaryClasses();
        SootClass c = Scene.v().loadClassAndSupport(mainClass);
        c.setApplicationClass();
        Scene.v().setMainClass(c);

        SootMethod mainMethod = c.getMethodByNameUnsafe("main");
        if (mainMethod != null) {
            Scene.v().setEntryPoints(Collections.singletonList(mainMethod));
        }

        PackManager.v().runPacks();

        CallGraph cg = Scene.v().getCallGraph();
        int count = 0;
        for (Iterator<Edge> it = cg.iterator(); it.hasNext();) {
            Edge e = it.next();
            System.out.println(e.src() + "  -->  " + e.tgt());
            count++;
        }
        System.out.println("Total edges: " + count);
    }
}
