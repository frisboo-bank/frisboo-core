import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class MessagingConventionExtension @Inject constructor(private val objects: ObjectFactory) {
    public val useKafka: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    public val useTestcontainers: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
}
