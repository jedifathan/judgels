import { push } from 'react-router-redux';

import { resetPasswordActions } from './resetPasswordActions';
import { BadRequestError } from '../../../../modules/api/error';

describe('resetPasswordActions', () => {
  let dispatch: jest.Mock<any>;
  let getState: jest.Mock<any>;

  let userAccountAPI: jest.Mocked<any>;
  let toastActions: jest.Mocked<any>;

  beforeEach(() => {
    dispatch = jest.fn();
    getState = jest.fn();

    userAccountAPI = {
      resetPassword: jest.fn(),
    };
    toastActions = {
      showSuccessToast: jest.fn(),
    };
  });

  describe('reset()', () => {
    const { reset } = resetPasswordActions;
    const doReset = async () => reset('code123', 'pass')(dispatch, getState, { userAccountAPI, toastActions });

    it('calls API to reset password', async () => {
      await doReset();

      expect(userAccountAPI.resetPassword).toHaveBeenCalledWith({
        emailCode: 'code123',
        newPassword: 'pass',
      });
    });

    describe('when the email code is valid', () => {
      beforeEach(async () => {
        userAccountAPI.resetPassword.mockImplementation(() => Promise.resolve());

        await doReset();
      });

      it('succeeds with toast', () => {
        expect(toastActions.showSuccessToast).toHaveBeenCalledWith('Password has been reset.');
      });

      it('redirects to /login', () => {
        expect(dispatch).toHaveBeenCalledWith(push('/login'));
      });
    });

    describe('when the email code is invalid', () => {
      let error: any;

      beforeEach(async () => {
        error = new BadRequestError();
        userAccountAPI.resetPassword.mockImplementation(() => {
          throw error;
        });
      });

      it('throws a more descriptive error', async () => {
        await expect(doReset()).rejects.toEqual(new Error('Invalid code.'));
      });
    });
  });
});