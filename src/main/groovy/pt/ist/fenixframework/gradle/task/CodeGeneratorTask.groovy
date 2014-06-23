package pt.ist.fenixframework.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import pt.ist.fenixframework.DmlCompiler
import pt.ist.fenixframework.core.DmlFile
import pt.ist.fenixframework.core.Project
import pt.ist.fenixframework.dml.CompilerArgs

import java.security.MessageDigest

/**
 * Code Generator Task, responsible for generating Base classes and setting up the Framework Project.
 *
 * @author João Carvalho (joao.pedro.carvalho@tecnico.ulisboa.pt)
 */
class CodeGeneratorTask extends DefaultTask {

    File generatedSourcesDir

    String codeGenerator
    Configuration configuration
    File dmlSources
    File externalDmlSources
    SourceSet sourceSet

    @TaskAction
    def apply() {
        def mainSourcesDir = sourceSet.java.srcDirs.iterator().next()

        // Special class-loader is required to contain all the dependencies
        ClassLoader loader = new URLClassLoader(configuration.collect {
            it.toURI().toURL()
        } as URL[], Thread.currentThread().getContextClassLoader())

        def projects = new ArrayList<>()
        configuration.allDependencies.each { Dependency dep ->
            // Filter dependencies with a project.properties
            if (loader.getResourceAsStream("${dep.name}/project.properties") != null) {
                projects.add(Project.fromName(dep.name, loader))
            }
        }

        def dmls = project.fileTree(dir: dmlSources, includes: ['**/*.dml']).files.collect { File file ->
            new DmlFile(file.toURI().toURL(), file.getName())
        }

        if (externalDmlSources != null) {
            project.fileTree(dir: externalDmlSources, includes: ['**/*.dml']).files.collect { File file ->
                dmls.add(new DmlFile(file.toURI().toURL(), file.getName()))
            }
        }

        sourceSet.resources.srcDir(dmlSources)

        Project ffProject = new Project(project.name, project.version, dmls, projects, true);

        ffProject.generateProjectProperties(sourceSet.output.classesDir.absolutePath)

        if (shouldCompile(ffProject)) {
            def localDmls = dmls.collect { DmlFile dml -> dml.getUrl() }

            def externalDmls = ffProject.fullDmlSortedList.collect { DmlFile dml -> dml.getUrl() }
            externalDmls.removeAll(localDmls)

            CompilerArgs compilerArgs = new CompilerArgs(project.name,
                    mainSourcesDir, generatedSourcesDir, "", false,
                    Class.forName(codeGenerator, true, loader), localDmls, externalDmls, project.ff.parameters)

            DmlCompiler.compile(compilerArgs)
        }
    }

    @Override
    public Task configure(Closure cls) {
        Task task = super.configure(cls)

        // Add the generated sources folder to the Java source dirs
        // This runs on the 'configure' method, so that IDE descriptors properly contain this source folder
        generatedSourcesDir = project.file("${project.buildDir}/generated-sources/${sourceSet.name}/ff-gradle-plugin/")
        sourceSet.java.srcDir(generatedSourcesDir)
        return task
    }

    /**
     * Determines whether the given {@link Project} should be compiled, comparing the
     * checksum of its Dml files with a cached checksum file.
     */
    private boolean shouldCompile(Project proj) {
        def digest = MessageDigest.getInstance("MD5")
        proj.fullDmlSortedList.each {
            file ->
                file.url.openStream().eachByte {
                    digest.update(it)
                }
        }
        def checksum = new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0')

        def checksumFile = new File("${project.buildDir}/${sourceSet.name}/ff-gradle-plugin.checksum")

        if (checksumFile.exists() && checksumFile.text.equals(checksum)) {
            return false
        } else {
            checksumFile.parentFile.mkdirs()
            checksumFile.createNewFile()
            checksumFile.text = checksum
            return true
        }
    }

}
