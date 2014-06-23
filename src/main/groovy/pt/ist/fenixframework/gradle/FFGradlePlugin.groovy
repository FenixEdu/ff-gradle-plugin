package pt.ist.fenixframework.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import pt.ist.fenixframework.gradle.task.AtomicTask
import pt.ist.fenixframework.gradle.task.CodeGeneratorTask

/**
 *
 * Gradle Plugin for the Fenix Framework
 *
 * This plugin registers tasks for the compilation steps required by the Fenix Framework.
 *
 * @author João Carvalho (joao.pedro.carvalho@tecnico.ulisboa.pt)
 */
class FFGradlePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create('ff', FFExtension)

        setupCodeGeneration(project)

        setupAtomic(project)
    }

    /*
     * Sets up the Code Generation tasks.
     */
    def setupCodeGeneration(Project project) {
        project.task('dmlGenerator', type: CodeGeneratorTask) {
            configuration = project.configurations.compile
            dmlSources = project.file('src/main/dml')
            sourceSet = project.sourceSets.main
            codeGenerator = project.ff.codeGenerator
        }
        // Code Generation occurs before Java Compilation
        project.tasks.compileJava.dependsOn('dmlGenerator')

        project.afterEvaluate {
            if (project.ff.enableTests) {
                project.task('testDmlGenerator', type: CodeGeneratorTask) {
                    configuration = project.configurations.testCompile
                    dmlSources = project.file('src/test/dml')
                    externalDmlSources = project.file('src/main/dml')
                    sourceSet = project.sourceSets.test
                    codeGenerator = project.ff.testCodeGenerator
                }
                project.tasks.compileTestJava.dependsOn('testDmlGenerator')
            }
        }

        // Exclude base classes from the final jar
        def jarTask = project.tasks.jar
        ["**/*_Base*",
         "**/pt/ist/fenixframework/ValueTypeSerializer*",
         "**/pt/ist/fenixframework/backend/CurrentBackEndId*"].each {
            jarTask.excludes.add(it)
        }
        jarTask.includeEmptyDirs = false
    }

    /*
     * Sets up the Atomic post-processing tasks.
     *
     * The tasks run after the Java Compilation, before the 'classes' tasks.
     */
    def setupAtomic(Project project) {
        project.task('atomic', type: AtomicTask, dependsOn: project.tasks.compileJava) {
            inputFiles = project.tasks.compileJava.outputs.files
        }
        project.tasks.classes.dependsOn('atomic')

        project.task('testAtomic', type: AtomicTask, dependsOn: project.tasks.compileTestJava) {
            inputFiles = project.tasks.compileTestJava.outputs.files
        }
        project.tasks.testClasses.dependsOn('testAtomic')

    }

}
