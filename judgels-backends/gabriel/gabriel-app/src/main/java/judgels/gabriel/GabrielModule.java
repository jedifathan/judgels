package judgels.gabriel;

import dagger.Module;
import dagger.Provides;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class GabrielModule {
    private final GabrielConfiguration gabrielConfig;

    public GabrielModule(GabrielConfiguration config) {
        this.gabrielConfig = config;
    }

    @Provides
    GabrielConfiguration config() {
        return gabrielConfig;
    }

    @Provides
    @Named("workersDir")
    Path gradingWorkersDir() {
        return Paths.get(gabrielConfig.getBaseDataDir(), "workers");
    }

    @Provides
    @Named("problemsDir")
    Path gradingProblemsDir() {
        return Paths.get(gabrielConfig.getBaseDataDir(), "problems");
    }

    @Provides
    @Singleton
    Clock clock() {
        return Clock.systemUTC();
    }
}
