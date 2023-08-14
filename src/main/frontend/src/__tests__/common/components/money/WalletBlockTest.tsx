import { describe } from "@jest/globals";
import { fireEvent, screen, waitFor } from "@testing-library/react";
import { setupStore } from "../../../../app/store";
import WalletBlock from "../../../../common/components/money/WalletBlock/WalletBlock";
import { renderWithProviders } from "../../../../common/utils/test-utils";

describe("WalletBlock", () => {
    it("render", () => {
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
        waitFor(() => expect(screen.getByText("Wallet 1")).toBeInTheDocument());
    });

    it("render no wallets", () => {
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
        waitFor(() => expect(screen.getByText("Add new wallet")).toBeInTheDocument());
    });

    it("render no wallets, selectedWalletId exists", () => {
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
        waitFor(() => expect(screen.getByText("Add new wallet")).toBeInTheDocument());
    });

    it("select wallet", () => {
        const store = setupStore({
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
        const elProps = {
            selectedWalletId: "1",
            setSelectedWalletId: jest.fn(),
        };
        renderWithProviders(<WalletBlock {...elProps} />, { store });

        const select = screen.getByTestId("select-wallet");
        select.focus();
        fireEvent.keyDown(select, { key: "ArrowDown" });
        fireEvent.keyDown(select, { key: "Enter" });

        waitFor(() => {
            expect(elProps.setSelectedWalletId).toBeCalledTimes(1);
            expect(elProps.setSelectedWalletId).toBeCalledWith("2");
        });
    });

    it("select add wallet button -> open modal", () => {
        const store = setupStore({
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
        const elProps = {
            selectedWalletId: "1",
            setSelectedWalletId: jest.fn(),
        };
        renderWithProviders(<WalletBlock {...elProps} />, { store });

        const select = screen.getByTestId("select-wallet");
        select.focus();
        fireEvent.keyDown(select, { key: "ArrowUp" });
        fireEvent.keyDown(select, { key: "Enter" });

        waitFor(() => {
            expect(screen.getByTestId("createWalletForm")).toBeInTheDocument();
            expect(elProps.setSelectedWalletId).toBeCalledTimes(0);
        });
    });
});
