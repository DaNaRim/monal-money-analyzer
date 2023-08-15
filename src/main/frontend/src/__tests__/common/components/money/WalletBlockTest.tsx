import { describe } from "@jest/globals";
import { fireEvent, screen, waitFor, within } from "@testing-library/react";
import { setupStore } from "../../../../app/store";
import WalletBlock from "../../../../common/components/money/WalletBlock/WalletBlock";
import { renderWithProviders } from "../../../../common/utils/test-utils";

describe("WalletBlock", () => {
    it("render", async () => {
        const store = setupStore({
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
        });
        const elProps = {
            selectedWalletId: "1",
            setSelectedWalletId: jest.fn(),
        };
        renderWithProviders(<WalletBlock {...elProps} />, { store });

        // Display a selected wallet name
        await waitFor(() => expect(screen.getByText("Wallet 1")).toBeInTheDocument());
    });

    it("render no wallets", async () => {
        const store = setupStore({
            wallets: {
                wallets: [],
                isInitialized: true,
            },
        });
        const elProps = {
            selectedWalletId: undefined,
            setSelectedWalletId: jest.fn(),
        };
        renderWithProviders(<WalletBlock {...elProps} />, { store });

        // Add new wallet button
        await waitFor(() => expect(screen.getByText("Add new wallet")).toBeInTheDocument());
    });

    it("render no wallets, selectedWalletId exists", async () => {
        const store = setupStore({
            wallets: {
                wallets: [],
                isInitialized: true,
            },
        });
        const elProps = {
            selectedWalletId: "1",
            setSelectedWalletId: jest.fn(),
        };
        renderWithProviders(<WalletBlock {...elProps} />, { store });

        // Add new wallet button
        await waitFor(() => expect(screen.getByText("Add new wallet")).toBeInTheDocument());
    });

    it("select wallet", async () => {
        const store = setupStoreWithWallets();
        const elProps = {
            selectedWalletId: "1",
            setSelectedWalletId: jest.fn(),
        };
        renderWithProviders(<WalletBlock {...elProps} />, { store });

        fireEvent.mouseDown(screen.getByRole("button"));
        const listbox = within(screen.getByRole("listbox"));
        fireEvent.click(listbox.getByText("Wallet 2"));

        await waitFor(() => {
            expect(elProps.setSelectedWalletId).toBeCalledTimes(1);
            expect(elProps.setSelectedWalletId).toBeCalledWith("2");
        });
    });

    it("select add wallet button -> open modal", async () => {
        const store = setupStoreWithWallets();
        const elProps = {
            selectedWalletId: "1",
            setSelectedWalletId: jest.fn(),
        };
        renderWithProviders(<WalletBlock {...elProps} />, { store });

        fireEvent.mouseDown(screen.getByRole("button"));
        const listbox = within(screen.getByRole("listbox"));
        fireEvent.click(listbox.getByText("Add new wallet"));

        await waitFor(() => expect(screen.getByTestId("createWalletForm")).toBeInTheDocument());
    });
});

function setupStoreWithWallets() {
    return setupStore({
        wallets: {
            wallets: [
                {
                    id: 1,
                    name: "Wallet 1",
                    balance: 100,
                    currency: "USD",
                },
                {
                    id: 2,
                    name: "Wallet 2",
                    balance: 100,
                    currency: "USD",
                },
            ],
            isInitialized: true,
        },
    });
}
