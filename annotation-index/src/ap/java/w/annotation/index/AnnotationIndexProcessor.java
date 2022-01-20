package w.annotation.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.val;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes("w.annotation.index.Indexed")
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class AnnotationIndexProcessor extends AbstractProcessor {

    Set<TypeElement> annotations;

    Map<String, AnnotationIndexModel> models = new HashMap<>();

    Messager messager;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.annotations = new HashSet<>();
        this.models = new HashMap<>();

        this.messager = processingEnv.getMessager();
    }

    private TypeElement getTypeElement(final String name) {
        return processingEnv.getElementUtils().getTypeElement(name);
    }

    private String getClassName(final TypeMirror type) {
        val typeElement = getTypeElement(type.toString());

        if (typeElement == null) {
            return type.toString();
        }

        return toClassName(typeElement);
    }

    private String getClassName(final TypeElement type) {
        try {
            return toClassName(type);
        } catch (final MirroredTypeException e) {
            return getClassName(e.getTypeMirror());
        }
    }

    private String toClassName(final TypeElement type) {
        val enclosedIn = type.getEnclosingElement();

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

    private String getDescriptor(final TypeMirror type) {
        return type.toString();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        for (val annotationType : roundEnv.getElementsAnnotatedWith(Indexed.class)) {
            if (annotationType.getKind() != ElementKind.ANNOTATION_TYPE) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "@Indexed annotation may be applied only to ANNOTATION_TYPE, but "
                                + annotationType.getKind());
                break;
            }

            this.annotations.add((TypeElement) annotationType);
        }

        for (val indexedAnnotation : this.annotations) {
            val model = models.computeIfAbsent(getClassName(indexedAnnotation),
                    __ -> new AnnotationIndexModel(new HashSet<>(), new HashSet<>(), new HashSet<>()));

            for (val annotatedElement : roundEnv.getElementsAnnotatedWith(indexedAnnotation)) {
                switch (annotatedElement.getKind()) {
                    case METHOD: {
                        val method = (ExecutableElement) annotatedElement;

                        val name = method.getSimpleName().toString();
                        val declaredIn = method.getEnclosingElement();

                        model.getMethods().add(new AnnotationIndexModel.Method(
                                getClassName((TypeElement) declaredIn),
                                name,
                                getClassName(method.getReturnType()),
                                method.getParameters().stream()
                                        .map(VariableElement::asType)
                                        .map(this::getClassName)
                                        .toArray(String[]::new)
                        ));
                        break;
                    }
                    case FIELD: {
                        val field = (VariableElement) annotatedElement;

                        val name = field.getSimpleName().toString();
                        val declaredIn = field.getEnclosingElement();

                        model.getFields().add(new AnnotationIndexModel.Field(
                                getClassName((TypeElement) declaredIn),
                                name
                        ));
                        break;
                    }
                    case ENUM:
                    case CLASS:
                    case INTERFACE:
                        model.getTypes().add(getClassName((TypeElement) annotatedElement));
                }
            }
        }

        if (roundEnv.processingOver()) {
            val objectMapper = new ObjectMapper();

            for (val entry : models.entrySet()) {
                val annotation = entry.getKey();
                val model = entry.getValue();

                try {
                    val object = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                            "", "META-INF/services/" + annotation + ".json");

                    try (val writer = object.openWriter()) {
                        objectMapper.writeValue(writer, model);
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

}
