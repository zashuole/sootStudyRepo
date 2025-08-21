package com.squirtle.intraConstant;

import soot.Local;
import soot.SootField;

import java.util.Objects;

public class ValueDomain {

    public enum ValueType {
        UNDEF,    // 未定义
        CON,      // 常量
        NAC       // 非常量
    }

    private ValueType type;
    private Integer constValue; // 如果是 CON，则存具体常量值
    private Local local;        // 局部变量
    private SootField field;    // 实例字段或静态字段

    // 构造函数
    ValueDomain(ValueType type, Integer constValue, Local local, SootField field) {
        this.type = type;
        this.constValue = constValue;
        this.local = local;
        this.field = field;
    }

    // 局部变量工厂方法
    public static ValueDomain undef(Local local) {
        return new ValueDomain(ValueType.UNDEF, null, local, null);
    }

    public static ValueDomain con(Local local, Integer v) {
        return new ValueDomain(ValueType.CON, v, local, null);
    }

    public static ValueDomain nac(Local local) {
        return new ValueDomain(ValueType.NAC, null, local, null);
    }

    // 字段工厂方法
    public static ValueDomain undefField(SootField field) {
        return new ValueDomain(ValueType.UNDEF, null, null, field);
    }

    public static ValueDomain conField(SootField field, Integer v) {
        return new ValueDomain(ValueType.CON, v, null, field);
    }

    public static ValueDomain nacField(SootField field) {
        return new ValueDomain(ValueType.NAC, null, null, field);
    }

    // 获取类型
    public ValueType getType() {
        return type;
    }

    public Integer getConstValue() {
        return constValue;
    }

    public Local getLocal() {
        return local;
    }

    public SootField getField() {
        return field;
    }

    // 合并两个 ValueDomain
    public ValueDomain mergeValues(ValueDomain v1, ValueDomain v2) {
        if (v1 == null) return v2;
        if (v2 == null) return v1;

        // 两边都是常量且相等
        if (v1.type == ValueType.CON &&
                v2.type == ValueType.CON &&
                Objects.equals(v1.constValue, v2.constValue)) {
            return v1.local != null ? ValueDomain.con(v1.local, v1.constValue)
                    : ValueDomain.conField(v1.field, v1.constValue);
        }

        // 其中一边 UNDEF → 用另一边
        if (v1.type == ValueType.UNDEF) return v2;
        if (v2.type == ValueType.UNDEF) return v1;

        // 常量不同或有 NAC → NAC
        return v1.local != null ? ValueDomain.nac(v1.local) : ValueDomain.nacField(v1.field);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueDomain that = (ValueDomain) o;
        return type == that.type &&
                Objects.equals(constValue, that.constValue) &&
                Objects.equals(local, that.local) &&
                Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, constValue, local, field);
    }

    @Override
    public String toString() {
        if (local != null) {
            return "ValueDomain{local=" + local + ", type=" + type + ", constValue=" + constValue + "}";
        } else {
            return "ValueDomain{field=" + field + ", type=" + type + ", constValue=" + constValue + "}";
        }
    }
}
