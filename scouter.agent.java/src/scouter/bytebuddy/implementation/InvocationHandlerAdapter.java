// Generated by delombok at Sun Feb 26 12:31:38 KST 2017
package scouter.bytebuddy.implementation;

import scouter.bytebuddy.description.field.FieldDescription;
import scouter.bytebuddy.description.method.MethodDescription;
import scouter.bytebuddy.description.type.TypeDescription;
import scouter.bytebuddy.description.type.TypeList;
import scouter.bytebuddy.dynamic.scaffold.FieldLocator;
import scouter.bytebuddy.dynamic.scaffold.InstrumentedType;
import scouter.bytebuddy.implementation.bytecode.ByteCodeAppender;
import scouter.bytebuddy.implementation.bytecode.StackManipulation;
import scouter.bytebuddy.implementation.bytecode.assign.Assigner;
import scouter.bytebuddy.implementation.bytecode.collection.ArrayFactory;
import scouter.bytebuddy.implementation.bytecode.constant.MethodConstant;
import scouter.bytebuddy.implementation.bytecode.member.FieldAccess;
import scouter.bytebuddy.implementation.bytecode.member.MethodInvocation;
import scouter.bytebuddy.implementation.bytecode.member.MethodReturn;
import scouter.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import scouter.bytebuddy.utility.RandomString;
import scouter.bytebuddy.jar.asm.MethodVisitor;
import scouter.bytebuddy.jar.asm.Opcodes;
import scouter.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.List;
import static scouter.bytebuddy.matcher.ElementMatchers.genericFieldType;

/**
 * An adapter for adapting an {@link java.lang.reflect.InvocationHandler}. The adapter allows the invocation handler
 * to also intercept method calls to non-interface methods.
 */
public abstract class InvocationHandlerAdapter implements Implementation {
    /**
     * A type description of the {@link InvocationHandler}.
     */
    private static final TypeDescription.Generic INVOCATION_HANDLER_TYPE = new TypeDescription.Generic.OfNonGenericType.ForLoadedType(InvocationHandler.class);
    /**
     * Indicates that a value should not be cached.
     */
    private static final boolean NO_CACHING = false;
    /**
     * Indicates that a {@link java.lang.reflect.Method} should be cached.
     */
    protected static final boolean CACHING = true;
    /**
     * The name of the field for storing an invocation handler.
     */
    protected final String fieldName;
    /**
     * The assigner that is used for assigning the return invocation handler's return value to the
     * intercepted method's return value.
     */
    protected final Assigner assigner;
    /**
     * Determines if the {@link java.lang.reflect.Method} instances that are handed to the intercepted methods are
     * cached in {@code static} fields.
     */
    protected final boolean cacheMethods;

    /**
     * Creates a new invocation handler for a given field.
     *
     * @param fieldName    The name of the field.
     * @param cacheMethods Determines if the {@link java.lang.reflect.Method} instances that are handed to the
     * intercepted methods are cached in {@code static} fields.
     * @param assigner     The assigner to apply when defining this implementation.
     */
    protected InvocationHandlerAdapter(String fieldName, boolean cacheMethods, Assigner assigner) {
        this.fieldName = fieldName;
        this.cacheMethods = cacheMethods;
        this.assigner = assigner;
    }

    /**
     * Creates an implementation for any instance of an {@link java.lang.reflect.InvocationHandler} that delegates
     * all method interceptions to the given instance which will be stored in a {@code static} field.
     *
     * @param invocationHandler The invocation handler to which all method calls are delegated.
     * @return An implementation that delegates all method interceptions to the given invocation handler.
     */
    public static InvocationHandlerAdapter of(InvocationHandler invocationHandler) {
        return of(invocationHandler, String.format("%s$%s", ForInstance.PREFIX, RandomString.hashOf(invocationHandler.hashCode())));
    }

    /**
     * Creates an implementation for any instance of an {@link java.lang.reflect.InvocationHandler} that delegates
     * all method interceptions to the given instance which will be stored in a {@code static} field.
     *
     * @param invocationHandler The invocation handler to which all method calls are delegated.
     * @param fieldName         The name of the field.
     * @return An implementation that delegates all method interceptions to the given invocation handler.
     */
    public static InvocationHandlerAdapter of(InvocationHandler invocationHandler, String fieldName) {
        return new ForInstance(fieldName, CACHING, Assigner.DEFAULT, invocationHandler);
    }

    /**
     * Creates an implementation for any {@link java.lang.reflect.InvocationHandler} that delegates
     * all method interceptions to a field with the given name. This field has to be of a subtype of invocation
     * handler and needs to be set before any invocations are intercepted. Otherwise, a {@link java.lang.NullPointerException}
     * will be thrown.
     *
     * @param name The name of the field.
     * @return An implementation that delegates all method interceptions to an instance field of the given name.
     */
    public static InvocationHandlerAdapter toField(String name) {
        return toField(name, FieldLocator.ForClassHierarchy.Factory.INSTANCE);
    }

    /**
     * Creates an implementation for any {@link java.lang.reflect.InvocationHandler} that delegates
     * all method interceptions to a field with the given name. This field has to be of a subtype of invocation
     * handler and needs to be set before any invocations are intercepted. Otherwise, a {@link java.lang.NullPointerException}
     * will be thrown.
     *
     * @param name                The name of the field.
     * @param fieldLocatorFactory The field locator factory
     * @return An implementation that delegates all method interceptions to an instance field of the given name.
     */
    public static InvocationHandlerAdapter toField(String name, FieldLocator.Factory fieldLocatorFactory) {
        return new ForField(name, CACHING, Assigner.DEFAULT, fieldLocatorFactory);
    }

    /**
     * Returns a list of stack manipulations that loads all arguments of an instrumented method.
     *
     * @param instrumentedMethod The method that is instrumented.
     * @return A list of stack manipulation that loads all arguments of an instrumented method.
     */
    private List<StackManipulation> argumentValuesOf(MethodDescription instrumentedMethod) {
        TypeList.Generic parameterTypes = instrumentedMethod.getParameters().asTypeList();
        List<StackManipulation> instruction = new ArrayList<StackManipulation>(parameterTypes.size());
        int currentIndex = 1;
        for (TypeDescription.Generic parameterType : parameterTypes) {
            instruction.add(new StackManipulation.Compound(MethodVariableAccess.of(parameterType).loadFrom(currentIndex), assigner.assign(parameterType, TypeDescription.Generic.OBJECT, Assigner.Typing.STATIC)));
            currentIndex += parameterType.getStackSize().getSize();
        }
        return instruction;
    }

    /**
     * By default, any {@link java.lang.reflect.Method} instance that is handed over to an
     * {@link java.lang.reflect.InvocationHandler} is cached in a static field. By invoking this method,
     * this feature can be disabled.
     *
     * @return A similar invocation handler adapter that applies caching.
     */
    public abstract AssignerConfigurable withoutMethodCache();

    /**
     * Applies an implementation that delegates to a invocation handler.
     *
     * @param methodVisitor         The method visitor for writing the byte code to.
     * @param implementationContext The implementation context for the current implementation.
     * @param instrumentedMethod    The method that is instrumented.
     * @param preparingManipulation A stack manipulation that applies any preparation to the operand stack.
     * @param fieldDescription      The field that contains the value for the invocation handler.
     * @return The size of the applied assignment.
     */
    protected ByteCodeAppender.Size apply(MethodVisitor methodVisitor, Context implementationContext, MethodDescription instrumentedMethod, StackManipulation preparingManipulation, FieldDescription fieldDescription) {
        if (instrumentedMethod.isStatic()) {
            throw new IllegalStateException("It is not possible to apply an invocation handler onto the static method " + instrumentedMethod);
        }
        StackManipulation.Size stackSize = new StackManipulation.Compound(preparingManipulation, FieldAccess.forField(fieldDescription).read(), MethodVariableAccess.loadThis(), cacheMethods ? MethodConstant.forMethod(instrumentedMethod.asDefined()).cached() : MethodConstant.forMethod(instrumentedMethod.asDefined()), ArrayFactory.forType(TypeDescription.Generic.OBJECT).withValues(argumentValuesOf(instrumentedMethod)), MethodInvocation.invoke(INVOCATION_HANDLER_TYPE.getDeclaredMethods().getOnly()), assigner.assign(TypeDescription.Generic.OBJECT, instrumentedMethod.getReturnType(), Assigner.Typing.DYNAMIC), MethodReturn.of(instrumentedMethod.getReturnType())).apply(methodVisitor, implementationContext);
        return new ByteCodeAppender.Size(stackSize.getMaximalSize(), instrumentedMethod.getStackSize());
    }


    /**
     * Allows for the configuration of an {@link Assigner}
     * of an {@link InvocationHandlerAdapter}.
     */
    protected interface AssignerConfigurable extends Implementation {
        /**
         * Configures an assigner to use with this invocation handler adapter.
         *
         * @param assigner The assigner to apply when defining this implementation.
         * @return This instrumentation with the given {@code assigner} configured.
         */
        Implementation withAssigner(Assigner assigner);
    }


    /**
     * An implementation of an {@link InvocationHandlerAdapter} that delegates method
     * invocations to an adapter that is stored in a static field.
     */
    protected static class ForInstance extends InvocationHandlerAdapter implements AssignerConfigurable {
        /**
         * The prefix for field that are created for storing the instrumented value.
         */
        private static final String PREFIX = "invocationHandler";
        /**
         * The invocation handler to which method interceptions are to be delegated.
         */
        protected final InvocationHandler invocationHandler;

        /**
         * Creates a new invocation handler adapter for delegating invocations to an invocation handler that is stored
         * in a static field.
         *
         * @param fieldName         The name of the field.
         * @param cacheMethods      Determines if the {@link java.lang.reflect.Method} instances that are handed to the
         * intercepted methods are cached in {@code static} fields.
         * @param assigner          The assigner to apply when defining this implementation.
         * @param invocationHandler The invocation handler to which all method calls are delegated.
         */
        protected ForInstance(String fieldName, boolean cacheMethods, Assigner assigner, InvocationHandler invocationHandler) {
            super(fieldName, cacheMethods, assigner);
            this.invocationHandler = invocationHandler;
        }

        @Override
        public AssignerConfigurable withoutMethodCache() {
            return new ForInstance(fieldName, NO_CACHING, assigner, invocationHandler);
        }

        @Override
        public Implementation withAssigner(Assigner assigner) {
            return new ForInstance(fieldName, cacheMethods, assigner, invocationHandler);
        }

        @Override
        public InstrumentedType prepare(InstrumentedType instrumentedType) {
            return instrumentedType.withField(new FieldDescription.Token(fieldName, Opcodes.ACC_SYNTHETIC | Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, INVOCATION_HANDLER_TYPE)).withInitializer(new LoadedTypeInitializer.ForStaticField(fieldName, invocationHandler));
        }

        @Override
        public ByteCodeAppender appender(Target implementationTarget) {
            return new Appender(implementationTarget.getInstrumentedType());
        }


        /**
         * An appender for implementing the {@link ForInstance}.
         */
        protected class Appender implements ByteCodeAppender {
            /**
             * The instrumented type for which the methods are being intercepted.
             */
            private final TypeDescription instrumentedType;

            /**
             * Creates a new appender.
             *
             * @param instrumentedType The type that is instrumented.
             */
            protected Appender(TypeDescription instrumentedType) {
                this.instrumentedType = instrumentedType;
            }

            @Override
            public Size apply(MethodVisitor methodVisitor, Context implementationContext, MethodDescription instrumentedMethod) {
                return ForInstance.this.apply(methodVisitor, implementationContext, instrumentedMethod, StackManipulation.Trivial.INSTANCE, instrumentedType.getDeclaredFields().filter(ElementMatchers.named(fieldName).and(ElementMatchers.genericFieldType(INVOCATION_HANDLER_TYPE))).getOnly());
            }

            private InvocationHandlerAdapter getInvocationHandlerAdapter() {
                return ForInstance.this;
            }

            // HE: Remove when Lombok support for getOuter is added.
            @Override
            public boolean equals(Object other) {
                return this == other || !(other == null || getClass() != other.getClass()) && instrumentedType.equals(((Appender) other).instrumentedType) && ForInstance.this.equals(((Appender) other).getInvocationHandlerAdapter());
            }

            // HE: Remove when Lombok support for getOuter is added.
            /**
             * Returns the outer class.
             *
             * @return The outer class of this instance.
             */
            @Override
            public int hashCode() {
                return 31 * ForInstance.this.hashCode() + instrumentedType.hashCode();
            }
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof InvocationHandlerAdapter.ForInstance)) return false;
            final InvocationHandlerAdapter.ForInstance other = (InvocationHandlerAdapter.ForInstance) o;
            if (!other.canEqual((java.lang.Object) this)) return false;
            if (!super.equals(o)) return false;
            final java.lang.Object this$invocationHandler = this.invocationHandler;
            final java.lang.Object other$invocationHandler = other.invocationHandler;
            if (this$invocationHandler == null ? other$invocationHandler != null : !this$invocationHandler.equals(other$invocationHandler)) return false;
            return true;
        }

        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        protected boolean canEqual(final java.lang.Object other) {
            return other instanceof InvocationHandlerAdapter.ForInstance;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + super.hashCode();
            final java.lang.Object $invocationHandler = this.invocationHandler;
            result = result * PRIME + ($invocationHandler == null ? 43 : $invocationHandler.hashCode());
            return result;
        }
    }


    /**
     * An implementation of an {@link InvocationHandlerAdapter} that delegates method
     * invocations to an adapter that is stored in an instance field.
     */
    protected static class ForField extends InvocationHandlerAdapter implements AssignerConfigurable {
        /**
         * The field locator factory to use.
         */
        private final FieldLocator.Factory fieldLocatorFactory;

        /**
         * Creates a new invocation handler adapter that loads its value from a field.
         *
         * @param fieldName           The name of the field.
         * @param cacheMethods        Determines if the {@link java.lang.reflect.Method} instances that are handed to the
         *                            intercepted methods are cached in {@code static} fields.
         * @param assigner            The assigner to apply when defining this implementation.
         * @param fieldLocatorFactory The field locator factory to use.
         */
        protected ForField(String fieldName, boolean cacheMethods, Assigner assigner, FieldLocator.Factory fieldLocatorFactory) {
            super(fieldName, cacheMethods, assigner);
            this.fieldLocatorFactory = fieldLocatorFactory;
        }

        @Override
        public AssignerConfigurable withoutMethodCache() {
            return new ForField(fieldName, NO_CACHING, assigner, fieldLocatorFactory);
        }

        @Override
        public Implementation withAssigner(Assigner assigner) {
            return new ForField(fieldName, cacheMethods, assigner, fieldLocatorFactory);
        }

        @Override
        public InstrumentedType prepare(InstrumentedType instrumentedType) {
            return instrumentedType;
        }

        @Override
        public ByteCodeAppender appender(Target implementationTarget) {
            FieldLocator.Resolution resolution = fieldLocatorFactory.make(implementationTarget.getInstrumentedType()).locate(fieldName);
            if (!resolution.isResolved()) {
                throw new IllegalStateException("Could not find a field named \'" + fieldName + "\' for " + implementationTarget.getInstrumentedType());
            } else if (!resolution.getField().getType().asErasure().isAssignableTo(InvocationHandler.class)) {
                throw new IllegalStateException("Field " + resolution.getField() + " does not declare a type that is assignable to invocation handler");
            }
            return new Appender(implementationTarget.getInstrumentedType(), resolution.getField());
        }

        /**
         * An appender for implementing the {@link ForField}.
         */
        protected class Appender implements ByteCodeAppender {
            /**
             * The type that is subject of the instrumentation.
             */
            private final TypeDescription instrumentedType;
            /**
             * The field that contains the invocation handler.
             */
            private final FieldDescription fieldDescription;

            /**
             * Creates a new appender.
             *
             * @param instrumentedType The type that is instrumented.
             * @param fieldDescription The field that contains the invocation handler.
             */
            protected Appender(TypeDescription instrumentedType, FieldDescription fieldDescription) {
                this.instrumentedType = instrumentedType;
                this.fieldDescription = fieldDescription;
            }

            @Override
            public Size apply(MethodVisitor methodVisitor, Context implementationContext, MethodDescription instrumentedMethod) {
                return ForField.this.apply(methodVisitor, implementationContext, instrumentedMethod, fieldDescription.isStatic() ? StackManipulation.Trivial.INSTANCE : MethodVariableAccess.loadThis(), fieldDescription);
            }

            // HE: Remove when Lombok support for getOuter is added.
            @Override
            public boolean equals(Object other) {
                return this == other || !(other == null || getClass() != other.getClass()) && instrumentedType.equals(((Appender) other).instrumentedType) && fieldDescription.equals(((Appender) other).fieldDescription) && ForField.this.equals(((Appender) other).getInvocationHandlerAdapter());
            }

            /**
             * Returns the outer class.
             *
             * @return The outer class.
             */
            private InvocationHandlerAdapter getInvocationHandlerAdapter() {
                return ForField.this;
            }

            // HE: Remove when Lombok support for getOuter is added.
            @Override
            public int hashCode() {
                return 31 * (31 * ForField.this.hashCode() + instrumentedType.hashCode()) + fieldDescription.hashCode();
            }
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof InvocationHandlerAdapter.ForField)) return false;
            final InvocationHandlerAdapter.ForField other = (InvocationHandlerAdapter.ForField) o;
            if (!other.canEqual((java.lang.Object) this)) return false;
            if (!super.equals(o)) return false;
            final java.lang.Object this$fieldLocatorFactory = this.fieldLocatorFactory;
            final java.lang.Object other$fieldLocatorFactory = other.fieldLocatorFactory;
            if (this$fieldLocatorFactory == null ? other$fieldLocatorFactory != null : !this$fieldLocatorFactory.equals(other$fieldLocatorFactory)) return false;
            return true;
        }

        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        protected boolean canEqual(final java.lang.Object other) {
            return other instanceof InvocationHandlerAdapter.ForField;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + super.hashCode();
            final java.lang.Object $fieldLocatorFactory = this.fieldLocatorFactory;
            result = result * PRIME + ($fieldLocatorFactory == null ? 43 : $fieldLocatorFactory.hashCode());
            return result;
        }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof InvocationHandlerAdapter)) return false;
        final InvocationHandlerAdapter other = (InvocationHandlerAdapter) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        final java.lang.Object this$fieldName = this.fieldName;
        final java.lang.Object other$fieldName = other.fieldName;
        if (this$fieldName == null ? other$fieldName != null : !this$fieldName.equals(other$fieldName)) return false;
        final java.lang.Object this$assigner = this.assigner;
        final java.lang.Object other$assigner = other.assigner;
        if (this$assigner == null ? other$assigner != null : !this$assigner.equals(other$assigner)) return false;
        if (this.cacheMethods != other.cacheMethods) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof InvocationHandlerAdapter;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $fieldName = this.fieldName;
        result = result * PRIME + ($fieldName == null ? 43 : $fieldName.hashCode());
        final java.lang.Object $assigner = this.assigner;
        result = result * PRIME + ($assigner == null ? 43 : $assigner.hashCode());
        result = result * PRIME + (this.cacheMethods ? 79 : 97);
        return result;
    }
}