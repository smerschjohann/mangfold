import it.tty0.mangfold.MangfoldAgent
import spock.lang.Specification

class MangfoldAgentTest extends Specification {
    def "runs without problems"() {
        final MangfoldAgent agent = new MangfoldAgent(14237, Thread.currentThread().getContextClassLoader());
        agent.start();
        agent.blockUntilShutdown();
    }
}
