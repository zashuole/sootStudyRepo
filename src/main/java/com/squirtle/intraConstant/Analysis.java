package com.squirtle.intraConstant;

import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class Analysis extends ForwardFlowAnalysis<Unit, FlowSet<ValueDomain>> {

    private SootMethod method;

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
        in.copy(out);

        if (!(d instanceof AssignStmt)) return;

        AssignStmt assign = (AssignStmt) d;
        Value lhs = assign.getLeftOp();
        Value rhs = assign.getRightOp();
        ValueDomain newVal = null;

        // 左值是局部变量
        if (lhs instanceof Local) {
            Local lhsLocal = (Local) lhs;
            newVal = evalRight(rhs, out, lhsLocal, null);
            removeValue(out, lhsLocal, null);
            out.add(newVal);
        }
        // 左值是字段
        else if (lhs instanceof FieldRef) {
            SootField lhsField = ((FieldRef) lhs).getField();
            newVal = evalRight(rhs, out, null, lhsField);
            removeValue(out, null, lhsField);
            out.add(newVal);
        }
    }

    private ValueDomain evalRight(Value rhs, FlowSet<ValueDomain> set, Local lhsLocal, SootField lhsField) {
        ValueDomain newVal;
        if (rhs instanceof IntConstant) {
            int val = ((IntConstant) rhs).value;
            newVal = lhsLocal != null ? ValueDomain.con(lhsLocal, val)
                    : ValueDomain.conField(lhsField, val);
        }
        else if (rhs instanceof Local) {
            ValueDomain rhsVal = findValue(set, (Local) rhs, null);
            newVal = lhsLocal != null ? copyForLocal(lhsLocal, rhsVal)
                    : copyForField(lhsField, rhsVal);
        }
        else if (rhs instanceof FieldRef) {
            SootField rhsField = ((FieldRef) rhs).getField();
            ValueDomain rhsVal = findValue(set, null, rhsField);
            newVal = lhsLocal != null ? copyForLocal(lhsLocal, rhsVal)
                    : copyForField(lhsField, rhsVal);
        }
        else if (rhs instanceof BinopExpr) {
            BinopExpr binExpr = (BinopExpr) rhs;
            ValueDomain leftVal = getValueFromValue(binExpr.getOp1(), set);
            ValueDomain rightVal = getValueFromValue(binExpr.getOp2(), set);
            newVal = lhsLocal != null ? evalBinop(lhsLocal, binExpr, leftVal, rightVal)
                    : evalBinopField(lhsField, binExpr, leftVal, rightVal);
        }
        else {
            newVal = lhsLocal != null ? ValueDomain.nac(lhsLocal)
                    : ValueDomain.nacField(lhsField);
        }
        return newVal;
    }

    private ValueDomain copyForLocal(Local l, ValueDomain v) {
        if (v.getType() == ValueDomain.ValueType.CON) return ValueDomain.con(l, v.getConstValue());
        else if (v.getType() == ValueDomain.ValueType.NAC) return ValueDomain.nac(l);
        else return ValueDomain.undef(l);
    }

    private ValueDomain copyForField(SootField f, ValueDomain v) {
        if (v.getType() == ValueDomain.ValueType.CON) return ValueDomain.conField(f, v.getConstValue());
        else if (v.getType() == ValueDomain.ValueType.NAC) return ValueDomain.nacField(f);
        else return ValueDomain.undefField(f);
    }

    private ValueDomain evalBinop(Local lhs, BinopExpr expr, ValueDomain left, ValueDomain right) {
        if (left.getType() == ValueDomain.ValueType.CON && right.getType() == ValueDomain.ValueType.CON) {
            int lv = left.getConstValue();
            int rv = right.getConstValue();
            int result;
            if (expr instanceof AddExpr) result = lv + rv;
            else if (expr instanceof SubExpr) result = lv - rv;
            else if (expr instanceof MulExpr) result = lv * rv;
            else if (expr instanceof DivExpr) result = rv != 0 ? lv / rv : 0;
            else return ValueDomain.nac(lhs);
            return ValueDomain.con(lhs, result);
        } else if (left.getType() == ValueDomain.ValueType.UNDEF || right.getType() == ValueDomain.ValueType.UNDEF) {
            return ValueDomain.undef(lhs);
        } else return ValueDomain.nac(lhs);
    }


    private ValueDomain evalBinopField(SootField f, BinopExpr expr, ValueDomain left, ValueDomain right) {
        if (left.getType() == ValueDomain.ValueType.CON && right.getType() == ValueDomain.ValueType.CON) {
            int lv = left.getConstValue();
            int rv = right.getConstValue();
            int result;
            if (expr instanceof AddExpr) result = lv + rv;
            else if (expr instanceof SubExpr) result = lv - rv;
            else if (expr instanceof MulExpr) result = lv * rv;
            else if (expr instanceof DivExpr) result = rv != 0 ? lv / rv : 0;
            else return ValueDomain.nacField(f);
            return ValueDomain.conField(f, result);
        } else if (left.getType() == ValueDomain.ValueType.UNDEF || right.getType() == ValueDomain.ValueType.UNDEF) {
            return ValueDomain.undefField(f);
        } else return ValueDomain.nacField(f);
    }

    private void removeValue(FlowSet<ValueDomain> set, Local l, SootField f) {
        ValueDomain toRemove = null;
        for (ValueDomain v : set) {
            if ((l != null && l.equals(v.getLocal())) || (f != null && f.equals(v.getField()))) {
                toRemove = v;
                break;
            }
        }
        if (toRemove != null) set.remove(toRemove);
    }

    private ValueDomain findValue(FlowSet<ValueDomain> set, Local l, SootField f) {
        for (ValueDomain v : set) {
            if ((l != null && l.equals(v.getLocal())) || (f != null && f.equals(v.getField()))) {
                return v;
            }
        }
        return l != null ? ValueDomain.undef(l) : ValueDomain.undefField(f);
    }

    private ValueDomain getValueFromValue(Value v, FlowSet<ValueDomain> set) {
        if (v instanceof IntConstant) return ValueDomain.con(null, ((IntConstant) v).value);
        else if (v instanceof Local) return findValue(set, (Local) v, null);
        else if (v instanceof FieldRef) return findValue(set, null, ((FieldRef) v).getField());
        else return ValueDomain.nac(null);
    }

    @Override
    protected FlowSet<ValueDomain> newInitialFlow() {
        return new ArraySparseSet<>();
    }

    @Override
    protected FlowSet<ValueDomain> entryInitialFlow() {
        ArraySparseSet<ValueDomain> set = new ArraySparseSet<>();
        //初始化局部变量
        for (Local local : method.getActiveBody().getLocals()) {
            set.add(ValueDomain.undef(local));
        }
        //初始化字段
        // 初始化静态字段
        SootClass clazz = method.getDeclaringClass();
        for (SootField field : clazz.getFields()) {
            if (field.isStatic()) {
                set.add(ValueDomain.undefField(field));
            } else {
                // 非静态字段保守处理为 NAC
                set.add(ValueDomain.nacField(field));
            }
        }
        return set;
    }

    @Override
    protected void merge(FlowSet<ValueDomain> in1, FlowSet<ValueDomain> in2, FlowSet<ValueDomain> out) {
        out.clear();
        // merge in1
        for (ValueDomain v1 : in1) {
            ValueDomain v2 = v1.getLocal() != null ? findValue(in2, v1.getLocal(), null)
                    : findValue(in2, null, v1.getField());
            out.add(new ValueDomain(ValueDomain.ValueType.UNDEF, null, v1.getLocal(), v1.getField()).mergeValues(v1, v2));
        }
        // merge in2 中未处理的
        for (ValueDomain v2 : in2) {
            boolean exists = false;
            for (ValueDomain v1 : in1) {
                if ((v1.getLocal() != null && v1.getLocal().equals(v2.getLocal()))
                        || (v1.getField() != null && v1.getField().equals(v2.getField()))) {
                    exists = true; break;
                }
            }
            if (!exists) out.add(v2);
        }
    }

    @Override
    protected void copy(FlowSet<ValueDomain> source, FlowSet<ValueDomain> dest) {
        source.copy(dest);
    }

    public void showResult(DirectedGraph<Unit> g) {
        for (Unit u : g) {
            FlowSet<ValueDomain> in = getFlowBefore(u);
            FlowSet<ValueDomain> out = getFlowAfter(u);
            System.out.println("Unit: " + u);
            System.out.println("  IN: " + in);
            System.out.println("  OUT: " + out);
        }
    }
}
