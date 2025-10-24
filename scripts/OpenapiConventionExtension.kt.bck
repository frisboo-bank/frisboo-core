import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class OpenapiConventionExtension @Inject constructor(objects: ObjectFactory) {
    // --- Paths and Naming ---
    public val outputDir: DirectoryProperty = objects.directoryProperty()
    public val packageName: Property<String> = objects.property(String::class.java)
    public val schemaDir: DirectoryProperty = objects.directoryProperty()
    public val schemaFilename: Property<String> = objects.property(String::class.java)

    // --- Build Information ---
    public val groupId: Property<String> = objects.property(String::class.java)
    public val artifactId: Property<String> = objects.property(String::class.java)
    public val artifactVersion: Property<String> = objects.property(String::class.java)

    // --- Generation Toggles ---
    public val generateApiDocumentation: Property<Boolean> = objects.property(Boolean::class.java)
    public val generateApis: Property<Boolean> = objects.property(Boolean::class.java)
    public val generateApiTests: Property<Boolean> = objects.property(Boolean::class.java)
    public val generateModels: Property<Boolean> = objects.property(Boolean::class.java)
    public val generateModelDocumentation: Property<Boolean> = objects.property(Boolean::class.java)
    public val generateModelTests: Property<Boolean> = objects.property(Boolean::class.java)

    // --- Validation ---
    public val validateSpec: Property<Boolean> = objects.property(Boolean::class.java)
    public val recommend: Property<Boolean> = objects.property(Boolean::class.java)
}
