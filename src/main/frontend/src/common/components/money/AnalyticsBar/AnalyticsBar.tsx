import { faChevronLeft, faChevronRight } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Checkbox, FormControlLabel } from "@mui/material";
import { ResponsiveBar } from "@nivo/bar";
import dayjs from "dayjs";
import React, { useEffect, useState } from "react";
import { type LocalizedStrings } from "react-localization";
import { useAppDispatch, useAppSelector } from "../../../../app/hooks/reduxHooks";
import useTranslation from "../../../../app/hooks/translation";
import { useGetDailyAnalyticsQuery } from "../../../../features/analytics/analyticsApiSlice";
import {
    type AnalyticsBarData,
    AnalyticsPeriodType,
    saveAnalyticsFromServer,
    selectAnalyticsForBarChart,
} from "../../../../features/analytics/analyticsSlice";
import {
    selectCategoriesWithSubcategories,
    selectIsCategoriesInitialized,
} from "../../../../features/category/categorySlice";
import {
    getCategoryLocalName,
    getColorByCategory,
} from "../../../../features/category/categoryUtil";
import { type Localization } from "../../../../i18n";
import { getParsedCurrentDate } from "../../../pages/TransactionsPage/TransactionsPage";
import { DATE_BLOCK_DATE_FORMAT } from "../DateBlock/DateBlock";
import styles from "./AnalyticsBar.module.scss";
import ZeroLine from "./ZeroLine";

interface AnalyticsBarProps {
    walletId: number;
}

const AnalyticsBar = ({ walletId }: AnalyticsBarProps) => {
    const t = useTranslation();
    const dispatch = useAppDispatch();

    const [periodType] = useState<AnalyticsPeriodType>(AnalyticsPeriodType.DAY);

    const [selectedDate, setSelectedDate] = useState<string>(getParsedCurrentDate());

    const [showCategories, setShowCategories] = useState<boolean>(false);

    const [showChildCategories, setShowChildCategories] = useState<boolean>(false);

    const [maxNumberWithoutSign, setMaxNumberWithoutSign] = useState<number>(0);

    const {
        data: analyticsFetchData,
        isFetching,
        isError: isAnalyticsError,
    } = useGetDailyAnalyticsQuery({
        walletId,
        date: selectedDate,
    }, { skip: periodType !== AnalyticsPeriodType.DAY });

    const categories = useAppSelector(selectCategoriesWithSubcategories);
    const isCategoriesInitialized = useAppSelector(selectIsCategoriesInitialized);

    const analyticsData = useAppSelector(state => selectAnalyticsForBarChart(
        state,
        walletId,
        periodType,
        selectedDate,
        { showCategories, showChildCategories },
    ), (left, right) => JSON.stringify(left) === JSON.stringify(right));

    useEffect(() => {
        dispatch(saveAnalyticsFromServer({
            walletId,
            data: analyticsFetchData,
            periodType,
        }));
    }, [analyticsFetchData, isCategoriesInitialized]);

    useEffect(() => {
        setMaxNumberWithoutSign(calculateMaxNumber(analyticsData));
    }, [analyticsData]);

    return (
        <div className={styles.analytics_bar} data-testid="analytics-bar">
            {isFetching && <div className={styles.data_loader}>{t.analyticsBar.loading}</div>}
            {isAnalyticsError && <div>{t.analyticsBar.error}</div>}
            {!isAnalyticsError &&
              <>
                <div className={styles.controls}>
                  <div className={styles.date_wrapper}>
                    <button className={styles.arrow_button}
                            onClick={() =>
                                setSelectedDate(getPreviousDate(selectedDate, periodType))
                            }
                            title={getDateButtonTitle(periodType, "prev", t)}>
                      <FontAwesomeIcon icon={faChevronLeft}/>
                    </button>
                    <p>{getDateToDisplay(selectedDate, periodType, t)}</p>
                    <button className={styles.arrow_button}
                            onClick={() => setSelectedDate(getNextDate(selectedDate, periodType))}
                            title={getDateButtonTitle(periodType, "next", t)}>
                      <FontAwesomeIcon icon={faChevronRight}/>
                    </button>
                  </div>
                  <div className={styles.checkbox_wrapper}>
                    <FormControlLabel
                      className={styles.checkbox}
                      label={t.analyticsBar.showCategories}
                      control={<Checkbox
                          checked={showCategories}
                          onChange={() => setShowCategories(!showCategories)}
                          inputProps={{ "aria-label": "controlled" }}
                      />}
                    />
                    <FormControlLabel
                      className={styles.checkbox}
                      label={t.analyticsBar.showChildCategories}
                      disabled={!showCategories}
                      control={<Checkbox
                          checked={showChildCategories}
                          onChange={() => setShowChildCategories(!showChildCategories)}
                          inputProps={{ "aria-label": "controlled" }}
                      />}
                    />
                  </div>
                </div>
                <ResponsiveBar
                  data={analyticsData}
                  keys={[...categories.map(cur => cur.name), "income", "outcome"]}
                  indexBy="date"
                  margin={{ top: 20, right: 100, bottom: 50, left: 100 }}
                  maxValue={maxNumberWithoutSign}
                  minValue={-maxNumberWithoutSign}
                  enableLabel={false}
                  colors={datum => getColorByCategory(datum.id as string, categories)}
                  tooltipLabel={datum =>
                      getCategoryLocalName(datum.id as string, categories, t)}
                  layers={[
                      "grid",
                      "axes",
                      "bars",
                      ZeroLine,
                      "annotations",
                      "legends",
                  ]}
                />
              </>
            }
        </div>
    );
};

export default AnalyticsBar;

const calculateMaxNumber = (analyticsData: AnalyticsBarData[]): number => {
    let max = 0;
    analyticsData.forEach(cur => {
        let sumOutcome = 0;
        let sumIncome = 0;
        // For i = 0 we have date
        for (let i = 1; i < Object.keys(cur).length; i++) {
            const value = Number(Object.values(cur)[i]);
            if (value > 0) {
                sumIncome += value;
            }
            if (value < 0) {
                sumOutcome += value;
            }
        }
        if (sumIncome > max) {
            max = sumIncome;
        }
        if (Math.abs(sumOutcome) > max) {
            max = Math.abs(sumOutcome);
        }
    });
    if (max === 0) {
        max = 1000; // If there is no data, we set max to 100
    }
    return max + max * 0.1;
};

const getDateToDisplay = (date: string,
                          periodType: AnalyticsPeriodType,
                          t: LocalizedStrings<Localization>,
): string => {
    switch (periodType) {
        case AnalyticsPeriodType.DAY:
            return `${t.getString(`data.month.${dayjs(date).format("M")}`)}`
                + ` ${dayjs(date).format("YYYY")}`;
        case AnalyticsPeriodType.MONTH:
            return dayjs(date).format("YYYY");
        case AnalyticsPeriodType.YEAR:
            return `${dayjs(date).subtract(3, "year").format("YYYY")}`
                + ` - ${dayjs(date).add(3, "year").format("YYYY")}`;
    }
};

const getNextDate = (date: string, periodType: AnalyticsPeriodType): string => {
    switch (periodType) {
        case AnalyticsPeriodType.DAY:
            return dayjs(date).add(1, "month").format(DATE_BLOCK_DATE_FORMAT);
        case AnalyticsPeriodType.MONTH:
            return dayjs(date).add(1, "year").format(DATE_BLOCK_DATE_FORMAT);
        case AnalyticsPeriodType.YEAR:
            return dayjs(date).add(3, "year").format(DATE_BLOCK_DATE_FORMAT);
    }
};

const getPreviousDate = (date: string, periodType: AnalyticsPeriodType): string => {
    switch (periodType) {
        case AnalyticsPeriodType.DAY:
            return dayjs(date).subtract(1, "month").format(DATE_BLOCK_DATE_FORMAT);
        case AnalyticsPeriodType.MONTH:
            return dayjs(date).subtract(1, "year").format(DATE_BLOCK_DATE_FORMAT);
        case AnalyticsPeriodType.YEAR:
            return dayjs(date).subtract(3, "year").format(DATE_BLOCK_DATE_FORMAT);
    }
};

const getDateButtonTitle = (periodType: AnalyticsPeriodType,
                            moveTo: "next" | "prev",
                            t: LocalizedStrings<Localization>,
): string => {
    switch (periodType) {
        case AnalyticsPeriodType.DAY:
            return t.getString(`analyticsBar.dateControl.${moveTo}.month`);
        case AnalyticsPeriodType.MONTH:
            return t.getString(`analyticsBar.dateControl.${moveTo}.year`);
        case AnalyticsPeriodType.YEAR:
            return t.formatString(
                t.getString(`analyticsBar.dateControl.${moveTo}.years`), 3,
            ).toString();
    }
};
