package pt.ist.fenixframework.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import pt.ist.esw.advice.ProcessAnnotations
import pt.ist.esw.advice.ProcessAnnotations.ProgramArgs
import pt.ist.fenixframework.Atomic
import pt.ist.fenixframework.atomic.AtomicContextFactory

/**
 * Atomic Task, to post-process @Atomic annotations
 *
 * @author João Carvalho (joao.pedro.carvalho@tecnico.ulisboa.pt)
 */
class AtomicTask extends DefaultTask {

    def inputFiles

    @TaskAction
    def atomicAction() {
        ProgramArgs args = new ProgramArgs(Atomic.class, AtomicContextFactory.class, new ArrayList<File>(inputFiles.files))
        new ProcessAnnotations(args) {
            @Override
            void processClassFile(File classFile) {
                if (!classFile.name.contains("_Base")) {
                    AtomicTask.this.logger.debug("Processing {}", classFile)
                    super.processClassFile(classFile)
                }
            }
        }.process();
    }

}
