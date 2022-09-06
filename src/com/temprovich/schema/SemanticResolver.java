package com.temprovich.schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.temprovich.schema.Expression.Attribute;
import com.temprovich.schema.Expression.Index;
import com.temprovich.schema.Expression.IndexGet;
import com.temprovich.schema.Expression.IndexSet;
import com.temprovich.schema.Expression.Parent;
import com.temprovich.schema.Expression.Self;
import com.temprovich.schema.Expression.Set;
import com.temprovich.schema.Statement.Trait;
import com.temprovich.schema.Statement.Use;
import com.temprovich.schema.lexer.Token;

public class SemanticResolver implements Expression.Visitor<Void>, Statement.Visitor<Void> {

    private enum FunctionType {
        NONE,
        FUNCTION,
        DEFINITION,
        METHOD;
    }

    private final Interpreter interpreter;
    FunctionType currentFunction = FunctionType.NONE;
    private final Stack<Map<String, Variable>> scopes;
    
    public SemanticResolver(Interpreter interpreter) {
        this.interpreter = interpreter;
        this.scopes = new Stack<Map<String, Variable>>();
    }

    @Override
    public Void visit(Expression.Literal statement) {
        return null;
    }

    @Override
    public Void visit(Expression.Grouping statement) {
        resolve(statement.expression());
        return null;
    }

    @Override
    public Void visit(Expression.Unary statement) {
        resolve(statement.right());
        return null;
    }

    @Override
    public Void visit(Expression.Binary statement) {
        resolve(statement.left());
        resolve(statement.right());
        return null;
    }

    @Override
    public Void visit(Expression.Call statement) {
        resolve(statement.callee());

        for (var argument : statement.arguments()) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visit(Expression.Assign statement) {
        resolve(statement.value());
        resolveLocal(statement, statement.name());
        return null;
    }

    @Override
    public Void visit(Expression.Variable statement) {
        if (!scopes.isEmpty() &&
            scopes.peek().containsKey(statement.name().lexeme()) &&
            !scopes.peek().get(statement.name().lexeme()).defined) {
                Schema.error(statement.name(), "Cannot read local variable in its own initializer.");
        }

        resolveLocal(statement, statement.name());
        return null;
    }

    @Override
    public Void visit(Expression.Logical statement) {
        resolve(statement.left());
        resolve(statement.right());
        return null;
    }

    @Override
    public Void visit(Statement.Block statement) {
        beginScope();
        resolve(statement.statements());
        endScope();

        return null;
    }

    @Override
    public Void visit(Statement.Expr statement) {
        resolve(statement.expression());
        return null;
    }

    @Override
    public Void visit(Statement.Function statement) {
        declare(statement.name());
        define(statement.name());
        resolveFunction(statement.function(), FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visit(Expression.Function statement) {
        resolveFunction(statement, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visit(Statement.If statement) {
        resolve(statement.condition());
        resolve(statement.thenBranch());
        if (statement.elseBranch() != null) {
            resolve(statement.elseBranch());
        }
        return null;
    }

    @Override
    public Void visit(Statement.Auto statement) {
        declare(statement.name());
        if (statement.value() != null) {
            resolve(statement.value());
        }
        define(statement.name());

        return null;
    }

    @Override
    public Void visit(Statement.While statement) {
        resolve(statement.condition());
        resolve(statement.body());
        return null;
    }

    @Override
    public Void visit(Statement.Return statement) {
        if (currentFunction == FunctionType.NONE) {
            Schema.error(statement.keyword(), "Cannot return from top-level code.");
        }

        if (statement.value() != null) {
            if (currentFunction == FunctionType.DEFINITION) {
                Schema.error(statement.keyword(), "Cannot return a value from a definition.");
            }
            resolve(statement.value());
        }

        return null;
    }

    @Override
    public Void visit(Statement.Break statement) {
        return null;
    }

    @Override
    public Void visit(Statement.Continue statement) {
        return null;
    }

    @Override
    public Void visit(Statement.Node statement) {
        NodeType enclosingNode = currentNodeType;
        currentNodeType = NodeType.NODE;

        declare(statement.name());
        define(statement.name());

        var parent = statement.parent();

        if (parent != null) {
            if (statement.name().lexeme().equals(parent.name().lexeme())) {
                Schema.error(parent.name(), "A node cannot inherit from itself.");
            }

            currentNodeType = NodeType.CHILD;
            resolve(statement.parent());
        }

        if (statement.parent() != null) {
            beginScope();
            scopes.peek().put("parent", new Variable(0));
        }

        beginScope();
        scopes.peek().put("self", new Variable(0));

        for (var method : statement.methods()) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.name().lexeme().equals("define")) {
                declaration = FunctionType.DEFINITION;
            }
            resolveFunction(method.function(), declaration);
        }

        for (var method : statement.metaMethods()) {
            beginScope();
            scopes.peek().put("self", new Variable(0));
            resolveFunction(method.function(), FunctionType.METHOD);
            endScope();
        }

        endScope();

        if (statement.parent() != null) {
            endScope();
        }

        currentNodeType = enclosingNode;
        return null;
    }

    @Override
    public Void visit(Attribute expression) {
        resolve(expression.object());
        return null;
    }

    @Override
    public Void visit(Set expression) {
        resolve(expression.value());
        resolve(expression.object());
        return null;
    }

    @Override
    public Void visit(Self expression) {
        if (currentNodeType == NodeType.NONE) {
            Schema.error(expression.keyword(), "Cannot use 'self' outside of a node.");
            return null;
        }

        resolveLocal(expression, expression.keyword());
        return null;
    }

    @Override
    public Void visit(Parent expression) {
        if (currentNodeType == NodeType.NONE) {
            Schema.error(expression.keyword(), "Cannot use 'parent' outside of a node.");
            return null;
        }
        if (currentNodeType == NodeType.TRAIT) {
            Schema.error(expression.keyword(), "Cannot use 'parent' in a trait.");
            return null;
        }
        if (currentNodeType != NodeType.CHILD) {
            Schema.error(expression.keyword(), "Cannot use 'parent' in a node with no parent.");
            return null;
        }
        
        resolveLocal(expression, expression.keyword());
        return null;
    }

    @Override
    public Void visit(Trait statement) {
        declare(statement.name());
        define(statement.name());

        NodeType enclosingClass = currentNodeType;
        currentNodeType = NodeType.TRAIT;

        for (var trait : statement.traits()) {
            resolve(trait);
        }

        beginScope();
        scopes.peek().put("self", new Variable(0));

        for (var method : statement.methods()) {
            FunctionType declaration = FunctionType.METHOD;
            resolveFunction(method.function(), declaration);
        }

        endScope();

        currentNodeType = enclosingClass;
        return null;
    }

    @Override
    public Void visit(Index expression) {
        declare(expression.name());
        resolve(expression.index());
        return null;
    }

    @Override
    public Void visit(IndexGet expression) {
        resolve(expression.size());
        return null;
    }

    @Override
    public Void visit(IndexSet expression) {
        declare(expression.name());
        resolve(expression.index());
        resolve(expression.value());
        return null;
    }

    @Override
    public Void visit(Use statement) {
        // nothing to handle
        return null;
    }

    private enum NodeType {
        NONE,
        NODE,
        CHILD,
        TRAIT;
    }

    private NodeType currentNodeType = NodeType.NONE;

    public void resolve(List<Statement> statements) {
        for (var statement : statements) {
            resolve(statement);
        }
    }

    public void resolve(Statement statement) {
        statement.accept(this);
    }

    public void resolve(Expression expression) {
        expression.accept(this);
    }

    public void beginScope() {
        scopes.push(new HashMap<String, Variable>());
    }

    public void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) {
            return;
        }

        var scope = scopes.peek();
        if (scope.containsKey(name.lexeme())) {
            Schema.error(name, "Variable with this name already declared in this scope.");
        }

        scope.put(name.lexeme(), new Variable(scope.size()));
    }

    private void define(Token name) {
        if (scopes.isEmpty()) {
            return;
        }

        scopes.peek().get(name.lexeme()).defined = true;
    }

    private void resolveLocal(Expression expression, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            var scope = scopes.get(i);
            if (scope.containsKey(name.lexeme())) {
                interpreter.resolve(expression, scopes.size() - 1 - i, scope.get(name.lexeme()).slot);
                return;
            }
        }
    }

    private void resolveFunction(Expression.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        if (function.parameters() != null) {
            for (var param : function.parameters()) {
                declare(param);
                define(param);
            }
        }
        resolve(function.body());
        endScope();

        currentFunction = enclosingFunction;
    }

    private class Variable {
        boolean defined = false;
        final int slot;

        private Variable(int slot) {
            this.slot = slot;
        }
    }
}