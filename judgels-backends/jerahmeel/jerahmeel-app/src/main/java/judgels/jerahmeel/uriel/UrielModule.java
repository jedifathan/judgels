package judgels.jerahmeel.uriel;

import com.palantir.conjure.java.api.config.service.UserAgent;
import dagger.Module;
import dagger.Provides;
import java.util.Optional;
import judgels.service.jaxrs.JaxRsClients;
import judgels.uriel.api.UrielClientConfiguration;
import judgels.uriel.api.contest.ContestService;

@Module
public class UrielModule {
    private final Optional<UrielClientConfiguration> config;

    public UrielModule(Optional<UrielClientConfiguration> config) {
        this.config = config;
    }

    @Provides
    Optional<ContestService> contestService() {
        UserAgent userAgent = UserAgent.of(UserAgent.Agent.of("jerahmeel", UserAgent.Agent.DEFAULT_VERSION));
        return config.map(cfg -> JaxRsClients.create(ContestService.class, cfg.getBaseUrl(), userAgent));
    }
}
