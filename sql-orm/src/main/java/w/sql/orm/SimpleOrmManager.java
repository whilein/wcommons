/*
 *    Copyright 2021 Whilein
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package w.sql.orm;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import w.commons.flow.Flow;
import w.commons.flow.FlowItems;
import w.commons.flow.IntFlow;
import w.commons.flow.IntFlowItems;
import w.commons.sql.Messenger;
import w.sql.orm.annotation.Bind;
import w.sql.orm.annotation.Column;
import w.sql.orm.definition.BindDefinition;
import w.sql.orm.definition.DaoDefinition;
import w.sql.orm.definition.EntityColumnDefinition;
import w.sql.orm.definition.EntityDefinition;
import w.sql.orm.definition.QueryDefinition;
import w.sql.orm.definition.ResultColumnDefinition;
import w.sql.orm.definition.ResultDefinition;
import w.sql.orm.definition.UpdateDefinition;
import w.sql.orm.type.TypeRegistry;
import w.sql.orm.type.TypeTransformer;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleOrmManager implements OrmManager {

    @Getter
    Messenger messenger;

    @Getter
    String database;

    public static @NotNull OrmManager create(final @NotNull Messenger messenger) {
        return new SimpleOrmManager(messenger, null);
    }

    @Override
    public @NotNull OrmManager withMessenger(final @NotNull Messenger messenger) {
        return new SimpleOrmManager(messenger, database);
    }

    @Override
    public @NotNull OrmManager withDatabase(final @Nullable String database) {
        return new SimpleOrmManager(messenger, database);
    }

    private Class<?> processGenericType(final Type type, final Map<String, Type> parameterMap) {
        if (type instanceof ParameterizedType) {
            val parameterized = (ParameterizedType) type;

            val raw = (Class<?>) parameterized.getRawType();

            final TypeVariable<?>[] typeParameters = raw.getTypeParameters();
            final Type[] typeArguments = parameterized.getActualTypeArguments();

            for (int i = 0; i < typeArguments.length; i++) {
                parameterMap.put(typeParameters[i].getName(), typeArguments[i]);
            }

            return raw;
        } else if (type instanceof WildcardType) {
            val wildcard = (WildcardType) type;

            return processGenericType(wildcard.getUpperBounds()[0], parameterMap);
        } else {
            val cls = (Class<?>) type;

            final TypeVariable<?>[] typeParameters = cls.getTypeParameters();

            if (typeParameters.length > 0) {
                throw new IllegalStateException("Cannot use raw parameterized type of " + cls.getName());
            }

            return cls;
        }
    }

    private void processDefinitions(
            final Type type,
            final List<QueryDefinition> out
    ) {
        final Map<String, Type> parameters = new HashMap<>();
        final Class<?> raw = processGenericType(type, parameters);

        for (val method : raw.getDeclaredMethods()) {
            if (method.isDefault()) {
                continue;
            }

            val returnType = getReturnType(method, parameters);
            val parameterTypes = getParameters(method, parameters);

            // val query = QueryParser.parse()

            // out.add(new QueryDefinitionImpl(
            //         "",
            //         method,
            //         returnType,
            //         parameterTypes
            // ));
        }

        for (val interfaceType : raw.getGenericInterfaces()) {
            processDefinitions(interfaceType, out);
        }
    }

    private Type shallowNormalize(final Type type, final Map<String, Type> parameters) {
        if (type instanceof Class<?>) {
            return type;
        } else if (type instanceof WildcardType) {
            val wildcard = (WildcardType) type;

            return shallowNormalize(wildcard.getUpperBounds()[0], parameters);
        } else if (type instanceof ParameterizedType) {
            val parameterized = (ParameterizedType) type;

            return shallowNormalize(parameterized.getRawType(), parameters);
        } else if (type instanceof TypeVariable<?>) {
            val typeVariable = (TypeVariable<?>) type;
            return parameters.get(typeVariable.getName());
        }

        throw new IllegalStateException("Unexpected type: " + type.getClass().getName()
                + " (" + type.getTypeName() + ")");
    }

    private Class<?> normalize(final Type type, final Map<String, Type> parameters) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }

        return normalize(shallowNormalize(type, parameters), parameters);
    }

    private BindDefinition[] getParameters(
            final Method method,
            final Map<String, Type> parameterMap
    ) {
        val parameters = method.getParameters();

        val definitions = new BindDefinition[parameters.length];

        for (int i = 0; i < definitions.length; i++) {
            val parameter = parameters[i];

            val type = normalize(parameter.getParameterizedType(), parameterMap);

            if (!TypeRegistry.isCommonType(type)) {
                throw new IllegalStateException("Parameter " + parameter.getName() + " of type "
                        + type.getName() + " cannot be an parameter");
            }

            definitions[i] = new BindDefinitionImpl(
                    type,
                    Optional.ofNullable(parameter.getAnnotation(Bind.class))
                            .map(Bind::value)
                            .orElseGet(parameter::getName),
                    TypeRegistry.lookupTransformer(type)
            );
        }

        return definitions;
    }


    private EntityDefinition getEntityDefinition(final Type type) {
        val parameters = new HashMap<String, Type>();
        val raw = processGenericType(type, parameters);

        if (!raw.isInterface()) {
            throw new IllegalStateException(raw.getName() + " should be an interface!");
        }

        val columns = new ArrayList<EntityColumnDefinition>();

        findEntityColumns(raw, parameters, columns);

        return new EntityDefinitionImpl(raw, columns.toArray(new EntityColumnDefinition[0]));
    }

    private void findEntityColumns(
            final Class<?> type,
            final Map<String, Type> parameterMap,
            final List<EntityColumnDefinition> out
    ) {
        for (val method : type.getDeclaredMethods()) {
            if (method.isDefault()) {
                continue;
            }

            if (method.getParameterCount() > 0) {
                throw new IllegalStateException("Method " + getMethodName(method) + " has parameters and " +
                        "cannot be an getter for column");
            }

            val returnType = normalize(method.getGenericReturnType(), parameterMap);

            if (!TypeRegistry.isCommonType(returnType)) {
                throw new IllegalStateException("Return type " + returnType.getName() + " of method "
                        + getMethodName(method) + " is unknown");
            }

            if (!method.isAnnotationPresent(Column.class)) {
                throw new IllegalStateException("Method " + getMethodName(method) + " should be annotated with @Column");
            }

            val column = method.getDeclaredAnnotation(Column.class);

            out.add(new EntityColumnDefinitionImpl(
                    returnType,
                    column.value(),
                    method,
                    TypeRegistry.lookupTransformer(returnType)
            ));
        }
    }

    private ResultDefinition getReturnType(
            final Method method,
            final Map<String, Type> parameters
    ) {
        val type = method.getGenericReturnType();

        Class<?> commonType = null;

        if (type instanceof Class<?>) {
            if (type == IntFlow.class || type == IntFlowItems.class) {
                commonType = int.class;
            }
        } else if (type instanceof ParameterizedType) {
            val parameterized = (ParameterizedType) type;
            val raw = parameterized.getRawType();

            if (raw == Flow.class || raw == FlowItems.class) {
                val genericReturnType = parameterized.getActualTypeArguments()[0];

                final Class<?> returnType = normalize(genericReturnType, parameters);

                if (TypeRegistry.isCommonType(returnType)) {
                    commonType = returnType;
                } else {
                    return getEntityDefinition(shallowNormalize(genericReturnType, parameters));
                }
            }
        }

        if (commonType == null) {
            throw new IllegalStateException("Illegal return type of "
                    + getMethodName(method) + ": " + method.getGenericReturnType());
        }

        //if (method.isAnnotationPresent(Update.class)) {
        //    if (commonType != int.class) {
        //        throw new IllegalStateException("Update " + getMethodName(method) + " should returns an int, but "
        //                + commonType.getName());
        //    }

        //    val update = method.getAnnotation(Update.class);

        //    return new UpdateDefinitionImpl(update.generatedKey());
        //}

        return new ResultColumnDefinitionImpl(
                commonType,
                Optional.ofNullable(method.getAnnotation(Column.class))
                        .map(Column::value)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Result of " + getMethodName(method)
                                        + " should be annotated with @Column")
                        ),
                TypeRegistry.lookupTransformer(commonType)
        );
    }

    private String getMethodName(final Method method) {
        return method.getDeclaringClass() + "#" + method.getName();
    }

    @Override
    public <D> D makeDao(final @NotNull Class<@NotNull D> daoType) {
        if (!daoType.isInterface()) {
            throw new IllegalArgumentException("daoType must be an interface!");
        }

        if (daoType.getTypeParameters().length > 0) {
            throw new IllegalArgumentException("daoType should not have any type parameter");
        }

        val queries = new ArrayList<QueryDefinition>();
        processDefinitions(daoType, queries);

        System.out.println(queries);
        val definition = new DaoDefinitionImpl<>(daoType, queries.toArray(new QueryDefinition[0]));
        val compiler = SimpleOrmCompiler.create(this, definition);

        return compiler.compile();
    }

    @Getter
    @ToString
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class DaoDefinitionImpl<T> implements DaoDefinition<T> {
        Class<T> type;
        QueryDefinition[] queries;
    }

    @Getter
    @ToString
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class QueryDefinitionImpl implements QueryDefinition {
        String query;
        Method method;

        ResultDefinition result;
        BindDefinition[] bindings;

        List<String> columns;

    }

    @Getter
    @ToString
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class BindDefinitionImpl implements BindDefinition {
        Class<?> type;
        String name;

        TypeTransformer typeTransformer;
    }

    @Getter
    @ToString
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class UpdateDefinitionImpl implements UpdateDefinition {
        boolean generatedKey;

        @Override
        public @NotNull Class<?> getType() {
            return void.class;
        }
    }

    @Getter
    @ToString
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class EntityColumnDefinitionImpl implements EntityColumnDefinition {
        Class<?> type;
        String name;

        Method method;

        TypeTransformer typeTransformer;
    }

    @Getter
    @ToString
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ResultColumnDefinitionImpl implements ResultColumnDefinition {
        Class<?> type;
        String name;

        TypeTransformer typeTransformer;
    }

    @Getter
    @ToString
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class EntityDefinitionImpl implements EntityDefinition {
        Class<?> type;
        EntityColumnDefinition[] columns;
    }
}