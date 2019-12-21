package judgels.jerahmeel;

import dagger.Component;
import javax.inject.Singleton;
import judgels.fs.aws.AwsModule;
import judgels.jerahmeel.admin.AdminResource;
import judgels.jerahmeel.chapter.lesson.ChapterLessonResource;
import judgels.jerahmeel.chapter.problem.ChapterProblemResource;
import judgels.jerahmeel.course.CourseResource;
import judgels.jerahmeel.course.chapter.CourseChapterResource;
import judgels.jerahmeel.gabriel.GabrielModule;
import judgels.jerahmeel.hibernate.JerahmeelHibernateDaoModule;
import judgels.jerahmeel.jophiel.JophielModule;
import judgels.jerahmeel.problemset.ProblemSetResource;
import judgels.jerahmeel.problemset.problem.ProblemSetProblemResource;
import judgels.jerahmeel.sandalphon.SandalphonModule;
import judgels.jerahmeel.sealtiel.SealtielModule;
import judgels.jerahmeel.submission.bundle.ItemSubmissionModule;
import judgels.jerahmeel.submission.bundle.ItemSubmissionResource;
import judgels.jerahmeel.submission.programming.SubmissionModule;
import judgels.jerahmeel.submission.programming.SubmissionResource;
import judgels.sandalphon.submission.programming.GradingResponsePoller;
import judgels.service.JudgelsApplicationModule;
import judgels.service.JudgelsModule;
import judgels.service.JudgelsPersistenceModule;
import judgels.service.JudgelsScheduler;
import judgels.service.hibernate.JudgelsHibernateModule;

@Component(modules = {
        AwsModule.class,
        SubmissionModule.class,
        ItemSubmissionModule.class,

        GabrielModule.class,
        JophielModule.class,
        SandalphonModule.class,
        SealtielModule.class,

        JudgelsModule.class,
        JudgelsApplicationModule.class,
        JudgelsHibernateModule.class,
        JudgelsPersistenceModule.class,

        JerahmeelModule.class,
        JerahmeelHibernateDaoModule.class
})
@Singleton
public interface JerahmeelComponent {
    AdminResource adminResource();
    CourseResource courseResource();
    CourseChapterResource courseChapterResource();
    ChapterLessonResource chapterLessonResource();
    ChapterProblemResource chapterProblemResource();
    ProblemSetResource problemSetResource();
    ProblemSetProblemResource problemSetProblemResource();
    ItemSubmissionResource itemSubmissionResource();
    SubmissionResource submissionResource();
    PingResource pingResource();

    JudgelsScheduler scheduler();
    GradingResponsePoller gradingResponsePoller();
}
