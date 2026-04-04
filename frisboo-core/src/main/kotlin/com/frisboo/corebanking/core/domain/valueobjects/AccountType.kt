/*
 * Copyright 2025 Frisboo Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.frisboo.corebanking.core.domain.valueobjects

/**
 * Represents the various types of accounts that can exist in the core banking system.
 */
public enum class AccountType {
    /** A brokerage account used for trading and managing investments. */
    BROKERAGE,

    /** A general business account. */
    BUSINESS,

    /** A checking account for business purposes. */
    BUSINESS_CHECKING,

    /** A savings account for business purposes. */
    BUSINESS_SAVINGS,

    /** A certificate of deposit account, typically with a fixed term and interest rate. */
    CERTIFICATE_OF_DEPOSIT,

    /** A standard checking account for personal use. */
    CHECKING,

    /** A corporate account used by large organizations. */
    CORPORATE,

    /** A credit card account. */
    CREDIT_CARD,

    /** A custodial account managed on behalf of a minor or dependent. */
    CUSTODIAL,

    /** An escrow account used to hold funds temporarily during transactions. */
    ESCROW,

    /** An estate account used to manage the assets of a deceased individual. */
    ESTATE,

    /** A flexible spending account, often used for healthcare or dependent care expenses. */
    FLEXIBLE_SPENDING,

    /** A foreign account used for international banking. */
    FOREIGN,

    /** A government account used by public sector entities. */
    GOVERNMENT,

    /** A health savings account used for medical expenses. */
    HEALTH_SAVINGS,

    /** An individual retirement account (IRA) for retirement savings. */
    INDIVIDUAL_RETIREMENT,

    /** An investment account used for managing securities and other assets. */
    INVESTMENT,

    /** A joint account shared by two or more individuals. */
    JOINT,

    /** An account for a limited liability company (LLC). */
    LIMITED_LIABILITY_COMPANY,

    /** A line of credit account allowing borrowing up to a specified limit. */
    LINE_OF_CREDIT,

    /** A loan account used to track borrowed funds and repayments. */
    LOAN,

    /** A merchant account used for processing payments and transactions. */
    MERCHANT,

    /** A money market account offering higher interest rates than standard savings accounts. */
    MONEY_MARKET,

    /** A mortgage account used for home loans. */
    MORTGAGE,

    /** A non-profit account used by charitable organizations. */
    NON_PROFIT,

    /** A retirement account used for saving and investing for retirement. */
    RETIREMENT,

    /** A standard savings account for personal use. */
    SAVINGS,

    /** A student account designed for individuals in education. */
    STUDENT,

    /** A treasury account used for managing government or institutional funds. */
    TREASURY,

    /** A trust account managed on behalf of a beneficiary. */
    TRUST,

    ;

    public companion object {
        public fun all(): List<AccountType> = entries
    }
}
