import dayjs from "dayjs";
import utc from "dayjs/plugin/utc";
import React from "react";
import useTranslation from "../../../../app/hooks/translation";
import { CategoryType } from "../../../../features/category/categorySlice";
import { type Transaction } from "../../../../features/transaction/transactionSlice";
import { addSpacesToNumber } from "../../../utils/moneyUtils";
import styles from "./TransactionBlock.module.scss";

dayjs.extend(utc);

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

export default TransactionElement;
