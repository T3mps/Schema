package com.temprovich.e30;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.temprovich.e30.Expression.Assign;
import com.temprovich.e30.Expression.Attribute;
import com.temprovich.e30.Expression.Call;
import com.temprovich.e30.Expression.Index;
import com.temprovich.e30.Expression.IndexGet;
import com.temprovich.e30.Expression.IndexSet;
import com.temprovich.e30.Expression.Logical;
import com.temprovich.e30.Expression.Parent;
import com.temprovich.e30.Expression.Self;
import com.temprovich.e30.Expression.Set;
import com.temprovich.e30.Expression.Variable;
import com.temprovich.e30.Statement.Auto;
import com.temprovich.e30.Statement.Block;
import com.temprovich.e30.Statement.Break;
import com.temprovich.e30.Statement.Continue;
import com.temprovich.e30.Statement.Expr;
import com.temprovich.e30.Statement.If;
import com.temprovich.e30.Statement.Trait;
import com.temprovich.e30.Statement.Use;
import com.temprovich.e30.Statement.While;
import com.temprovich.e30.error.E30RuntimeError;
import com.temprovich.e30.instance.E30Array;
import com.temprovich.e30.instance.E30Callable;
import com.temprovich.e30.instance.E30Function;
import com.temprovich.e30.instance.E30Instance;
import com.temprovich.e30.instance.E30Node;
import com.temprovich.e30.lexer.Token;
import com.temprovich.e30.lexer.TokenType;
import com.temprovich.e30.module.E30Module;
import com.temprovich.e30.module.E30ModuleBase;
import com.temprovich.e30.module.E30ModuleIO;
import com.temprovich.e30.module.E30ModuleInternal;
import com.temprovich.e30.module.E30ModuleMath;
import com.temprovich.e30.throwables.BreakException;

public class Interpreter implements Expression.Visitor<Object>,
                                    Statement.Visitor<Void> {

    private static E30Module[] preincluded = new E30Module[] {
        new E30ModuleInternal(),
        new E30ModuleBase()
    };

    private final Map<String, Object> globals;
    private Environment environment;
    private final Map<Object, Integer> locals;
    private final Map<Object, Integer> slots;


    public Interpreter() {
        this.globals = new HashMap<String, Object>();
        for (var preinclude : preincluded) {
            preinclude.inject(globals);
        }
        this.environment = null;
        this.locals = new HashMap<Object, Integer>();
        this.slots = new HashMap<Object, Integer>();
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
    public static <T> String typeOf(T object) {
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

    @Override
    public Object visit(Expression.Literal expression) {
        return expression.value();
    }

    @Override
    public Object visit(Expression.Grouping expression) {
        return evaluate(expression.expression());
    }

    @Override
    public Object visit(Expression.Unary expression) {
        Object right = evaluate(expression.right());

        switch (expression.operator().type()) {
            case MINUS: return -(double) right;
            case BANG: return !predicate(right);
            case PLUS_PLUS: {
                Integer distance = locals.get(expression.right());
                if (distance != null) {
                    environment.assign(distance, slots.get(expression.right()), (double) right + 1);
                    return null;
                }
                return (double) right + 1;
            }
            case MINUS_MINUS: {
                Integer distance = locals.get(expression.right());
                if (distance != null) {
                    environment.assign(distance, slots.get(expression.right()), (double) right - 1);
                    return null;
                }
                return (double) right - 1;
            }
            default: throw new E30RuntimeError("unknown operator");
        }
    }

    @Override
    public Object visit(Expression.Binary expression) {
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
                if (left instanceof String || right instanceof String) {
                    return stringify(left) + stringify(right);
                }

                throw new E30RuntimeError(expression.operator(), "Operands must be two numbers or two strings."); // throw an error if the attempted operation is neither addition nor concatenation
            case SLASH:
                validateArithmeticExpression(expression.operator(), left, right);
                return (double) left / (double) right;
            case STAR:
                validateArithmeticExpression(expression.operator(), left, right);
                return (double) left * (double) right;
            case PLUS_EQUAL: {
                Integer distance = locals.get(expression.left());
                if (distance != null) {
                    environment.assign(distance, slots.get(expression.left()), (double) left + (double) right);
                    return null;
                }
                return (double) left + (double) right;
            }
            case MINUS_EQUAL: {
                Integer distance = locals.get(expression.left());
                if (distance != null) {
                    environment.assign(distance, slots.get(expression.left()), (double) left - (double) right);
                    return null;
                }
                return (double) left - (double) right;
            }
            case STAR_EQUAL: {
                Integer distance = locals.get(expression.left());
                if (distance != null) {
                    environment.assign(distance, slots.get(expression.left()), (double) left * (double) right);
                    return null;
                }
                return (double) left * (double) right;
            }
            case SLASH_EQUAL: {
                Integer distance = locals.get(expression.left());
                if (distance != null) {
                    environment.assign(distance, slots.get(expression.left()), (double) left / (double) right);
                    return null;
                }
                return (double) left / (double) right;
            }
            default:
                throw new E30RuntimeError("unknown operator");
        }
    }

    @Override
    public Void visit(Expr statement) {
        evaluate(statement.expression());
        return null;
    }
    
    @Override
    public Void visit(Auto statement) {
        Object value = null;
        if (statement.value() != null) {
            value = evaluate(statement.value());
        }

        define(statement.name(), value);
        return null;
    }

    @Override
    public Void visit(Statement.Node statement) {
        Object parent = null;
        if (statement.parent() != null) {
            parent = evaluate(statement.parent());

            if (!(parent instanceof E30Node)) {
                throw new E30RuntimeError(statement.parent().name(), "Parent must be a node.");
            }
        }

        define(statement.name(), null);

        if (statement.parent() != null) {
            environment = new Environment(environment);
            define("parent", parent);
        }

        var metaMethods = applyTraits(statement.traits());
        for (var method : statement.metaMethods()) {
            E30Function function = new E30Function(method, environment, false);
            metaMethods.put(method.name().lexeme(), function);
        }

        E30Node metaNode = new E30Node(null, (E30Node) parent, statement.name().lexeme() + ":metanode", metaMethods);

        var methods = applyTraits(statement.traits());
        for (var method : statement.methods()) {
            E30Function function = new E30Function(method, environment, method.name().lexeme().equals("define"));
            methods.put(method.name().lexeme(), function);
        }

        E30Node node = new E30Node(metaNode, (E30Node) parent, statement.name().lexeme(),  methods);
        
        if (statement.parent() != null) {
            environment = environment.enclosing();
        }

        Integer distance = locals.get(statement);
        if (distance != null) {
            environment.assign(distance, slots.get(statement), node);
            return null;
        }

        globals.put(statement.name().lexeme(), node);

        return null;
    }

    @Override
    public Object visit(Variable expression) {
        return fetchVariable(expression.name(), expression);
    }

    @Override
    public Object visit(Assign expression) {
        Object value = evaluate(expression.value());
        Integer distance = locals.get(expression);
        if (distance != null) {
            environment.assign(distance, slots.get(expression), value);
        } else {
            if (globals.containsKey(expression.name().lexeme())) {
                globals.put(expression.name().lexeme(), value);
            } else {
                throw new E30RuntimeError(expression.name(), "Undefined variable '" + expression.name().lexeme() + "'.");
            }
        }
        
        return value;
    }

    @Override
    public Void visit(Block statement) {
        executeBlock(statement.statements(), new Environment(environment));
        return null;
    }

    @Override
    public Void visit(If statement) {
        if (predicate(evaluate(statement.condition()))) {
            execute(statement.thenBranch());
        } else if (statement.elseBranch() != null) {
            execute(statement.elseBranch());
        }

        return null;
    }

    @Override
    public Object visit(Logical statement) {
        Object left = evaluate(statement.left());
        
        if (statement.operator().type() == TokenType.OR) {
            if (predicate(left)) return left;
        } else {
            if (!predicate(left)) return left;
        }

        return evaluate(statement.right());
    }

    @Override
    public Void visit(While statement) {
        try {
            while (predicate(evaluate(statement.condition()))) {
                var stmt = statement.body();

                // handle continue statements
                if (stmt instanceof Continue) {
                    //TODO: implement continue
                }

                execute(statement.body());
            }
        } catch (BreakException error) {
            // do nothing
        }

        return null;
    }

    @Override
    public Object visit(Call expression) {
        var callee = evaluate(expression.callee());

        List<Object> arguments = new ArrayList<Object>();
        for (Expression argument : expression.arguments()) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof E30Callable)) {
            throw new E30RuntimeError(expression.paren(), "Can only call functions and classes.");
        }

        E30Callable function = (E30Callable) callee;

        if (arguments.size() != function.arity() && !function.isVariadic()) {
            throw new E30RuntimeError(expression.paren(), "Function received " + arguments.size() + " arguments, but expects " + function.arity() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Void visit(Statement.Function statement) {
        E30Function function = new E30Function(statement, environment, false);
        define(statement.name(), function);
        return null;
    }

    @Override
    public Object visit(Expression.Function expression) {
        return new E30Function(new Statement.Function(null, expression), environment, false);
    }

    @Override
    public Void visit(Statement.Return statement) {
        Object value = null;
        if (statement.value() != null) {
            value = evaluate(statement.value());
        }

        throw new com.temprovich.e30.throwables.ReturnException(value);
    }

    @Override
    public Void visit(Break statement) {
        throw new BreakException();
    }

    @Override
    public Void visit(Continue statement) {
        // handle in while statement
        return null;
    }

    @Override
    public Object visit(Attribute expression) {
        var object = evaluate(expression.object());
        if (object instanceof E30Instance) {
            var result = ((E30Instance) object).get(expression.name());
            if (result instanceof E30Function && ((E30Function) result).isGetter()) {
                return ((E30Function) result).call(this, null);
            }

            return result;
        }
        
        throw new E30RuntimeError(expression.name(), "Only qualified instances have accessible attributes.");
    }

    @Override
    public Object visit(Set expression) {
        var object = evaluate(expression.object());
        if (!(object instanceof E30Instance)) {
            throw new E30RuntimeError(expression.name(), "Only instances have fields.");
        }

        Object value = evaluate(expression.value());
        ((E30Instance) object).set(expression.name(), value);
        return value;
    }

    @Override
    public Object visit(Self expression) {
        return fetchVariable(expression.keyword(), expression);
    }

    @Override
    public Object visit(Parent expression) {
        int distance = locals.get(expression);
        E30Node parent = (E30Node) environment.fetch(distance, 0);
        
        E30Instance instance = (E30Instance) environment.fetch(distance - 1, 0);

        E30Function method = parent.fetchMethod(expression.method().lexeme());

        if (method == null) {
            throw new E30RuntimeError(expression.method(), "Undefined property '" + expression.method().lexeme() + "'.");
        }

        return method.bind(instance);
    }

    @Override
    public Void visit(Trait statement) {
        define(statement.name().lexeme(), null);

        Map<String, E30Function> methods = applyTraits(statement.traits());

        for (var method : statement.methods()) {
            if (methods.containsKey(method.name().lexeme())) {
                throw new E30RuntimeError(method.name(), "Method '" + method.name().lexeme() + "' already defined.");
            }
            E30Function function = new E30Function(method, environment, false);
            methods.put(method.name().lexeme(), function);
        }

        E30Trait trait = new E30Trait(statement.name(), methods);

        Integer distance = locals.get(statement);
        if (distance != null) {
            environment.assign(distance, slots.get(statement), trait);
            return null;
        }

        globals.put(statement.name().lexeme(), trait);

        return null;
    }

    private Map<String, E30Function> applyTraits(List<Expression> traits) {
        Map<String, E30Function> methods = new HashMap<String, E30Function>();

        for (Expression traitExpression : traits) {
            Object traitObject = evaluate(traitExpression);
            if (!(traitObject instanceof E30Trait)) {
                Token name = ((Expression.Variable) traitExpression).name();
                throw new E30RuntimeError(name, "Only traits can be applied.");
            }

            E30Trait trait = (E30Trait) traitObject;
            for (var name : trait.methods().keySet()) {
                if (methods.containsKey(name)) {
                    throw new E30RuntimeError("Method '" + name + "' already defined.");
                }

                methods.put(name, trait.methods().get(name));
            }
        }

        return methods;
    }

    @Override
    public Object visit(Index expression) {
        E30Array array;
        Integer distance = locals.get(expression);
        if (distance != null) {
            array = (E30Array) environment.fetch(distance, slots.get(expression));
        } else {
            array = (E30Array) globals.get(expression.name().lexeme());
        }

        Object index = evaluate(expression.index());
        if (!(index instanceof Double)) {
            throw new E30RuntimeError("Array index must be a number.");
        }

        int i = ((Double) index).intValue();

        if (i < 0 || i >= array.length()) {
            throw new E30RuntimeError("Array index out of bounds.");
        }

        return array.getValue(i);
    }

    @Override
    public Object visit(IndexGet expression) {
        Double value = (Double) evaluate(expression.size());
        int length = value.intValue();
        if (length < 0) {
            throw new E30RuntimeError("Array size must be a positive number.");
        }
        return new E30Array(length);
    }

    @Override
    public Object visit(IndexSet expression) {
        E30Array array;
        Integer distance = locals.get(expression);
        if (distance != null) {
            array = (E30Array) environment.fetch(distance, slots.get(expression));
        } else {
            array = (E30Array) globals.get(expression.name().lexeme());
        }
        Object index = evaluate(expression.index());
        if (!(index instanceof Double)) {
            throw new E30RuntimeError("Array index must be a number.");
        }

        int i = ((Double) index).intValue();
        
        if (i < 0 || i >= array.length()) {
            throw new E30RuntimeError("Array index out of bounds.");
        }

        Object value = evaluate(expression.value());
        array.setValue(i, value);
        return value;
    }

    @Override
    public Void visit(Use statement) {
        for (var moduleName : statement.modules()) {
            E30Module module = switch (moduleName.lexeme()) {
                case "internal" -> throw new E30RuntimeError(moduleName, "Module 'internal' already loaded.");
                case "base"     -> throw new E30RuntimeError(moduleName, "Module 'base' already loaded.");
                case "math"     -> new E30ModuleMath();
                case "io"       -> new E30ModuleIO();
                default         -> null;
            };
    
            if (module == null) {
                throw new E30RuntimeError(moduleName, "Module '" + moduleName + "'does not exist.");
            }
            module.inject(globals);
        }

        return null;
    }

    public void resolve(Expression expression, int depth, int slot) {
        locals.put(expression, depth);
        slots.put(expression, slot);
    }

    public void executeBlock(List<Statement> statements, Environment environment) {
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

    private void define(Token name, Object value) {
        if (environment != null) {
            environment.define(value);
        } else {
            globals.put(name.lexeme(), value);
        }
    }

    private void define(String name, Object value) {
        if (environment != null) {
            environment.define(value);
        } else {
            globals.put(name, value);
        }
    }

    private Object fetchVariable(Token name, Expression expression) {
        Integer distance = locals.get(expression);

        if (distance != null) {
            return environment.fetch(distance, slots.get(expression));
        }

        if (globals.containsKey(name.lexeme())) {
            return globals.get(name.lexeme());
        }

        throw new E30RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
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
}
