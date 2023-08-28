import { faChevronLeft, faChevronRight } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import dayjs from "dayjs";
import React, { useEffect, useRef, useState } from "react";
import useTranslation from "../../../../app/hooks/translation";
import styles from "../DateBlock/DateBlock.module.scss";

export const DATE_BLOCK_DATE_FORMAT = "YYYY-MM-DD";

const highestAllowedDate = dayjs(new Date()).add(10, "year").format(DATE_BLOCK_DATE_FORMAT);

interface DateBlockProps {
    date: string;
    setDate: React.Dispatch<React.SetStateAction<string>>;
}

const DateBlock = ({ date, setDate }: DateBlockProps) => {
    const t = useTranslation();

    const [newDate, setNewDate] = useState<string>(date);
    const [isDateError, setIsDateError] = useState<boolean>(false);
    const dateInputRef = useRef<HTMLInputElement>(null);

    const setPreviousDay = () => {
        const newDate0 = dayjs(date).subtract(1, "day").format(DATE_BLOCK_DATE_FORMAT);
        setDate(newDate0);
        setNewDate(newDate0);
    };

    const setNextDay = () => {
        const newDate0 = dayjs(date).add(1, "day").format(DATE_BLOCK_DATE_FORMAT);
        setDate(newDate0);
        setNewDate(newDate0);
    };

    const confirmDateSelection = () => {
        if (dayjs(newDate).isBefore("2000-01-01")) {
            setNewDate("2000-01-01");
            setDate("2000-01-01");
            return;
        }
        if (dayjs(newDate).isAfter(highestAllowedDate)) {
            setNewDate(highestAllowedDate);
            setDate(highestAllowedDate);
            return;
        }
        if (dayjs(newDate).isValid()) {
            setDate(newDate);
        } else {
            setIsDateError(true);
        }
    };

    const displayDayOfWeek = () => {
        if (isDateError) {
            return <p className={styles.dateError}>{t.dateBlock.invalidDateError}</p>;
        }
        return dayjs(newDate).isValid()
            ? <p>{t.getString(`data.dayOfWeek.${(dayjs(newDate).format("d"))}`)}</p>
            : <p>{t.dateBlock.invalidDate}</p>;
    };

    const handleDateInputKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter") {
            dateInputRef.current?.blur();
        }
    };

    useEffect(() => {
        if (isDateError && dayjs(newDate).isValid()) {
            setDate(newDate);
            setIsDateError(false);
        }
    }, [newDate]);

    return (
        <div className={styles.date_block}>
            <button className={styles.arrow_button}
                    onClick={setPreviousDay}
                    title={t.dateBlock.prevDay}>
                <FontAwesomeIcon icon={faChevronLeft} data-testid="date-previous"/>
            </button>
            <div className={styles.date_display}>
                <input type="date"
                       ref={dateInputRef}
                       className={styles.date_input}
                       required={true}
                       value={newDate}
                       min="2000-01-01"
                       max={highestAllowedDate}
                       onChange={(e) => setNewDate(e.target.value)}
                       onBlur={() => confirmDateSelection()}
                       onKeyDown={handleDateInputKeyDown}
                />
                {displayDayOfWeek()}
            </div>
            <button className={styles.arrow_button}
                    onClick={setNextDay}
                    title={t.dateBlock.nextDay}>
                <FontAwesomeIcon icon={faChevronRight} data-testid="date-next"/>
            </button>
        </div>
    );
};

export default DateBlock;
