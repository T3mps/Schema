package com.temprovich.e30;

import java.util.List;

import com.temprovich.e30.Expression.Assign;
import com.temprovich.e30.Expression.Variable;
import com.temprovich.e30.Statement.Auto;
import com.temprovich.e30.Statement.Block;
import com.temprovich.e30.Statement.Expr;
import com.temprovich.e30.Statement.Print;

public class Interpreter implements Expression.Visitor<Object>,
                                    Statement.Visitor<Void> {

    private Environment environment;

    public Interpreter() {
        this.environment = new Environment();
    }
    
    public void interpret(List<Statement> statements) {
        try {
            for (var statement : statements) {
                execute(statement);
            }
        } catch (E30RuntimeException error) {
            E30.runtimeError(error);
        }
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal expression) {
        return expression.value();
    }

    @Override
    public Object visitGroupingExpression(Expression.Grouping expression) {
        return evaluate(expression.expression());
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression) {
        Object right = evaluate(expression.right());

        switch (expression.operator().type()) {
            case MINUS: return -(double) right;
            case BANG: return !predicate(right);
            default: throw new IllegalArgumentException("unknown operator");
        }
    }

    @Override
    public Object visitBinaryExpression(Expression.Binary expression) {
        Object left = evaluate(expression.left());
        Object right = evaluate(expression.right());

        switch (expression.operator().type()) {
            case GREATER:
                validateArithmeticExpression(expression.operator(), left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                validateArithmeticExpression(expression.operator(), left, right);
                return (double) left >= (double) right;
            case LESS:
                validateArithmeticExpression(expression.operator(), left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                validateArithmeticExpression(expression.operator(), left, right);
                return (double) left <= (double) right;
            case BANG_EQUAL:    return !isEqual(left, right);
            case EQUAL_EQUAL:   return isEqual(left, right);
            case MINUS:
                validateArithmeticExpression(expression.operator(), left, right);
                return (double) left - (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String) {
                    return (String) left + stringify(right);
                }
                if (right instanceof String) {
                    return stringify(left) + (String) right;
                }

                throw new E30RuntimeException(expression.operator(), "Operands must be two numbers or two strings."); // throw an error if the attempted operation is neither addition nor concatenation
            case SLASH:
                validateArithmeticExpression(expression.operator(), left, right);
                return (double) left / (double) right;
            case STAR:
                validateArithmeticExpression(expression.operator(), left, right);
                return (double) left * (double) right;
            default:
                throw new IllegalArgumentException("unknown operator");
        }
    }

    @Override
    public Void visitExprStatement(Expr expr) {
        evaluate(expr.expression());
        return null;
    }

    @Override
    public Void visitPrintStatement(Print print) {
        var value = evaluate(print.expression());
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitAutoStatement(Auto auto) {
        Object value = null;
        if (auto.value() != null) {
            value = evaluate(auto.value());
        }

        environment.define(auto.name().lexeme(), value);
        return null;
    }

    @Override
    public Object visitVariableExpression(Variable variable) {
        return environment.fetch(variable.name());
    }

    @Override
    public Object visitAssignExpression(Assign assign) {
        Object value = evaluate(assign.value());
        environment.assign(assign.name(), value);
        return value;
    }

    @Override
    public Void visitBlockStatement(Block block) {
        executeBlock(block.statements(), new Environment(environment));
        return null;
    }

    private void executeBlock(List<Statement> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (var statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    private Object evaluate(Expression expression) {
        return expression.accept(this);
    }

    private boolean predicate(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        return true;
    }

    public boolean isEqual(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null) {
            return false;
        }
        return a.equals(b);
    }

    private void validateArithmeticExpression(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) {
            return;
        }
        
        throw new E30RuntimeException(operator, "Malformed expression detected: attempted to operate on " + left.getClass().getName() + ", " + right.getClass().getName() + " with operator " + operator.type());
    }

    private String stringify(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Double) {
            String string = value.toString();
            if (string.endsWith(".0")) {
                return string.substring(0, string.length() - 2);
            }

            return string;
        }

        return value.toString();
    }
}
