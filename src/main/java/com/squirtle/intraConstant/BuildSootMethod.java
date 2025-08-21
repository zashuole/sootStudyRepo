package com.squirtle.intraConstant;

import soot.*;
import soot.options.Options;

import java.util.Collections;
import java.util.List;

public class BuildSootMethod {
    public List<SootMethod> getSootMethod(String targetClass) {
        // 源码根目录
        String srcPath = "D:\\code\\soot-study\\src\\test\\java";

        // 配置 Soot 分析源码
        Options.v().set_process_dir(Collections.singletonList(srcPath));
        Options.v().set_src_prec(Options.src_prec_java); // 处理源码
        Options.v().set_output_format(Options.output_format_none); // 不输出文件
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);

        // 加载目标类
        SootClass c = Scene.v().loadClassAndSupport(targetClass);
        Scene.v().loadNecessaryClasses();

        c.setApplicationClass();

        //获取类中的所有方法
        return c.getMethods();
    }
}
