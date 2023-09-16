import { type BarCustomLayerProps } from "@nivo/bar/dist/types/types";
import { type AnalyticsBarData } from "../../../../features/analytics/analyticsSlice";

const COLOR = "#000";

/**
 * Horizontal line at 0 y coordinate and label 0
 *
 * @param yScale function to scale y coordinate (from nivo)
 * @param innerWidth width of the chart (from nivo)
 *
 */
const ZeroLine = ({ yScale, innerWidth }: BarCustomLayerProps<AnalyticsBarData>) => (
    <>
        <line
            x1={0}
            x2={innerWidth}
            y1={yScale(0)}
            y2={yScale(0)}
            stroke={COLOR}
            strokeWidth={1}
        />
        <text
            x={-10}
            y={yScale(0)}
            textAnchor="end"
            dominantBaseline="central"
            fontWeight="bold"
            fontSize="12"
            style={{ fill: COLOR }}
        >
            0
        </text>
    </>
);

export default ZeroLine;
