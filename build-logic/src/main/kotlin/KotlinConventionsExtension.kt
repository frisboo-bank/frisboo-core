import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class KotlinConventionsExtension @Inject constructor(objects: ObjectFactory) {
    // Language and compilation flags
    public val warningsAsErrors: Property<Boolean> = objects.property(Boolean::class.java)
    public val progressive: Property<Boolean> = objects.property(Boolean::class.java)

    // Toolchain and target bytecode
    public val jdkToolchain: Property<Int> = objects.property(Int::class.java)
    public val jvmTarget: Property<String> = objects.property(String::class.java)

    // Dependency/BOM management
    public val addBoms: Property<Boolean> = objects.property(Boolean::class.java)
    public val useEnforcedPlatforms: Property<Boolean> = objects.property(Boolean::class.java)

    // Extra compiler opt-ins
    public val additionalOptIns: ListProperty<String> = objects.listProperty(String::class.java)
}
