import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class PersistenceConventionExtension @Inject constructor(private val objects: ObjectFactory) {
    public val useMigration: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    public val useMongo: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    public val usePostgres: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    public val useTestcontainers: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
}
