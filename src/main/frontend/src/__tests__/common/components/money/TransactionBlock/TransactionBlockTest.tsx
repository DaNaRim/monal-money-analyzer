import { describe } from "@jest/globals";
import { screen, waitForElementToBeRemoved } from "@testing-library/react";
import dayjs from "dayjs";
import { rest } from "msw";
import { setupServer } from "msw/node";
import { setupStore } from "../../../../../app/store";
import TransactionBlock
    from "../../../../../common/components/money/TransactionBlock/TransactionBlock";
import { renderWithProviders } from "../../../../../common/utils/test-utils";
import { type Category, CategoryType } from "../../../../../features/category/categorySlice";

describe("TransactionBlock", () => {
    const handlers = [
        rest.get("/api/v1/transaction/date", async (req, res, ctx) => {
            const from = req.url.searchParams.get("from") ?? ""; // YYYY-MM-DD HH format

            if (from.startsWith("2023-03")) {
                return await res(ctx.status(200));
            }
            if (from.startsWith("2023-02")) {
                res.networkError("Failed to connect");
                return;
            }
            const transactions = [
                {
                    id: 1,
                    categoryId: 1,
                    amount: 100,
                    date: dayjs("2023-08-28").utc().toDate(),
                    description: "Description 1",
                },
            ];
            return await res(ctx.status(200), ctx.json(transactions));
        }),
    ];

    const server = setupServer(...handlers);

    beforeAll(() => server.listen());
    afterEach(() => server.resetHandlers());
    afterAll(() => server.close());

    it("render", async () => {
        const store = setupTransactionBlockState();
        renderWithProviders(<TransactionBlock walletId={1} date={"2023-08-28"}/>, { store });

        await waitForElementToBeRemoved(() => screen.getByText("Loading..."));

        expect(screen.getByText("Food and beverages")).toBeInTheDocument();
        expect(screen.getByText("100")).toBeInTheDocument();
        expect(screen.getByText("Description 1")).toBeInTheDocument();
        expect(screen.queryByText("No transactions")).not.toBeInTheDocument();
    });

    it("render no Categories", async () => {
        const store = setupTransactionBlockState();
        renderWithProviders(<TransactionBlock walletId={1} date={"2023-03-03"}/>, { store });

        await waitForElementToBeRemoved(() => screen.getByText("Loading..."));

        expect(screen.getByText("No transactions")).toBeInTheDocument();
        expect(screen.queryByText("Food and beverages")).not.toBeInTheDocument();
        expect(screen.queryByText("100")).not.toBeInTheDocument();
        expect(screen.queryByText("Description 1")).not.toBeInTheDocument();
    });

    it("render no Wallets", async () => {
        const store = setupStore({
            categories: {
                categories: [
                    {
                        id: 1,
                        name: "Food and beverages",
                        type: CategoryType.OUTCOME,
                        subCategories: [],
                    },
                ],
                isInitialized: true,
            },
            wallets: {
                wallets: [],
                isInitialized: true,
            },
        });
        renderWithProviders(<TransactionBlock walletId={1} date={"2023-08-28"}/>, { store });

        expect(screen.getByText("Add a wallet to start")).toBeInTheDocument();
        expect(screen.queryByText("Loading...")).not.toBeInTheDocument();
        expect(screen.queryByText("No transactions")).not.toBeInTheDocument();
    });

    it("render network error", async () => {
        const store = setupTransactionBlockState();
        renderWithProviders(<TransactionBlock walletId={1} date={"2023-02-02"}/>, { store });

        await waitForElementToBeRemoved(() => screen.getByText("Loading..."));

        expect(screen.getByText("Failed to load transactions")).toBeInTheDocument();
    });
});

function setupTransactionBlockState() {
    const category: Category = {
        id: 1,
        name: "Food and beverages",
        type: CategoryType.OUTCOME,
        subCategories: [],
    };
    return setupStore({
        wallets: {
            wallets: [
                {
                    id: 1,
                    name: "Wallet 1",
                    balance: 100,
                    currency: "USD",
                },
            ],
            isInitialized: true,
        },
        categories: {
            categories: [
                category,
            ],
            isInitialized: true,
        },
    });
}
