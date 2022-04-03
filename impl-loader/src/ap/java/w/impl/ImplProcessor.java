package w.impl;

import lombok.val;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ImplProcessor extends AbstractProcessor {

    private final Map<String, List<ImplModel>> models = new HashMap<>();

    private static String getClassName(final TypeElement type) {
        val enclosedIn = type.getEnclosingElement();

        // if class is inner
        if (enclosedIn instanceof TypeElement) {
            return getClassName((TypeElement) enclosedIn) + '$' + type.getSimpleName();
        }

        return type.getQualifiedName().toString();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Impl.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private String getClassName(final Class<?> cls) {
        try {
            return cls.getName();
        } catch (final MirroredTypeException e) {
            return getClassName(e.getTypeMirror());
        }
    }

    private String getClassName(final TypeMirror type) {
        return getClassName(processingEnv.getElementUtils().getTypeElement(type.toString()));
    }

    private void addModel(
            final String apiName,
            final String implName,
            final String factory,
            final ImplPriority priority
    ) {
        models.computeIfAbsent(apiName, x -> new ArrayList<>())
                .add(new ImplModel(implName, factory, priority));
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        for (val element : roundEnv.getElementsAnnotatedWith(Impl.class)) {
            val type = (TypeElement) element;
            val name = getClassName(type);

            val annotation = element.getAnnotation(Impl.class);
            val priority = annotation.priority();

            try {
                for (val apiType : annotation.types()) {
                    addModel(getClassName(apiType), name, annotation.factory(), priority);
                }
            } catch (final MirroredTypeException e) {
                for (val apiType : e.getTypeMirrors()) {
                    addModel(getClassName(apiType), name, annotation.factory(), priority);
                }
            }
        }

        if (roundEnv.processingOver()) {
            for (val entry : models.entrySet()) {
                val apiName = entry.getKey();
                val modelList = entry.getValue();

                try {
                    val object = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                            "", "META-INF/impl/" + apiName);

                    try (val writer = object.openWriter()) {
                        for (val model : modelList) {
                            writer.append(model.getImplType());
                            writer.append(':');
                            writer.append(model.getFactoryMethod());
                            writer.append(':');
                            writer.append(model.getPriority().name());
                            writer.append('\n');
                        }
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

}
