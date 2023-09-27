import { describe } from "@jest/globals";
import { screen, waitFor, waitForElementToBeRemoved } from "@testing-library/react";
import dayjs from "dayjs";
import { rest } from "msw";
import { setupServer } from "msw/node";
import AnalyticsBar from "../../../../../common/components/money/AnalyticsBar/AnalyticsBar";
import { renderWithProviders } from "../../../../../common/utils/test-utils";
import { type ViewAnalyticsDto } from "../../../../../features/analytics/analyticsApiSlice";

describe("AnalyticsBar", () => {
    const handlers = [
        rest.get("/api/v1/analytics", async (req, res, ctx) => {
            const walletId = req.url.searchParams.get("walletId");

            if (walletId === "0") {
                return await res(ctx.status(500));
            }
            const analytics: ViewAnalyticsDto = {
                income: {
                    "2021-01-01": {
                        "Category 1": 100,
                        "Category 2": 200,
                    },
                    "2021-01-02": {
                        "Category 1": 100,
                    },
                },
                outcome: {
                    "2021-01-02": {
                        "Category 3": 100,
                    },
                },
            };
            return await res(ctx.status(200), ctx.json(analytics));
        }),
    ];

    const server = setupServer(...handlers);

    beforeAll(() => server.listen());
    afterEach(() => server.resetHandlers());
    afterAll(() => server.close());

    it("render", async () => {
        renderWithProviders(<AnalyticsBar walletId={1}/>);

        expect(screen.getByText("Loading...")).toBeInTheDocument();

        expect(screen.getByTitle("Previous month")).toBeInTheDocument();
        expect(screen.getByText(dayjs().format("MMMM YYYY"))).toBeInTheDocument();
        expect(screen.getByTitle("Next month")).toBeInTheDocument();

        expect(screen.getByText("Show categories")).toBeInTheDocument();
        expect(screen.getByText("Show child categories")).toBeInTheDocument();

        expect(document.querySelector("div[style=\"width: 100%; height: 100%;\"]"))
            .toBeInTheDocument();

        await waitFor(() => expect(screen.queryByText("Loading...")).not.toBeInTheDocument());
    });

    it("render fetch Analytics Error", async () => {
        renderWithProviders(<AnalyticsBar walletId={0}/>);

        await waitForElementToBeRemoved(() => screen.getByText("Loading..."));

        expect(screen.getByText("Failed to load analytics")).toBeInTheDocument();
        expect(document.querySelector("div[style=\"width: 100%; height: 100%;\"]"))
            .toBeInTheDocument();
    });
});
