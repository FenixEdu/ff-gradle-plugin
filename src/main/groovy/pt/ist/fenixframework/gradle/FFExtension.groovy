package pt.ist.fenixframework.gradle

/**
 * Fenix Framework Gradle Plugin configuration extension.
 *
 * @author João Carvalho (joao.pedro.carvalho@tecnico.ulisboa.pt)
 */
class FFExtension {

    // Name of the Code Generator to be used for the main compilation
    String codeGenerator = "pt.ist.fenixframework.dml.DefaultCodeGenerator"

    // Name of the Code Generator to be used for test class generation
    String testCodeGenerator = "pt.ist.fenixframework.dml.DefaultCodeGenerator"

    // Whether test DML should be compiled
    boolean enableTests = false

    // Parameters to be passed to the Code Generator
    Map<String, String> parameters = Collections.emptyMap()

}
