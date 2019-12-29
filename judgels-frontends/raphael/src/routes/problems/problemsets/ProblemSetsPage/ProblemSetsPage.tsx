import * as React from 'react';
import { connect } from 'react-redux';
import { RouteComponentProps } from 'react-router';
import { parse } from 'query-string';

import { LoadingState } from '../../../../components/LoadingState/LoadingState';
import Pagination from '../../../../components/Pagination/Pagination';
import { Card } from '../../../../components/Card/Card';
import SearchBox from '../../../../components/SearchBox/SearchBox';
import { ProblemSetsResponse } from '../../../../modules/api/jerahmeel/problemSet';
import { ProblemSetCard } from '../ProblemSetCard/ProblemSetCard';
import { problemSetActions as injectedProblemSetActions } from '../modules/problemSetActions';

import './ProblemSetsPage.css';

export interface ProblemSetsPageProps extends RouteComponentProps<{ name: string }> {
  onGetProblemSets: (archiveSlug?: string, name?: string, page?: number) => Promise<ProblemSetsResponse>;
}

interface ProblemSetsFilter {
  archiveSlug?: string;
  name?: string;
}

export interface ProblemSetsPageState {
  response?: Partial<ProblemSetsResponse>;
  filter?: ProblemSetsFilter;
  isFilterLoading?: boolean;
}

class ProblemSetsPage extends React.Component<ProblemSetsPageProps, ProblemSetsPageState> {
  private static PAGE_SIZE = 20;

  state: ProblemSetsPageState = {};

  constructor(props) {
    super(props);

    const queries = parse(this.props.location.search);
    const archiveSlug = queries.archiveSlug as string;
    const name = queries.name as string;

    this.state = { filter: { archiveSlug, name }, isFilterLoading: false };
  }

  render() {
    return (
      <Card title="Problemsets">
        {this.renderHeader()}
        {this.renderProblemSets()}
        {this.renderPagination()}
      </Card>
    );
  }

  private renderHeader = () => {
    return (
      <>
        <div className="content-card__section search-box-inline">{this.renderFilter()}</div>
        <div className="clearfix" />
        <div className="content-card__section">
          {this.renderFilterResultsBanner()}
          <hr />
        </div>
      </>
    );
  };

  private renderFilterResultsBanner = () => {
    const { archiveSlug, name } = this.state.filter;
    if (!archiveSlug && !name) {
      return <small>Most recently added problemsets:</small>;
    }

    const { response } = this.state;
    const archiveName = response && response.archiveName;

    if (archiveName && !name) {
      return (
        <>
          Archive <b>{archiveName}</b>:
        </>
      );
    }
    if (!name) {
      return null;
    }

    const archiveNameResult = archiveName ? (
      <>
        {' '}
        in archive <b>{archiveName}</b>
      </>
    ) : (
      ''
    );

    return (
      <>
        Search results for: <b>{name}</b>
        {archiveNameResult}
      </>
    );
  };

  private renderFilter = () => {
    const { name } = this.state.filter;
    return (
      <SearchBox
        onRouteChange={this.searchBoxUpdateQueries}
        initialValue={name || ''}
        isLoading={this.state.isFilterLoading}
      />
    );
  };

  private renderProblemSets = () => {
    const { response } = this.state;
    if (!response) {
      return <LoadingState />;
    }

    const { data, archiveDescriptionsMap } = response;
    if (!data) {
      return <LoadingState />;
    }
    if (data.page.length === 0) {
      return (
        <p>
          <small>No problemsets.</small>
        </p>
      );
    }
    return data.page.map(problemSet => (
      <ProblemSetCard
        key={problemSet.jid}
        problemSet={problemSet}
        archiveDescription={archiveDescriptionsMap[problemSet.archiveJid]}
      />
    ));
  };

  private renderPagination = () => {
    const { filter } = this.state;
    return (
      <Pagination
        pageSize={ProblemSetsPage.PAGE_SIZE}
        onChangePage={this.onChangePage}
        key={'' + filter.archiveSlug + filter.name}
      />
    );
  };

  private onChangePage = async (nextPage?: number) => {
    if (this.state.response) {
      this.setState({ response: { ...this.state.response, data: undefined } });
    }
    const { archiveSlug, name } = this.state.filter;
    const response = await this.props.onGetProblemSets(archiveSlug, name, nextPage);
    this.setState({ response, isFilterLoading: false });
    return response.data.totalCount;
  };

  private searchBoxUpdateQueries = (name: string, queries: any) => {
    this.setState(prevState => {
      const prevFilter = prevState.filter || {};
      return {
        filter: {
          ...prevFilter,
          name,
        },
        isFilterLoading: prevFilter.name !== name,
      };
    });
    return { ...queries, page: undefined, name };
  };
}

export function createProblemSetsPage(problemSetActions) {
  const mapDispatchToProps = {
    onGetProblemSets: problemSetActions.getProblemSets,
  };
  return connect(undefined, mapDispatchToProps)(ProblemSetsPage);
}

export default createProblemSetsPage(injectedProblemSetActions);
