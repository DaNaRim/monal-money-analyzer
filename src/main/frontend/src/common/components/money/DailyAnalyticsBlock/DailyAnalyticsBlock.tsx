import { Checkbox, FormControlLabel, Tab, Tabs } from "@mui/material";
import { type LegendProps } from "@nivo/legends/dist/types/types";
import { ResponsivePie } from "@nivo/pie";
import React, { useMemo, useState } from "react";
import { useAppSelector } from "../../../../app/hooks/reduxHooks";
import useTranslation from "../../../../app/hooks/translation";
import { categoryColorMap } from "../../../../features/category/categoryColorMap";
import {
    type Category,
    CategoryType,
    selectTransactionCategories,
} from "../../../../features/category/categorySlice";
import {
    selectTransactionsByWalletAndDate,
} from "../../../../features/transaction/transactionSlice";
import { addSpacesToNumber } from "../../../utils/moneyUtils";
import styles from "./DailyAnalyticsBlock.module.scss";

interface DailyAnalyticsBlockProps {
    walletId: number;
    date: string;
}

interface DataTemplate {
    id: string;
    label: string;
    value: number;
    color: string;
}

const DailyAnalyticsBlock = ({ walletId, date }: DailyAnalyticsBlockProps) => {
    const t = useTranslation();

    const [selectedTab, setSelectedTab] = useState<CategoryType>(CategoryType.OUTCOME);

    const [showChildCategories, setShowChildCategories] = useState<boolean>(false);

    const transactions
        = useAppSelector(state => selectTransactionsByWalletAndDate(state, walletId, date));

    const categories = useAppSelector(selectTransactionCategories);

    const getCategoryLocalName = (category: Category | undefined) => {
        const categoryNameKey
            = category?.name.toLowerCase().replaceAll(" ", "_") ?? "";

        return category == null
            ? t.data.transactionCategory.deleted
            : t.getString(
                `data.transactionCategory.${category.type.toLowerCase()}.${categoryNameKey}`,
            );
    };

    const getParentCategory = (category: Category | null) => {
        if (category == null) {
            return null;
        }
        let result = null;
        categories.forEach(cur => cur.subCategories?.forEach(subCategory => {
            if (subCategory.id === category.id) {
                result = cur;
            }
        }));
        return result;
    };

    const getColorByCategory = (category: Category | null): string => {
        if (category == null) {
            return "";
        }
        const categoryName = category.name.toLowerCase().replaceAll(" ", "_");
        // @ts-expect-error - Because of this error: element implicitly has an any type because
        // expression of type string can't be used to index type
        return categoryColorMap[category.type.toLowerCase()][categoryName];
    };

    const getCategoryDiagramDataByType = (categoryType: CategoryType) => {
        const resultData: DataTemplate[] = [];
        transactions
            .filter(transaction => transaction.category?.type === categoryType)
            .forEach(transaction => {
                const category = showChildCategories
                    ? transaction.category
                    : getParentCategory(transaction.category) ?? transaction.category;

                if (category == null) {
                    return;
                }
                const categoryLocalName = getCategoryLocalName(category);

                const categoryData
                    = resultData.find((data) => data.id === categoryLocalName);

                if (categoryData == null) {
                    resultData.push({
                        id: categoryLocalName,
                        label: categoryLocalName,
                        value: transaction.amount,
                        color: getColorByCategory(category),
                    });
                } else {
                    categoryData.value += transaction.amount;
                }
            });
        return resultData;
    };

    const getTotalAmount = (categoryType: CategoryType) => {
        let totalAmount = 0;
        transactions.forEach(transaction => {
            if (transaction.category?.type === categoryType) {
                totalAmount += transaction.amount;
            }
        });
        return totalAmount;
    };

    const categoryDiagramData = useMemo<DataTemplate[]>(() => {
        if (selectedTab === CategoryType.OUTCOME) {
            return getCategoryDiagramDataByType(CategoryType.OUTCOME);
        }
        return getCategoryDiagramDataByType(CategoryType.INCOME);
    }, [transactions, t, selectedTab, showChildCategories]);

    const outcomeTotalAmount = useMemo(() => getTotalAmount(CategoryType.OUTCOME), [transactions]);

    const incomeTotalAmount = useMemo(() => getTotalAmount(CategoryType.INCOME), [transactions]);

    const generateLegend = (offsetY: number,
                            data: DataTemplate[],
                            sliceStart?: number,
                            sliceEnd?: number,
    ) => ({
        ...legendProps,
        translateY: offsetY,
        data: data
            .sort((a, b) => a.value - b.value)
            .slice(sliceStart, sliceEnd)
            .map(cur => ({
                id: cur.id,
                label: cur.label,
                color: cur.color,
            })),
    });

    const generateLegends = () => {
        // Split data array into chunks of four elements
        const splitData: DataTemplate[][] = [];
        const chunkSize = 4;
        for (let i = 0; i < categoryDiagramData.length; i += chunkSize) {
            splitData.push(categoryDiagramData.slice(i, i + chunkSize));
        }
        let offsetY = 56; // Value is offset for first legend

        return splitData.map(cur => {
            const legend = generateLegend(offsetY, cur);
            offsetY += 24; // Offset for next legends
            return legend;
        });
    };

    return (
        <div className={styles.analytics_wrapper}>
            <Tabs value={selectedTab}
                  classes={{
                      indicator: styles.category_type_tab_indicator,
                  }}
                  onChange={(_e, value) => setSelectedTab(value)}
                  aria-label="category type for analitics">
                <Tab className={styles.category_type_tab}
                     label={t.data.transactionCategoryType.outcome}
                     value={CategoryType.OUTCOME}/>
                <Tab className={styles.category_type_tab}
                     label={t.data.transactionCategoryType.income}
                     value={CategoryType.INCOME}/>
            </Tabs>
            <div className={styles.circle_wrapper}>
                <div className={styles.total_amount_wrapper}>
                    <p className={styles.total_amount}>{addSpacesToNumber(outcomeTotalAmount)}</p>
                    <p className={styles.total_amount}>{addSpacesToNumber(incomeTotalAmount)}</p>
                </div>
                {categoryDiagramData.length !== 0 &&
                  <FormControlLabel
                    className={styles.show_child_categories_checkbox}
                    label={t.analyticsBlock.showChildCategories}
                    control={<Checkbox
                        checked={showChildCategories}
                        onChange={() => setShowChildCategories(!showChildCategories)}
                        inputProps={{ "aria-label": "controlled" }}
                    />}
                  />
                }
                {categoryDiagramData.length === 0 &&
                  <p className={styles.no_data}>{t.analyticsBlock.noDataForAnalytics}</p>
                }
                <ResponsivePie data={categoryDiagramData}
                               theme={{ legends: { text: { fontSize: 14 } } }}
                               sortByValue={true}
                               valueFormat={value => addSpacesToNumber(value)}
                               margin={{ top: 48, right: 48, bottom: 80, left: 80 }}
                               borderWidth={1}
                               colors={{ datum: "data.color" }}
                               arcLabelsRadiusOffset={0.6}
                               arcLabelsSkipAngle={10}
                               arcLinkLabelsSkipAngle={10}
                               arcLinkLabelsThickness={2}
                               arcLinkLabelsColor={{ from: "color" }}
                               legends={generateLegends()}
                />
            </div>
        </div>
    );
};

export default DailyAnalyticsBlock;

const legendProps: LegendProps = {
    anchor: "bottom",
    direction: "row",
    justify: false,
    translateX: -20,
    itemsSpacing: 64,
    itemWidth: 120,
    itemHeight: 28,
    itemTextColor: "#999",
    itemDirection: "left-to-right",
    itemOpacity: 1,
    symbolSize: 16,
    symbolShape: "circle",
    effects: [
        {
            on: "hover",
            style: {
                itemTextColor: "#000",
            },
        },
    ],
};
