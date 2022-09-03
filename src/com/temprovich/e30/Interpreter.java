package com.temprovich.e30;

import java.util.ArrayList;
import java.util.List;

import com.temprovich.e30.Expression.Assign;
import com.temprovich.e30.Expression.Call;
import com.temprovich.e30.Expression.Logical;
import com.temprovich.e30.Expression.Variable;
import com.temprovich.e30.Statement.Auto;
import com.temprovich.e30.Statement.Block;
import com.temprovich.e30.Statement.Expr;
import com.temprovich.e30.Statement.Function;
import com.temprovich.e30.Statement.If;
import com.temprovich.e30.Statement.While;
import com.temprovich.e30.error.E30RuntimeError;
import com.temprovich.e30.preinclude.E30NativeBase;
import com.temprovich.e30.preinclude.E30NativeInternal;
import com.temprovich.e30.preinclude.E30NativeMath;
import com.temprovich.e30.preinclude.Preinclude;

public class Interpreter implements Expression.Visitor<Object>,
                                    Statement.Visitor<Void> {

    private static Preinclude[] preincluded = new Preinclude[] {
        new E30NativeInternal(),
        new E30NativeBase(),
        new E30NativeMath(), // TODO: move to import statement
    };

    private final Environment globals;

    private Environment environment;

    public Interpreter() {
        this.globals = new Environment();
        for (var preinclude : preincluded) {
            preinclude.inject(globals);
        }
        this.environment = globals;
    }

    public void interpret(List<Statement> statements) {
        try {
            for (var statement : statements) {
                execute(statement);
            }
        } catch (E30RuntimeError error) {
            E30.runtimeError(error);
        }
    }

    /*
     * Determines the type of an object and returns the appropriate string
     */
    public <T> String typeOf(T object) {
        if (object == null) {
            return "null";
        } else if (object instanceof Boolean) {
            return "bool";
        } else if (object instanceof Double) {
            if (((Double) object).intValue() == (Double) object) {
                return "int";
            } else {
                return "float";
            }
        } else if (object instanceof String) {
            return "string";
        } else if (object instanceof E30Callable) {
            return "function";
        }
        
        return "?";
    }

    public boolean is(Object object0, Object object1) {
        if (object0 == null && object1 == null) {
            return true;
        } else if (object0 == null) {
            return false;
        } else {
            return typeOf(object0).equals(typeOf(object1));
        }
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
            default: throw new E30RuntimeError("unknown operator");
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

                throw new E30RuntimeError(expression.operator(), "Operands must be two numbers or two strings."); // throw an error if the attempted operation is neither addition nor concatenation
            case SLASH:
                validateArithmeticExpression(expression.operator(), left, right);
                return (double) left / (double) right;
            case STAR:
                validateArithmeticExpression(expression.operator(), left, right);
                return (double) left * (double) right;
            default:
                throw new E30RuntimeError("unknown operator");
        }
    }

    @Override
    public Void visitExprStatement(Expr statement) {
        evaluate(statement.expression());
        return null;
    }
    
    @Override
    public Void visitAutoStatement(Auto statement) {
        Object value = null;
        if (statement.value() != null) {
            value = evaluate(statement.value());
        }

        environment.define(statement.name().lexeme(), value);
        return null;
    }

    @Override
    public Object visitVariableExpression(Variable expression) {
        return environment.fetch(expression.name());
    }

    @Override
    public Object visitAssignExpression(Assign expression) {
        Object value = evaluate(expression.value());
        environment.assign(expression.name(), value);
        return value;
    }

    @Override
    public Void visitBlockStatement(Block statement) {
        executeBlock(statement.statements(), new Environment(environment));
        return null;
    }

    @Override
    public Void visitIfStatement(If statement) {
        if (predicate(evaluate(statement.condition()))) {
            execute(statement.thenBranch());
        } else if (statement.elseBranch() != null) {
            execute(statement.elseBranch());
        }

        return null;
    }

    @Override
    public Object visitLogicalExpression(Logical statement) {
        Object left = evaluate(statement.left());
        
        if (statement.operator().type() == TokenType.OR) {
            if (predicate(left)) return left;
        } else {
            if (!predicate(left)) return left;
        }

        return evaluate(statement.right());
    }

    @Override
    public Void visitWhileStatement(While statement) {
        while (predicate(evaluate(statement.condition()))) {
            execute(statement.body());
        }

        return null;
    }

    @Override
    public Object visitCallExpression(Call expression) {
        var callee = evaluate(expression.callee());

        List<Object> arguments = new ArrayList<Object>();
        for (Expression argument : expression.arguments()) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof E30Callable)) {
            throw new E30RuntimeError(expression.paren(), "Can only call functions and classes.");
        }

        E30Callable function = (E30Callable) callee;

        if (arguments.size() != function.arity()) {
            throw new E30RuntimeError(expression.paren(), "Function received " + arguments.size() + " arguments, but expects " + function.arity() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Void visitFunctionStatement(Function statement) {
        E30Function function = new E30Function(statement, environment);
        environment.define(statement.name().lexeme(), function);
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement) {
        Object value = null;
        if (statement.value() != null) {
            value = evaluate(statement.value());
        }

        throw new com.temprovich.e30.Return(value);
    }

    protected void executeBlock(List<Statement> statements, Environment environment) {
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

    private void execute(Statement statement) {
        statement.accept(this);
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
        if (obj instanceof Double) {
            return (double) obj != 0;
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
        
        throw new E30RuntimeError(operator, "Malformed expression detected: attempted to operate on " + left.getClass().getName() + ", " + right.getClass().getName() + " with operator " + operator.type());
    }

    public static String stringify(Object value) {
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

    public Environment getGlobals() {
        return globals;
    }
}
