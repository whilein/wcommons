package w.impl;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// WARNING
// В данном сурс сете не работает ломбок
public final class ImplProcessor extends AbstractProcessor {

    private final Map<String, List<ImplModel>> models = new HashMap<>();

    private static String getClassName(final TypeElement type) {
        final Element enclosedIn = type.getEnclosingElement();

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

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        for (final Element element : roundEnv.getElementsAnnotatedWith(Impl.class)) {
            final TypeElement type = (TypeElement) element;
            final String name = getClassName(type);

            final Impl annotation = element.getAnnotation(Impl.class);
            final ImplPriority priority = annotation.priority();

            String apiName;

            try {
                apiName = annotation.type().getName();
            } catch (final MirroredTypeException e) {
                apiName = getClassName(processingEnv.getElementUtils().getTypeElement(e.getTypeMirror().toString()));
            }

            models.computeIfAbsent(apiName, x -> new ArrayList<>())
                    .add(new ImplModel(name, annotation.factory(), priority));
        }

        if (roundEnv.processingOver()) {
            for (Map.Entry<String, List<ImplModel>> entry : models.entrySet()) {
                final String apiName = entry.getKey();
                final List<ImplModel> modelList = entry.getValue();

                try {
                    final FileObject object = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                            "", "META-INF/impl/" + apiName);

                    try (final Writer writer = object.openWriter()) {
                        for (final ImplModel model : modelList) {
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
