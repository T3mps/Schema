package com.temprovich.e30.preinclude;

import java.util.List;

import com.temprovich.e30.E30Callable;
import com.temprovich.e30.Environment;
import com.temprovich.e30.Interpreter;

public final class E30NativeMath implements Preinclude {

    /*
     * PI: Represents the mathematical constant PI.
     */
    private static final Definition PI = new Definition("PI", Math.PI);

    /*
     * E: Represents the mathematical constant E.
     */
    private static final Definition E = new Definition("E", Math.E);

    /*
     * abs(x): Returns the absolute value of x.
     */
    private static final Definition ABS = new Definition("abs", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.abs((Double) arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * max(x, y): Returns the larger of x and y.
     */
    private static final Definition MAX = new Definition("max", new E30Callable() {

        @Override
        public int arity() { return 2; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.max((Double) arguments.get(0), (Double) arguments.get(1));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * min(x, y): Returns the smaller of x and y.
     */
    private static final Definition MIN = new Definition("min", new E30Callable() {

        @Override
        public int arity() { return 2; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.min((Double) arguments.get(0), (Double) arguments.get(1));
        }

        @Override
        public String toString() { return "<native function>"; }
    });
    
    /*
     * round(x): Returns the nearest integer to x.
     */
    private static final Definition ROUND = new Definition("round", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.round((Double) arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * floor(x): Returns the largest integer less than or equal to x.
     */
    private static final Definition FLOOR = new Definition("floor", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.floor((Double) arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * ceil(x): Returns the smallest integer greater than or equal to x.
     */
    private static final Definition CEIL = new Definition("ceil", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.ceil((Double) arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * sqrt(x): Returns the square root of x.
     */
    private static final Definition SQRT = new Definition("sqrt", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.sqrt((Double) arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * cbrt(x): Returns the cube root of x.
     */
    private static final Definition CBRT = new Definition("cbrt", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.cbrt((Double) arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * pow(x, y): Returns x raised to the power of y.
     */
    private static final Definition POW = new Definition("pow", new E30Callable() {

        @Override
        public int arity() { return 2; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.pow((Double) arguments.get(0), (Double) arguments.get(1));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * signum(x): Returns the signum function of x.
     */
    private static final Definition SIGNUM = new Definition("signum", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.signum((Double) arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * sign(x): Alias for signum(x).
     */
    private static final Definition SIGN = new Definition("sign", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.signum((Double) arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * random(): Returns a random number between 0 and 1.
     */
    private static final Definition RANDOM = new Definition("random", new E30Callable() {

        @Override
        public int arity() { return 0; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.random();
        }

        @Override
        public String toString() { return "<native function>"; }
    });
    
    /*
     * ln(x): Returns the natural logarithm of x.
     */
    private static final Definition LN = new Definition("ln", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.log((Double) arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * log(x): Returns the base-10 logarithm of x.
     */
    private static final Definition LOG10 = new Definition("log10", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.log10((Double) arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * log(x, base): Returns the logarithm of x with the given base.
     */
    private static final Definition LOG_BASE = new Definition("log", new E30Callable() {

        @Override
        public int arity() { return 2; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.log((Double) arguments.get(0)) / Math.log((Double) arguments.get(1));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * sin(x): Returns the sine of x.
     */
    private static final Definition SIN = new Definition("sin", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.sin((Double) arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * cos(x): Returns the cosine of x.
     */
    private static final Definition COS = new Definition("cos", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.cos((Double) arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * tan(x): Returns the tangent of x.
     */
    private static final Definition TAN = new Definition("tan", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.tan((Double) arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * asin(x): Returns the arcsine of x.
     */
    private static final Definition ASIN = new Definition("asin", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.asin((Double) arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * acos(x): Returns the arccosine of x.
     */
    private static final Definition ACOS = new Definition("acos", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.acos((Double) arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * atan(x): Returns the arctangent of x.
     */
    private static final Definition ATAN = new Definition("atan", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.atan((Double) arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * atan2(y, x): Returns the arctangent of y/x.
     */
    private static final Definition ATAN2 = new Definition("atan2", new E30Callable() {

        @Override
        public int arity() { return 2; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.atan2((Double) arguments.get(0), (Double) arguments.get(1));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * sinh(x): Returns the hyperbolic sine of x.
     */
    private static final Definition SINH = new Definition("sinh", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.sinh((Double) arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * cosh(x): Returns the hyperbolic cosine of x.
     */
    private static final Definition COSH = new Definition("cosh", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.cosh((Double) arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * tanh(x): Returns the hyperbolic tangent of x.
     */
    private static final Definition TANH = new Definition("tanh", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.tanh((Double) arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * asinh(x): Returns the inverse hyperbolic sine of x.
     */
    private static final Definition ASINH = new Definition("asinh", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            double x = (Double) arguments.get(0);
            double sign = 1.0;
            if (x < 0.0) {
                sign = -1.0;
                x = -x;
            }
            return sign * Math.log(x + Math.sqrt(x * x + 1.0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * acosh(x): Returns the inverse hyperbolic cosine of x.
     */
    private static final Definition ACOSH = new Definition("acosh", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            double x = (Double) arguments.get(0);
            return Math.log(x + Math.sqrt(x * x - 1.0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * atanh(x): Returns the inverse hyperbolic tangent of x.
     */
    private static final Definition ATANH = new Definition("atanh", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            double x = (Double) arguments.get(0);
            return 0.5 * Math.log((1.0 + x) / (1.0 - x));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * degrees(x): Converts x from radians to degrees.
     */
    private static final Definition DEGREES = new Definition("degrees", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return (Double) arguments.get(0) * 180.0 / Math.PI;
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * radians(x): Converts x from degrees to radians.
     */
    private static final Definition RADIANS = new Definition("radians", new E30Callable() {

        @Override
        public int arity() { return 1; }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return (Double) arguments.get(0) * Math.PI / 180.0;
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    @Override
    public void inject(Environment environment) {
        PI.inject(environment);
        E.inject(environment);
        ABS.inject(environment);
        MAX.inject(environment);
        MIN.inject(environment);
        ROUND.inject(environment);
        FLOOR.inject(environment);
        CEIL.inject(environment);
        SQRT.inject(environment);
        CBRT.inject(environment);
        POW.inject(environment);
        SIGNUM.inject(environment);
        SIGN.inject(environment);
        RANDOM.inject(environment);
        LN.inject(environment);
        LOG10.inject(environment);
        LOG_BASE.inject(environment);
        SIN.inject(environment);
        COS.inject(environment);
        TAN.inject(environment);
        ASIN.inject(environment);
        ACOS.inject(environment);
        ATAN.inject(environment);
        ATAN2.inject(environment);
        SINH.inject(environment);
        COSH.inject(environment);
        TANH.inject(environment);
        ASINH.inject(environment);
        ACOSH.inject(environment);
        ATANH.inject(environment);
        DEGREES.inject(environment);
        RADIANS.inject(environment);
    }
}
