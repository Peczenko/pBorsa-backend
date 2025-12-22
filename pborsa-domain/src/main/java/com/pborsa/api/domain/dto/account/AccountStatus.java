package com.pborsa.api.domain.dto.account;

/**
 * Enum representing Alpaca account status.
 */
public enum AccountStatus {
    /**
     * Account is active and can trade.
     */
    ACTIVE,

    /**
     * Account is onboarding.
     */
    ONBOARDING,

    /**
     * Account submission is pending.
     */
    SUBMISSION_FAILED,

    /**
     * Account is submitted and under review.
     */
    SUBMITTED,

    /**
     * Account requires action from user.
     */
    ACTION_REQUIRED,

    /**
     * Account update is pending.
     */
    ACCOUNT_UPDATED,

    /**
     * Account is disabled.
     */
    DISABLED,

    /**
     * Account approval is pending.
     */
    APPROVAL_PENDING
}

