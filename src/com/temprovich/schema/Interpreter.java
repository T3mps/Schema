package com.temprovich.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.temprovich.schema.Expression.Assign;
import com.temprovich.schema.Expression.Attribute;
import com.temprovich.schema.Expression.Call;
import com.temprovich.schema.Expression.Index;
import com.temprovich.schema.Expression.IndexGet;
import com.temprovich.schema.Expression.IndexSet;
import com.temprovich.schema.Expression.Logical;
import com.temprovich.schema.Expression.Parent;
import com.temprovich.schema.Expression.Self;
import com.temprovich.schema.Expression.Set;
import com.temprovich.schema.Expression.Variable;
import com.temprovich.schema.Statement.Auto;
import com.temprovich.schema.Statement.Block;
import com.temprovich.schema.Statement.Break;
import com.temprovich.schema.Statement.Continue;
import com.temprovich.schema.Statement.Expr;
import com.temprovich.schema.Statement.If;
import com.temprovich.schema.Statement.Trait;
import com.temprovich.schema.Statement.Use;
import com.temprovich.schema.Statement.While;
import com.temprovich.schema.error.SchemaRuntimeError;
import com.temprovich.schema.instance.SchemaArray;
import com.temprovich.schema.instance.SchemaCallable;
import com.temprovich.schema.instance.SchemaFunction;
import com.temprovich.schema.instance.SchemaInstance;
import com.temprovich.schema.instance.SchemaNode;
import com.temprovich.schema.lexer.Token;
import com.temprovich.schema.lexer.TokenType;
import com.temprovich.schema.module.SchemaModule;
import com.temprovich.schema.module.SchemaModuleBase;
import com.temprovich.schema.module.SchemaModuleIO;
import com.temprovich.schema.module.SchemaModuleInternal;
import com.temprovich.schema.module.SchemaModuleMath;
import com.temprovich.schema.throwables.BreakException;

public class Interpreter implements Expression.Visitor<Object>,
                                    Statement.Visitor<Void> {

    private static SchemaModule[] preincluded = new SchemaModule[] {
        new SchemaModuleInternal(),
        new SchemaModuleBase()
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
        } catch (SchemaRuntimeError error) {
            Schema.runtimeError(error);
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
        } else if (object instanceof SchemaCallable) {
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
            default: throw new SchemaRuntimeError("unknown operator");
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

                throw new SchemaRuntimeError(expression.operator(), "Operands must be two numbers or two strings."); // throw an error if the attempted operation is neither addition nor concatenation
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
                throw new SchemaRuntimeError("unknown operator");
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

            if (!(parent instanceof SchemaNode)) {
                throw new SchemaRuntimeError(statement.parent().name(), "Parent must be a node.");
            }
        }

        define(statement.name(), null);

        if (statement.parent() != null) {
            environment = new Environment(environment);
            define("parent", parent);
        }

        var metaMethods = applyTraits(statement.traits());
        for (var method : statement.metaMethods()) {
            SchemaFunction function = new SchemaFunction(method, environment, false);
            metaMethods.put(method.name().lexeme(), function);
        }

        SchemaNode metaNode = new SchemaNode(null, (SchemaNode) parent, statement.name().lexeme() + ":metanode", metaMethods);

        var methods = applyTraits(statement.traits());
        for (var method : statement.methods()) {
            SchemaFunction function = new SchemaFunction(method, environment, method.name().lexeme().equals("define"));
            methods.put(method.name().lexeme(), function);
        }

        SchemaNode node = new SchemaNode(metaNode, (SchemaNode) parent, statement.name().lexeme(),  methods);
        
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
                throw new SchemaRuntimeError(expression.name(), "Undefined variable '" + expression.name().lexeme() + "'.");
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

        if (!(callee instanceof SchemaCallable)) {
            throw new SchemaRuntimeError(expression.paren(), "Can only call functions and classes.");
        }

        SchemaCallable function = (SchemaCallable) callee;

        if (arguments.size() != function.arity() && !function.isVariadic()) {
            throw new SchemaRuntimeError(expression.paren(), "Function received " + arguments.size() + " arguments, but expects " + function.arity() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Void visit(Statement.Function statement) {
        SchemaFunction function = new SchemaFunction(statement, environment, false);
        define(statement.name(), function);
        return null;
    }

    @Override
    public Object visit(Expression.Function expression) {
        return new SchemaFunction(new Statement.Function(null, expression), environment, false);
    }

    @Override
    public Void visit(Statement.Return statement) {
        Object value = null;
        if (statement.value() != null) {
            value = evaluate(statement.value());
        }

        throw new com.temprovich.schema.throwables.ReturnException(value);
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
        if (object instanceof SchemaInstance) {
            var result = ((SchemaInstance) object).get(expression.name());
            if (result instanceof SchemaFunction && ((SchemaFunction) result).isGetter()) {
                return ((SchemaFunction) result).call(this, null);
            }

            return result;
        }
        
        throw new SchemaRuntimeError(expression.name(), "Only qualified instances have accessible attributes.");
    }

    @Override
    public Object visit(Set expression) {
        var object = evaluate(expression.object());
        if (!(object instanceof SchemaInstance)) {
            throw new SchemaRuntimeError(expression.name(), "Only instances have fields.");
        }

        Object value = evaluate(expression.value());
        ((SchemaInstance) object).set(expression.name(), value);
        return value;
    }

    @Override
    public Object visit(Self expression) {
        return fetchVariable(expression.keyword(), expression);
    }

    @Override
    public Object visit(Parent expression) {
        int distance = locals.get(expression);
        SchemaNode parent = (SchemaNode) environment.fetch(distance, 0);
        
        SchemaInstance instance = (SchemaInstance) environment.fetch(distance - 1, 0);

        SchemaFunction method = parent.fetchMethod(expression.method().lexeme());

        if (method == null) {
            throw new SchemaRuntimeError(expression.method(), "Undefined property '" + expression.method().lexeme() + "'.");
        }

        return method.bind(instance);
    }

    @Override
    public Void visit(Trait statement) {
        define(statement.name().lexeme(), null);

        Map<String, SchemaFunction> methods = applyTraits(statement.traits());

        for (var method : statement.methods()) {
            if (methods.containsKey(method.name().lexeme())) {
                throw new SchemaRuntimeError(method.name(), "Method '" + method.name().lexeme() + "' already defined.");
            }
            SchemaFunction function = new SchemaFunction(method, environment, false);
            methods.put(method.name().lexeme(), function);
        }

        SchemaTrait trait = new SchemaTrait(statement.name(), methods);

        Integer distance = locals.get(statement);
        if (distance != null) {
            environment.assign(distance, slots.get(statement), trait);
            return null;
        }

        globals.put(statement.name().lexeme(), trait);

        return null;
    }

    private Map<String, SchemaFunction> applyTraits(List<Expression> traits) {
        Map<String, SchemaFunction> methods = new HashMap<String, SchemaFunction>();

        for (Expression traitExpression : traits) {
            Object traitObject = evaluate(traitExpression);
            if (!(traitObject instanceof SchemaTrait)) {
                Token name = ((Expression.Variable) traitExpression).name();
                throw new SchemaRuntimeError(name, "Only traits can be applied.");
            }

            SchemaTrait trait = (SchemaTrait) traitObject;
            for (var name : trait.methods().keySet()) {
                if (methods.containsKey(name)) {
                    throw new SchemaRuntimeError("Method '" + name + "' already defined.");
                }

                methods.put(name, trait.methods().get(name));
            }
        }

        return methods;
    }

    @Override
    public Object visit(Index expression) {
        SchemaArray array;
        Integer distance = locals.get(expression);
        if (distance != null) {
            array = (SchemaArray) environment.fetch(distance, slots.get(expression));
        } else {
            array = (SchemaArray) globals.get(expression.name().lexeme());
        }

        Object index = evaluate(expression.index());
        if (!(index instanceof Double)) {
            throw new SchemaRuntimeError("Array index must be a number.");
        }

        int i = ((Double) index).intValue();

        if (i < 0 || i >= array.length()) {
            throw new SchemaRuntimeError("Array index out of bounds.");
        }

        return array.getValue(i);
    }

    @Override
    public Object visit(IndexGet expression) {
        Double value = (Double) evaluate(expression.size());
        int length = value.intValue();
        if (length < 0) {
            throw new SchemaRuntimeError("Array size must be a positive number.");
        }
        return new SchemaArray(length);
    }

    @Override
    public Object visit(IndexSet expression) {
        SchemaArray array;
        Integer distance = locals.get(expression);
        if (distance != null) {
            array = (SchemaArray) environment.fetch(distance, slots.get(expression));
        } else {
            array = (SchemaArray) globals.get(expression.name().lexeme());
        }
        Object index = evaluate(expression.index());
        if (!(index instanceof Double)) {
            throw new SchemaRuntimeError("Array index must be a number.");
        }

        int i = ((Double) index).intValue();
        
        if (i < 0 || i >= array.length()) {
            throw new SchemaRuntimeError("Array index out of bounds.");
        }

        Object value = evaluate(expression.value());
        array.setValue(i, value);
        return value;
    }

    @Override
    public Void visit(Use statement) {
        for (var moduleName : statement.modules()) {
            SchemaModule module = switch (moduleName.lexeme()) {
                case "internal" -> throw new SchemaRuntimeError(moduleName, "Module 'internal' already loaded.");
                case "base"     -> throw new SchemaRuntimeError(moduleName, "Module 'base' already loaded.");
                case "math"     -> new SchemaModuleMath();
                case "io"       -> new SchemaModuleIO();
                default         -> null;
            };
    
            if (module == null) {
                throw new SchemaRuntimeError(moduleName, "Module '" + moduleName + "'does not exist.");
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

        throw new SchemaRuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
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
        
        throw new SchemaRuntimeError(operator, "Malformed expression detected: attempted to operate on " + left.getClass().getName() + ", " + right.getClass().getName() + " with operator " + operator.type());
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
