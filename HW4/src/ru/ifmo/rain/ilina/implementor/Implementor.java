package ru.ifmo.rain.ilina.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.Impler;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Implementor implements Impler {

    private final static String TAB = "    ";

    private final static String SPACE = " ";

    private final static String COMMA = ",";

    private final static String EOLN = System.lineSeparator();

    public Implementor() {}

    private static class MethodWrapper {

        private final Method inner;

        private final static int BASE = 37;

        private final static int MOD = (int) (1e9 + 7);

        MethodWrapper(Method other) {
            inner = other;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj instanceof MethodWrapper) {
                MethodWrapper other = (MethodWrapper) obj;
                return Arrays.equals(inner.getParameterTypes(), other.inner.getParameterTypes())
                        && inner.getReturnType().equals(other.inner.getReturnType())
                        && inner.getName().equals(other.inner.getName());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return ((Arrays.hashCode(inner.getParameterTypes())
                    + BASE * inner.getReturnType().hashCode()) % MOD
                    + inner.getName().hashCode() * BASE * BASE) % MOD;
        }

        Method getInner() {
            return inner;
        }
    }

    @Override
    public void implement(Class<?> token, Path output) throws ImplerException {
        if (token == null || output == null) {
            throw new ImplerException("Not-null arguments expected");
        }
        if (token.isPrimitive() || token.isArray() || Modifier.isFinal(token.getModifiers()) || token == Enum.class) {
            throw new ImplerException("Incorrect class token");
        }
        output = output.resolve(token.getPackage().getName().replace('.', File.separatorChar))
                .resolve(token.getSimpleName() + "Impl" + ".java");
        if (output.getParent() != null) {
            try {
                Files.createDirectories(output.getParent());
            } catch (IOException e) {
                throw new ImplerException("Unable to create directories for output file", e);
            }
        }
        try (Writer writer = Files.newBufferedWriter(output)) {
            writer.write(getPackage(token) + "public class " + token.getSimpleName() + "Impl" + SPACE +
                    (token.isInterface() ? "implements" : "extends") + SPACE +
                    token.getSimpleName() + SPACE + "{" + EOLN);
            if (!token.isInterface()) {
                implementConstructors(token, writer);
            }
            implementAbstractMethods(token, writer);
            writer.write("}" + EOLN);
        } catch (IOException e) {
            throw new ImplerException("Unable to write to output file", e);
        }
    }

    private static String toUnicode(String in) {
        StringBuilder b = new StringBuilder();
        for (char c : in.toCharArray()) {
            if (c >= 128) {
                b.append(String.format("\\u%04X", (int) c));
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }

    private static String getTabs(int cnt) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < cnt; i++) {
            res.append(TAB);
        }
        return res.toString();
    }

    private static String getClassName(Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    private static String getDefaultValue(Class<?> token) {
        if (token.equals(boolean.class)) {
            return " false";
        } else if (token.equals(void.class)) {
            return "";
        } else if (token.isPrimitive()) {
            return " 0";
        }
        return " null";
    }

    private static String getPackage(Class<?> token) {
        StringBuilder res = new StringBuilder();
        if (!token.getPackage().getName().equals("")) {
            res.append("package" + SPACE).append(token.getPackage().getName()).append(";").append(EOLN);
        }
        res.append(EOLN);
        return res.toString();
    }

    private static void createDirectories(Path path) throws ImplerException {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new ImplerException("Unable to create directories for output file", e);
            }
        }
    }

    private static String getParam(Parameter param, boolean typeNeeded) {
        return (typeNeeded ? param.getType().getCanonicalName() + SPACE : "") + param.getName();
    }

    private static String getParams(Executable exec, boolean typedNeeded) {
        return Arrays.stream(exec.getParameters())
                .map(param -> getParam(param, typedNeeded))
                .collect(Collectors.joining(COMMA + SPACE, "(", ")"));
    }

    private static String getExceptions(Executable exec) {
        StringBuilder res = new StringBuilder();
        Class<?>[] exceptions = exec.getExceptionTypes();
        if (exceptions.length > 0) {
            res.append(SPACE + "throws" + SPACE);
        }
        res.append(Arrays.stream(exceptions)
                .map(Class::getCanonicalName)
                .collect(Collectors.joining(COMMA + SPACE))
        );
        return res.toString();
    }

    private static String getReturnTypeAndName(Executable exec) {
        if (exec instanceof Method) {
            Method tmp = (Method) exec;
            return tmp.getReturnType().getCanonicalName() + SPACE + tmp.getName();
        } else {
            return getClassName(((Constructor<?>) exec).getDeclaringClass());
        }
    }

    private static String getBody(Executable exec) {
        if (exec instanceof Method) {
            return "return" + getDefaultValue(((Method) exec).getReturnType());
        } else {
            return "super" + getParams(exec, false);
        }
    }

    private static String getExecutable(Executable exec) {
        StringBuilder res = new StringBuilder(getTabs(1));
        final int mods = exec.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.NATIVE & ~Modifier.TRANSIENT;
        res.append(Modifier.toString(mods))
                .append(mods > 0 ? SPACE : "")
                .append(getReturnTypeAndName(exec))
                .append(getParams(exec, true))
                .append(getExceptions(exec))
                .append(SPACE)
                .append("{")
                .append(EOLN)
                .append(getTabs(2))
                .append(getBody(exec))
                .append(";")
                .append(EOLN)
                .append(getTabs(1))
                .append("}")
                .append(EOLN);
        return res.toString();
    }

    private static void getAbstractMethods(Method[] methods, Set<MethodWrapper> storage) {
        Arrays.stream(methods)
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .map(MethodWrapper::new)
                .collect(Collectors.toCollection(() -> storage));
    }

    private static void implementAbstractMethods(Class<?> token, Writer writer) throws IOException {
        HashSet<MethodWrapper> methods = new HashSet<>();
        getAbstractMethods(token.getMethods(), methods);
        while (token != null) {
            getAbstractMethods(token.getDeclaredMethods(), methods);
            token = token.getSuperclass();
        }
        for (MethodWrapper method : methods) {
            writer.write(toUnicode(getExecutable(method.getInner())));
        }
    }

    private static void implementConstructors(Class<?> token, Writer writer) throws IOException, ImplerException {
        Constructor<?>[] constructors = Arrays.stream(token.getDeclaredConstructors())
                .filter(constructor -> !Modifier.isPrivate(constructor.getModifiers()))
                .toArray(Constructor<?>[]::new);
        if (constructors.length == 0) {
            throw new ImplerException("No non-private constructors in class");
        }
        for (Constructor<?> constructor : constructors) {
            writer.write(toUnicode(getExecutable(constructor)));
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            System.out.println("Two arguments expected");
            return;
        } else if (args[0] == null || args[1] == null) {
            System.out.println("Two arguments must be not-null");
            return;
        }
        Impler implementor = new Implementor();
        try {
            implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
        } catch (InvalidPathException e) {
            System.out.println("Incorrect path: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Incorrect class name: " + e.getMessage());
        } catch (ImplerException e) {
            System.out.println("An error occurred during implementation: " + e.getMessage());
        }
    }
}