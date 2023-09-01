import dayjs from "dayjs";
import utc from "dayjs/plugin/utc";
import React, { useEffect } from "react";
import { useAppDispatch, useAppSelector } from "../../../../app/hooks/reduxHooks";
import useTranslation from "../../../../app/hooks/translation";
import {
    CategoryType,
    selectCategoriesWithSubcategories,
    selectIsCategoriesInitialized,
} from "../../../../features/category/categorySlice";
import { useGetTransactionsQuery } from "../../../../features/transaction/transactionApiSlice";
import {
    saveTransactions,
    selectIsTransactionsInitByWalletAndDate,
    selectTransactionsByWalletAndDate,
    type Transaction,
} from "../../../../features/transaction/transactionSlice";
import { selectIsWalletsExists } from "../../../../features/wallet/walletSlice";
import { addSpacesToNumber } from "../../../utils/moneyUtils";
import styles from "./TransactionBlock.module.scss";

dayjs.extend(utc);

interface TransactionBlockProps {
    walletId: number;
    date: string;
}

const TransactionBlock = ({ walletId, date }: TransactionBlockProps) => {
    const t = useTranslation();
    const dispatch = useAppDispatch();

    const isCategoriesInitialized = useAppSelector(selectIsCategoriesInitialized);
    const categoriesWithSubcategories = useAppSelector(selectCategoriesWithSubcategories);

    const isWalletsExists = useAppSelector(selectIsWalletsExists);

    const transactions
        = useAppSelector(state => selectTransactionsByWalletAndDate(state, walletId, date));

    const isTransactionsInitialized
        = useAppSelector(state => selectIsTransactionsInitByWalletAndDate(state, walletId, date));

    const {
        data: fetchedTransactions,
        isFetching: isTransactionsFetching,
        isError: isTransactionsError,
    } = useGetTransactionsQuery({
        walletId,
        date,
    }, { skip: isTransactionsInitialized || !isWalletsExists });

    useEffect(() => {
        if (fetchedTransactions == null || !isCategoriesInitialized) {
            return;
        }
        const preparedTransactions = fetchedTransactions.map(transaction => {
            const category = categoriesWithSubcategories
                ?.find(category => category.id === transaction.categoryId) ?? null;

            const parsedTransaction: Transaction = {
                ...transaction,
                category,
            };
            return parsedTransaction;
        });
        dispatch(saveTransactions({
            walletId,
            date,
            transactions: preparedTransactions,
        }));
    }, [fetchedTransactions, isCategoriesInitialized]);

    return (
        <div className={styles.transactions} data-testid="transaction-block">
            {(!isCategoriesInitialized || isTransactionsFetching)
                && <p className={styles.message}>{t.transactionBlock.loading}</p>
            }
            {!isWalletsExists && !isTransactionsFetching && isCategoriesInitialized
                && <p className={styles.message}>{t.transactionBlock.noWallets}</p>
            }
            {isTransactionsError
                && <p className={styles.error}>{t.transactionBlock.failToLoadTransactions}</p>
            }
            {isCategoriesInitialized
                && !isTransactionsFetching
                && transactions.length === 0
                && !isTransactionsError
                && isWalletsExists
                && <p className={styles.message}>{t.transactionBlock.noTransactions}</p>
            }
            {isCategoriesInitialized && !isTransactionsFetching && transactions.length > 0
                && transactions.map(transaction => (
                    <TransactionElement key={transaction.id} transaction={transaction}/>
                ))
            }
        </div>
    );
};

export default TransactionBlock;

const TransactionElement = ({ transaction }: { transaction: Transaction }) => {
    const t = useTranslation();

    const isIncome = transaction.category?.type === CategoryType.INCOME;

    const category = transaction.category;

    const categoryNameKey
        = category?.name.toLowerCase().replaceAll(" ", "_") ?? "";

    const categoryName = category == null
        ? t.data.transactionCategory.deleted
        : t.getString(`data.transactionCategory.${category.type.toLowerCase()}.${categoryNameKey}`);

    return (
        <div className={styles.transaction}>
            <p className={styles.transaction_date}>
                {dayjs.utc(transaction.date).local().format("HH:mm")}
            </p>
            <div className={styles.transaction_display}>
                <div className={styles.transaction_left}>
                    <p className={styles.transaction_category}>
                        {categoryName}
                    </p>
                    <p className={styles.transaction_description}>{transaction.description}</p>
                </div>
                <p className={`${styles.transaction_amount}`
                    + ` ${isIncome ? styles.transaction_income : ""}`}>
                    {addSpacesToNumber(transaction.amount)}
                </p>
            </div>
        </div>
    );
};
