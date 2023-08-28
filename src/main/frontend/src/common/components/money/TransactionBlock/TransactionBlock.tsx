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
    saveTransactionsByWalletAndDate,
    selectIsTransactionsInitByWalletAndDate,
    selectTransactionsByWalletAndDate,
    type Transaction,
} from "../../../../features/transaction/transactionSlice";
import styles from "./TransactionBlock.module.scss";

dayjs.extend(utc);

interface TransactionBlockProps {
    walletId: number;
    date: string;
}

const TransactionBlock = ({ walletId, date }: TransactionBlockProps) => {
    const dispatch = useAppDispatch();

    const isCategoriesInitialized = useAppSelector(selectIsCategoriesInitialized);
    const categoriesWithSubcategories = useAppSelector(selectCategoriesWithSubcategories);

    const transactions
        = useAppSelector(state => selectTransactionsByWalletAndDate(state, walletId, date));

    const isTransactionsInitialized
        = useAppSelector(state => selectIsTransactionsInitByWalletAndDate(state, walletId, date));

    const {
        data: fetchedTransactions,
        isFetching: isTransactionsFetching,
    } = useGetTransactionsQuery({ walletId, date }, { skip: isTransactionsInitialized });

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
        dispatch(saveTransactionsByWalletAndDate({
            walletId,
            date,
            transactions: preparedTransactions,
        }));
    }, [fetchedTransactions, isCategoriesInitialized]);

    return (
        <div className={styles.transactions}>
            {(!isCategoriesInitialized || isTransactionsFetching) &&
              <p className={styles.message}>Loading...</p>
            }
            {isCategoriesInitialized && !isTransactionsFetching && transactions.length === 0
                && <p className={styles.message}>No transactions</p>
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
                    {transaction.amount}
                </p>
            </div>
        </div>
    );
};
