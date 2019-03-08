import * as React from 'react';
import { HTMLTable } from '@blueprintjs/core';
import { ItemSubmission } from 'modules/api/sandalphon/submissionBundle';
import { VerdictTag } from '../VerdictTag/VerdictTag';
import { FormattedRelative } from 'react-intl';
import { ContentCard } from 'components/ContentCard/ContentCard';

import './ProblemSubmissionCard.css';

export interface ProblemSubmissionCardProps {
  alias: string;
  submissions: ItemSubmission[];
  canSupervise: boolean;
  canManage: boolean;
}

export const ProblemSubmissionCard: React.FunctionComponent<ProblemSubmissionCardProps> = ({
  alias,
  submissions,
  canManage,
}) => {
  const renderAnswer = (answer?: string) => (answer && answer.length > 0 ? answer : '-');

  const renderSingleSubmission = (submission: ItemSubmission, itemNum: number) => (
    <tr key={submission.itemJid}>
      <td className="col-item-num">{itemNum + 1}</td>
      <td>{renderAnswer(submission.answer)}</td>
      {canManage && (
        <td className="col-verdict">
          {submission.grading ? <VerdictTag verdict={submission.grading.verdict} /> : '-'}
        </td>
      )}
      <td className="col-time">
        <FormattedRelative value={submission.time} />
      </td>
    </tr>
  );

  return (
    <ContentCard className="contest-bundle-problem-submission">
      <h3>{alias}</h3>
      <hr />
      <HTMLTable striped className="table-list-condensed submission-table">
        <thead>
          <tr>
            <th className="col-item-num">No</th>
            <th>Answer</th>
            {canManage && <th className="col-verdict">Verdict</th>}
            <th className="col-time">Time</th>
          </tr>
        </thead>
        <tbody>{submissions.map(renderSingleSubmission)}</tbody>
      </HTMLTable>
    </ContentCard>
  );
};