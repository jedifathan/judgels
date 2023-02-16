package judgels.michael;

import dagger.Component;
import javax.inject.Singleton;
import judgels.fs.aws.AwsModule;
import judgels.jophiel.hibernate.JophielHibernateDaoModule;
import judgels.jophiel.user.avatar.UserAvatarModule;
import judgels.michael.index.IndexResource;
import judgels.michael.problem.base.ProblemResource;
import judgels.service.JudgelsApplicationModule;
import judgels.service.JudgelsPersistenceModule;
import judgels.service.JudgelsScheduler;
import judgels.service.hibernate.JudgelsHibernateModule;

@Component(modules = {
        JudgelsApplicationModule.class,
        JudgelsPersistenceModule.class,
        JudgelsHibernateModule.class,
        MichaelModule.class,

        // Jophiel
        JophielHibernateDaoModule.class,
        AwsModule.class,
        UserAvatarModule.class})
@Singleton
public interface MichaelComponent {
    PingResource pingResource();
    IndexResource indexResource();
    ProblemResource problemResource();

    JudgelsScheduler scheduler();
}
