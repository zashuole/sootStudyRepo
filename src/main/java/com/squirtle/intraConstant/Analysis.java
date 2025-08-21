package com.squirtle.intraConstant;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class Analysis extends ForwardFlowAnalysis<Unit, FlowSet<ValueDomain>> {

    private SootMethod method;

    /**
     * Construct the analysis from a DirectedGraph representation of a Body.
     *
     * @param graph
     */
    public Analysis(DirectedGraph<Unit> graph, SootMethod sootMethod) {
        super(graph);
        this.method = sootMethod;
        doAnalysis();
    }

    public void setMethod(SootMethod method) {
        this.method = method;
    }

    @Override
    protected void flowThrough(FlowSet<ValueDomain> in, Unit d, FlowSet<ValueDomain> out) {
        // 先复制前驱状态
        in.copy(out);
        // 只处理赋值语句
        if (d instanceof AssignStmt) {
            AssignStmt assign = (AssignStmt) d;
            Value lhs = assign.getLeftOp();  // 左值
            Value rhs = assign.getRightOp(); // 右值

            if (!(lhs instanceof Local)) {
                return; // 我们只分析局部变量
            }

            Local lhsLocal = (Local) lhs;
            ValueDomain newVal = null;

            // 1. 右值是常量
            if (rhs instanceof IntConstant) {
                int val = ((IntConstant) rhs).value;
                newVal = ValueDomain.con(lhsLocal, val);
            }
            // 2. 右值是局部变量
            else if (rhs instanceof Local) {
                Local rhsLocal = (Local) rhs;
                ValueDomain rhsVal = findValue(out, rhsLocal);

                if (rhsVal.getType() == ValueDomain.ValueType.CON) {
                    newVal = ValueDomain.con(lhsLocal, rhsVal.getConstValue());
                } else if (rhsVal.getType() == ValueDomain.ValueType.NAC) {
                    newVal = ValueDomain.nac(lhsLocal);
                } else {
                    newVal = ValueDomain.undef(lhsLocal);
                }
            }
            // 3. 右值是二元表达式
            else if (rhs instanceof BinopExpr) {
                BinopExpr binExpr = (BinopExpr) rhs;
                Value leftOp = binExpr.getOp1();
                Value rightOp = binExpr.getOp2();

                ValueDomain leftVal = getValueFromValue(leftOp, out);
                ValueDomain rightVal = getValueFromValue(rightOp, out);

                newVal = evalBinop(lhsLocal, binExpr, leftVal, rightVal);
            }
            // 4. 其他情况
            else {
                newVal = ValueDomain.nac(lhsLocal);
            }

            // 更新 out
            removeValue(out, lhsLocal);
            out.add(newVal);
        }
    }

    // 从 FlowSet 找到某个 Local 的 ValueDomain
    private ValueDomain findValue(FlowSet<ValueDomain> set, Local l) {
        for (ValueDomain v : set) {
            if (v.getLocal().equals(l)) {
                return v;
            }
        }
        return ValueDomain.undef(l);
    }

    // 根据 Value 构造 ValueDomain
    private ValueDomain getValueFromValue(Value v, FlowSet<ValueDomain> set) {
        if (v instanceof IntConstant) {
            return ValueDomain.con(null, ((IntConstant) v).value); // 临时值，没有对应 Local
        } else if (v instanceof Local) {
            return findValue(set, (Local) v);
        } else {
            return ValueDomain.nac(null);
        }
    }

    // 对二元运算求值
    private ValueDomain evalBinop(Local lhs, BinopExpr expr, ValueDomain left, ValueDomain right) {
        if (left.getType() == ValueDomain.ValueType.CON && right.getType() == ValueDomain.ValueType.CON) {
            int lv = left.getConstValue();
            int rv = right.getConstValue();
            int result;
            if (expr instanceof AddExpr) {
                result = lv + rv;
            } else if (expr instanceof SubExpr) {
                result = lv - rv;
            } else if (expr instanceof MulExpr) {
                result = lv * rv;
            } else if (expr instanceof DivExpr) {
                result = rv != 0 ? lv / rv : 0; // 简单处理除零
            } else {
                return ValueDomain.nac(lhs);
            }
            return ValueDomain.con(lhs, result);
        } else if (left.getType() == ValueDomain.ValueType.UNDEF || right.getType() == ValueDomain.ValueType.UNDEF) {
            return ValueDomain.undef(lhs);
        } else {
            return ValueDomain.nac(lhs);
        }
    }

    // 删除已有 Local 的 ValueDomain
    private void removeValue(FlowSet<ValueDomain> set, Local l) {
        ValueDomain toRemove = null;
        for (ValueDomain v : set) {
            if (v.getLocal().equals(l)) {
                toRemove = v;
                break;
            }
        }
        if (toRemove != null) set.remove(toRemove);
    }

    @Override
    protected FlowSet<ValueDomain> newInitialFlow() {
        return new ArraySparseSet<>();
    }

    @Override
    protected FlowSet<ValueDomain> entryInitialFlow() {
        ArraySparseSet<ValueDomain> set = new ArraySparseSet<>();

        for(Local local : method.getActiveBody().getLocals()) {
            set.add(ValueDomain.undef(local));
        }
        return set;
    }

    @Override
    protected void merge(FlowSet<ValueDomain> in1, FlowSet<ValueDomain> in2, FlowSet<ValueDomain> out) {
        out.clear();

        // 先处理 in1 中的变量
        for (ValueDomain v1 : in1) {
            ValueDomain v2 = findValue(in2, v1.getLocal());
            out.add(ValueDomain.undef(v1.getLocal()).mergeValues(v1, v2));
        }

        // 再把 in2 中 in1 没有的变量加入
        for (ValueDomain v2 : in2) {
            if (findValue(in1, v2.getLocal()) == null) {
                out.add(v2);
            }
        }
    }

    @Override
    protected void copy(FlowSet<ValueDomain> source, FlowSet<ValueDomain> dest) {
        source.copy(dest);
    }

    public void showResult( DirectedGraph<Unit> g){
        for (Unit u : g) {
            FlowSet<ValueDomain> in = getFlowBefore(u);
            FlowSet<ValueDomain> out = getFlowAfter(u);
            System.out.println("Unit: " + u);
            System.out.println("  IN: " + in);
            System.out.println("  OUT: " + out);
        }
    }
}
