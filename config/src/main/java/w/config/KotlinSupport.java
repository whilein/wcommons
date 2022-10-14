package w.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * @author whilein
 */
@UtilityClass
public class KotlinSupport {

    private final Support SUPPORT;

    static {
        Support support;

        try {
            Class.forName("com.fasterxml.jackson.module.kotlin.KotlinModule");

            support = new Support();
        } catch (final ClassNotFoundException e) {
            support = null;
        }

        SUPPORT = support;
    }

    public void tryEnable(final ObjectMapper objectMapper) {
        if (SUPPORT != null) {
            SUPPORT.enable(objectMapper);
        }
    }

    public boolean isAvailable() {
        return SUPPORT != null;
    }

    private static final class Support {

        public void enable(final @NotNull ObjectMapper objectMapper) {
            objectMapper.registerModule(new KotlinModule.Builder()
                    .build());
        }

    }
}
