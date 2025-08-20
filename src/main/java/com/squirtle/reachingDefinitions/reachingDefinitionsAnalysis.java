package com.squirtle.reachingDefinitions;

import soot.*;
import soot.jimple.Expr;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;

import java.io.File;
import java.util.Collections;

public class reachingDefinitionsAnalysis {
    //这里采用soot分析源码，因为如果分析class文件，会编译优化，字节码文件不完整
    public static void main(String[] args) {
        // 源码根目录
        String srcPath = "D:\\code\\soot-study\\src\\test\\java";

        // 配置 Soot 分析源码
        Options.v().set_process_dir(Collections.singletonList(srcPath));
        Options.v().set_src_prec(Options.src_prec_java); // 处理源码
        Options.v().set_output_format(Options.output_format_none); // 不输出文件
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);

        // 加载目标类
        Scene.v().addBasicClass(
                "com.squirtle.reachingDefinitions.ReachingDefinitionsExample",
                SootClass.BODIES
        );
        Scene.v().loadNecessaryClasses();

        SootClass c = Scene.v().getSootClass("com.squirtle.reachingDefinitions.ReachingDefinitionsExample");
        c.setApplicationClass();

        SootMethod m = c.getMethodByName("exampleMethod");
        Body b = m.retrieveActiveBody(); // 获取 Jimple 方法体
        ExceptionalUnitGraph g = new ExceptionalUnitGraph(b);
        Analysis analysis = new Analysis(g);
        analysis.showResult(g);

    }
}
