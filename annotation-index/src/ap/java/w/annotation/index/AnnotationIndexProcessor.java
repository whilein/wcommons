package w.annotation.index;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes("w.annotation.index.Indexed")
public final class AnnotationIndexProcessor extends AbstractProcessor {

    private Set<TypeElement> annotations;

    private Map<String, Set<String>> annotated;
    private Messager messager;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.annotations = new HashSet<>();
        this.annotated = new HashMap<>();

        this.messager = processingEnv.getMessager();
    }

    private TypeElement getTypeElement(final String name) {
        return processingEnv.getElementUtils().getTypeElement(name);
    }

    private String getClassName(final TypeElement type) {
        try {
            return toClassName(type);
        } catch (final MirroredTypeException e) {
            return toClassName(getTypeElement(e.getTypeMirror().toString()));
        }
    }

    private String toClassName(final TypeElement type) {
        final Element enclosedIn = type.getEnclosingElement();

        // if class is inner
        if (enclosedIn instanceof TypeElement) {
            return toClassName((TypeElement) enclosedIn) + '$' + type.getSimpleName();
        }

        return type.getQualifiedName().toString();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        for (final Element annotationType : roundEnv.getElementsAnnotatedWith(Indexed.class)) {
            if (annotationType.getKind() != ElementKind.ANNOTATION_TYPE) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "@Indexed annotation may be applied only to ANNOTATION_TYPE, but "
                                + annotationType.getKind());
                break;
            }

            this.annotations.add((TypeElement) annotationType);
        }

        for (final TypeElement indexedAnnotation : this.annotations) {
            final Set<String> set = annotated.computeIfAbsent(getClassName(indexedAnnotation), __ -> new HashSet<>());

            for (final Element annotatedElement : roundEnv.getElementsAnnotatedWith(indexedAnnotation)) {
                if (!(annotatedElement instanceof TypeElement)) {
                    continue;
                }

                set.add(getClassName((TypeElement) annotatedElement));
            }
        }

        if (roundEnv.processingOver()) {
            for (final Map.Entry<String, Set<String>> entry : annotated.entrySet()) {
                final String annotation = entry.getKey();
                final Set<String> annotated = entry.getValue();

                try {
                    final FileObject object = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                            "", "META-INF/services/" + annotation);

                    try (final Writer writer = object.openWriter()) {
                        for (final String annotatedName : annotated) {
                            writer.append(annotatedName);
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
