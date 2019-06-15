package judgels.uriel.contest.scoreboard.ioi;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import judgels.gabriel.api.LanguageRestriction;
import judgels.gabriel.api.Verdict;
import judgels.sandalphon.api.submission.programming.Submission;
import judgels.uriel.api.contest.Contest;
import judgels.uriel.api.contest.ContestStyle;
import judgels.uriel.api.contest.contestant.ContestContestant;
import judgels.uriel.api.contest.module.IoiStyleModuleConfig;
import judgels.uriel.api.contest.module.StyleModuleConfig;
import judgels.uriel.api.contest.scoreboard.IoiScoreboard;
import judgels.uriel.api.contest.scoreboard.IoiScoreboard.IoiScoreboardContent;
import judgels.uriel.api.contest.scoreboard.IoiScoreboard.IoiScoreboardEntry;
import judgels.uriel.api.contest.scoreboard.ScoreboardState;
import judgels.uriel.contest.scoreboard.AbstractProgrammingScoreboardProcessorTests;
import judgels.uriel.contest.scoreboard.ScoreboardProcessResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class IoiScoreboardProcessorTests extends AbstractProgrammingScoreboardProcessorTests {
    private IoiScoreboardProcessor scoreboardProcessor = new IoiScoreboardProcessor();

    @Nested
    class Process {
        private ScoreboardState state = new ScoreboardState.Builder()
                .addProblemJids("p1", "p2")
                .addProblemAliases("A", "B")
                .build();

        private Contest contest = new Contest.Builder()
                .beginTime(Instant.ofEpochMilli(5))
                .duration(Duration.ofMillis(100))
                .id(1)
                .jid("JIDC")
                .name("contest-name")
                .slug("contest-slug")
                .style(ContestStyle.IOI)
                .build();

        private StyleModuleConfig styleModuleConfig = new IoiStyleModuleConfig.Builder().build();

        private Set<ContestContestant> contestants = ImmutableSet.of(
                new ContestContestant.Builder().userJid("c1").build(),
                new ContestContestant.Builder().userJid("c2").contestStartTime(Instant.ofEpochMilli(10)).build());

        @Test
        void time_calculation() {
            List<Submission> submissions = ImmutableList.of(
                    createMilliSubmission(1, 20, "c2", "p1", 78, Verdict.TIME_LIMIT_EXCEEDED),
                    createMilliSubmission(2, 20, "c1", "p2", 50, Verdict.OK),
                    createMilliSubmission(3, 25, "c1", "p1", 0, Verdict.WRONG_ANSWER));

            ScoreboardProcessResult result = scoreboardProcessor.process(
                    contest,
                    state,
                    Optional.empty(),
                    styleModuleConfig,
                    contestants,
                    submissions,
                    ImmutableList.of(),
                    Optional.empty());

            assertThat(Lists.transform(result.getEntries(), e -> (IoiScoreboardEntry) e)).containsExactly(
                    new IoiScoreboardEntry.Builder()
                            .rank(1)
                            .contestantJid("c2")
                            .addScores(
                                    Optional.of(78),
                                    Optional.empty()
                            )
                            .totalScores(78)
                            .lastAffectingPenalty(10)
                            .build(),
                    new IoiScoreboardEntry.Builder()
                            .rank(2)
                            .contestantJid("c1")
                            .addScores(
                                    Optional.of(0),
                                    Optional.of(50)
                            )
                            .totalScores(50)
                            .lastAffectingPenalty(15)
                            .build());
        }

        @Nested
        class ProblemOrdering {
            private List<Submission> submissions = ImmutableList.of(
                    createMilliSubmission(1, 20, "c2", "p1", 50, Verdict.TIME_LIMIT_EXCEEDED),
                    createMilliSubmission(2, 20, "c1", "p2", 50, Verdict.OK));

            @Test
            void base_case() {
                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.empty(),
                        styleModuleConfig,
                        contestants,
                        submissions,
                        ImmutableList.of(),
                        Optional.empty());

                assertThat(Lists.transform(result.getEntries(), e -> (IoiScoreboardEntry) e)).containsExactly(
                        new IoiScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c2")
                                .addScores(
                                        Optional.of(50),
                                        Optional.empty()
                                )
                                .totalScores(50)
                                .lastAffectingPenalty(10)
                                .build(),
                        new IoiScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c1")
                                .addScores(
                                        Optional.empty(),
                                        Optional.of(50)
                                )
                                .totalScores(50)
                                .lastAffectingPenalty(15)
                                .build());
            }

            @Test
            void reversed_case() {
                state = new ScoreboardState.Builder()
                        .addProblemJids("p2", "p1")
                        .addProblemAliases("B", "A")
                        .build();

                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.empty(),
                        styleModuleConfig,
                        contestants,
                        submissions,
                        ImmutableList.of(),
                        Optional.empty());

                assertThat(Lists.transform(result.getEntries(), e -> (IoiScoreboardEntry) e)).containsExactly(
                        new IoiScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c2")
                                .addScores(
                                        Optional.empty(),
                                        Optional.of(50)
                                )
                                .totalScores(50)
                                .lastAffectingPenalty(10)
                                .build(),
                        new IoiScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c1")
                                .addScores(
                                        Optional.of(50),
                                        Optional.empty()
                                )
                                .totalScores(50)
                                .lastAffectingPenalty(15)
                                .build());
            }
        }

        @Nested
        class LastAffectingPenalty {
            private List<Submission> submissions = ImmutableList.of(
                    createMilliSubmission(1, 20, "c2", "p1", 50, Verdict.TIME_LIMIT_EXCEEDED),
                    createMilliSubmission(2, 20, "c1", "p2", 50, Verdict.OK));

            @Test
            void sorted_without_last_affecting_penalty() {
                styleModuleConfig = new IoiStyleModuleConfig.Builder().usingLastAffectingPenalty(false).build();

                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.empty(),
                        styleModuleConfig,
                        contestants,
                        submissions,
                        ImmutableList.of(),
                        Optional.empty());

                assertThat(Lists.transform(result.getEntries(), e -> (IoiScoreboardEntry) e)).containsExactly(
                        new IoiScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c2")
                                .addScores(
                                        Optional.of(50),
                                        Optional.empty()
                                )
                                .totalScores(50)
                                .lastAffectingPenalty(10)
                                .build(),
                        new IoiScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c1")
                                .addScores(
                                        Optional.empty(),
                                        Optional.of(50)
                                )
                                .totalScores(50)
                                .lastAffectingPenalty(15)
                                .build());
            }

            @Test
            void sorted_with_last_affecting_penalty() {
                styleModuleConfig = new IoiStyleModuleConfig.Builder().usingLastAffectingPenalty(true).build();

                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.empty(),
                        styleModuleConfig,
                        contestants,
                        submissions,
                        ImmutableList.of(),
                        Optional.empty());

                assertThat(Lists.transform(result.getEntries(), e -> (IoiScoreboardEntry) e)).containsExactly(
                        new IoiScoreboardEntry.Builder()
                                .rank(1)
                                .contestantJid("c2")
                                .addScores(
                                        Optional.of(50),
                                        Optional.empty()
                                )
                                .totalScores(50)
                                .lastAffectingPenalty(10)
                                .build(),
                        new IoiScoreboardEntry.Builder()
                                .rank(2)
                                .contestantJid("c1")
                                .addScores(
                                        Optional.empty(),
                                        Optional.of(50)
                                )
                                .totalScores(50)
                                .lastAffectingPenalty(15)
                                .build());
            }
        }

        @Nested
        class IncrementalProcess {
            List<Submission> submissions = ImmutableList.of(
                    createMilliSubmission(5, 100, "c1", "p1", 0, Verdict.WRONG_ANSWER),
                    createMilliSubmission(6, 200, "c2", "p1", 0, Verdict.WRONG_ANSWER),
                    createMilliSubmission(7, 300, "c1", "p1", 20, Verdict.WRONG_ANSWER),
                    createMilliSubmission(8, 400, "c1", "p1", 100, Verdict.ACCEPTED),
                    createMilliSubmission(9, 500, "c2", "p2", 95, Verdict.WRONG_ANSWER),
                    createMilliSubmission(10, 600, "c2", "p1", 0, Verdict.PENDING),
                    createMilliSubmission(11, 700, "c1", "p2", 80, Verdict.OK));

            Set<ContestContestant> contestants = ImmutableSet.of(
                    new ContestContestant.Builder().userJid("c1").build(),
                    new ContestContestant.Builder().userJid("c2").contestStartTime(Instant.ofEpochMilli(300)).build(),
                    new ContestContestant.Builder().userJid("c3").build());

            IoiScoreboardIncrementalContent incrementalContent = new IoiScoreboardIncrementalContent.Builder()
                    .lastSubmissionId(3)
                    .putLastAffectingPenaltiesByContestantJid("c2", 75L)
                    .putLastAffectingPenaltiesByContestantJid("c3", 90L)
                    .putScoresMapsByContestantJid("c2", ImmutableMap.of("p1", empty(), "p2", of(90)))
                    .putScoresMapsByContestantJid("c3", ImmutableMap.of("p1", of(20), "p2", of(30)))
                    .build();

            @Test
            void empty_initial_incremental_content() {
                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.empty(),
                        styleModuleConfig,
                        contestants,
                        submissions,
                        ImmutableList.of(),
                        Optional.empty());

                assertThat(result.getIncrementalContent()).isEqualTo(new IoiScoreboardIncrementalContent.Builder()
                        .lastSubmissionId(9)
                        .putLastAffectingPenaltiesByContestantJid("c1", 395L)
                        .putLastAffectingPenaltiesByContestantJid("c2", 200L)
                        .putScoresMapsByContestantJid("c1", ImmutableMap.of("p1", of(100), "p2", empty()))
                        .putScoresMapsByContestantJid("c2", ImmutableMap.of("p1", of(0), "p2", of(95)))
                        .build());
            }

            @Test
            void empty_new_submissions() {
                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.of(incrementalContent),
                        styleModuleConfig,
                        contestants,
                        ImmutableList.of(),
                        ImmutableList.of(),
                        Optional.empty());

                assertThat(result.getIncrementalContent()).isEqualTo(new IoiScoreboardIncrementalContent.Builder()
                        .from(incrementalContent)
                        .lastSubmissionId(Optional.empty())
                        .build());
            }

            @Test
            void existing_incremental_content() {
                ScoreboardProcessResult result = scoreboardProcessor.process(
                        contest,
                        state,
                        Optional.of(incrementalContent),
                        styleModuleConfig,
                        contestants,
                        submissions,
                        ImmutableList.of(),
                        Optional.empty());

                assertThat(result.getIncrementalContent()).isEqualTo(new IoiScoreboardIncrementalContent.Builder()
                        .lastSubmissionId(9)
                        .putLastAffectingPenaltiesByContestantJid("c1", 395L)
                        .putLastAffectingPenaltiesByContestantJid("c2", 200L)
                        .putLastAffectingPenaltiesByContestantJid("c3", 90L)
                        .putScoresMapsByContestantJid("c1", ImmutableMap.of("p1", of(100), "p2", empty()))
                        .putScoresMapsByContestantJid("c2", ImmutableMap.of("p1", of(0), "p2", of(95)))
                        .putScoresMapsByContestantJid("c3", ImmutableMap.of("p1", of(20), "p2", of(30)))
                        .build());
            }
        }
    }

    @Test
    void filter_problem_jids() {
        IoiScoreboardEntry entry = new IoiScoreboardEntry.Builder()
                .rank(0)
                .contestantJid("123")
                .totalScores(100)
                .lastAffectingPenalty(12)
                .build();

        IoiScoreboard scoreboard = new IoiScoreboard.Builder()
                .state(new ScoreboardState.Builder()
                        .addProblemJids("p1", "p2", "p3", "p4")
                        .addProblemAliases("A", "B", "C", "D")
                        .build())
                .content(new IoiScoreboardContent.Builder()
                        .addEntries(
                                new IoiScoreboardEntry.Builder()
                                        .from(entry)
                                        .rank(1)
                                        .contestantJid("c1")
                                        .addScores(of(10), of(20), of(30), of(40))
                                        .totalScores(100)
                                        .build(),
                                new IoiScoreboardEntry.Builder()
                                        .from(entry)
                                        .rank(2)
                                        .contestantJid("c2")
                                        .addScores(of(10), of(10), of(30), of(20))
                                        .totalScores(70)
                                        .build(),
                                new IoiScoreboardEntry.Builder()
                                        .from(entry)
                                        .rank(2)
                                        .contestantJid("c3")
                                        .addScores(of(10), of(20), of(20), of(20))
                                        .totalScores(70)
                                        .build())
                        .build())
                .build();

        IoiScoreboard filteredScoreboard = new IoiScoreboard.Builder()
                .state(new ScoreboardState.Builder()
                        .addProblemJids("p1", "p3")
                        .addProblemAliases("A", "C")
                        .build())
                .content(new IoiScoreboardContent.Builder()
                        .addEntries(
                                new IoiScoreboardEntry.Builder()
                                        .from(entry)
                                        .rank(1)
                                        .contestantJid("c1")
                                        .addScores(of(10), of(30))
                                        .totalScores(40)
                                        .build(),
                                new IoiScoreboardEntry.Builder()
                                        .from(entry)
                                        .rank(1)
                                        .contestantJid("c2")
                                        .addScores(of(10), of(30))
                                        .totalScores(40)
                                        .build(),
                                new IoiScoreboardEntry.Builder()
                                        .from(entry)
                                        .rank(3)
                                        .contestantJid("c3")
                                        .addScores(of(10), of(20))
                                        .totalScores(30)
                                        .build())
                        .build())
                .build();

        IoiStyleModuleConfig config = new IoiStyleModuleConfig.Builder()
                .gradingLanguageRestriction(LanguageRestriction.noRestriction())
                .build();
        assertThat(scoreboardProcessor.filterProblemJids(scoreboard, ImmutableSet.of("p1", "p3"), config))
                .isEqualTo(filteredScoreboard);
    }
}
