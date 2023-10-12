import { faEllipsisVertical } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Menu, MenuItem } from "@mui/material";
import dayjs from "dayjs";
import utc from "dayjs/plugin/utc";
import React, { useState } from "react";
import { useAppSelector } from "../../../../app/hooks/reduxHooks";
import useTranslation from "../../../../app/hooks/translation";
import {
    CategoryType,
    selectTransactionCategories,
} from "../../../../features/category/categorySlice";
import { getCategoryLocalName } from "../../../../features/category/categoryUtil";
import { type Transaction } from "../../../../features/transaction/transactionSlice";
import DeleteTransactionModal from "../../../modal/DeleteTransactionModal/DeleteTransactionModal";
import UpdateTransactionModal from "../../../modal/UpdateTransactionModal/UpdateTransactionModal";
import { addSpacesToNumber } from "../../../utils/moneyUtils";
import styles from "./TransactionBlock.module.scss";

dayjs.extend(utc);

/**
 * Transaction element component in transaction list
 *
 * @param transaction - transaction to display
 */
const TransactionElement = ({ transaction }: { transaction: Transaction }) => {
    const t = useTranslation();

    // Needed to display a menu near transaction element
    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

    const isIncome = transaction.category?.type === CategoryType.INCOME;

    const categories = useAppSelector(selectTransactionCategories);

    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState<boolean>(false);

    const [isEditModalOpen, setIsEditModalOpen] = useState<boolean>(false);

    const handleDelete = () => {
        setIsDeleteModalOpen(true);
        setAnchorEl(null); // close menu
    };

    const handleEdit = () => {
        setIsEditModalOpen(true);
        setAnchorEl(null); // close menu
    };

    return (
        <div className={styles.transaction}>
            <p className={styles.transaction_date}>
                {dayjs(transaction.date).format("HH:mm")}
            </p>
            <div className={styles.transaction_display}>
                <div className={styles.transaction_left}>
                    <p className={styles.transaction_category}>
                        {getCategoryLocalName(transaction.category, categories, t)}
                    </p>
                    <p className={styles.transaction_description}>{transaction.description}</p>
                </div>
                <div className={styles.transaction_right}>
                    <p className={`${styles.transaction_amount}`
                        + ` ${isIncome ? styles.transaction_income : ""}`}>
                        {addSpacesToNumber(transaction.amount)}
                    </p>
                    <button className={styles.controls_icon}
                            onClick={e => setAnchorEl(e.currentTarget)}>
                        <FontAwesomeIcon icon={faEllipsisVertical}/>
                    </button>
                </div>
            </div>
            <Menu
                open={Boolean(anchorEl)}
                onClose={() => setAnchorEl(null)}
                anchorEl={anchorEl}
            >
                <MenuItem key="delete" onClick={handleDelete}>
                    {t.transactionElement.menu.delete}
                </MenuItem>
                <MenuItem key="edit" onClick={handleEdit}>
                    {t.transactionElement.menu.edit}
                </MenuItem>
            </Menu>
            <DeleteTransactionModal open={isDeleteModalOpen}
                                    setOpen={setIsDeleteModalOpen}
                                    transaction={transaction}/>
            <UpdateTransactionModal open={isEditModalOpen}
                                    setOpen={setIsEditModalOpen}
                                    transaction={transaction}/>
        </div>
    );
};

export default TransactionElement;
