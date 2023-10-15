import { describe } from "@jest/globals";
import {
    act,
    fireEvent,
    screen,
    waitFor,
    waitForElementToBeRemoved,
} from "@testing-library/react";
import { rest } from "msw";
import { setupServer } from "msw/node";
import React from "react";
import { type ErrorResponse, ResponseErrorType } from "../../../../app/hooks/formUtils";
import { setupStore } from "../../../../app/store";
import CreateWalletModal from "../../../../common/modal/CreateWalletModal/CreateWalletModal";
import { renderWithProviders } from "../../../../common/utils/test-utils";
import { type Wallet } from "../../../../features/wallet/walletSlice";

const walletTestHandler = [
    rest.post("api/v1/wallet", async (req, res, ctx) => {
        const { name, balance, currency } = await req.json();

        if (name === "fieldError") {
            const error: ErrorResponse = {
                message: "Name error",
                type: ResponseErrorType.FIELD_VALIDATION_ERROR,
                errorCode: "code",
                fieldName: "name",
            };
            return await res(ctx.status(400), ctx.json([error]));
        }
        const wallet: Wallet = {
            id: 1,
            name,
            balance,
            currency,
        };
        return await res(ctx.status(201), ctx.json(wallet));
    }),
];

describe("CreateWalletModal", () => {
    const server = setupServer(...walletTestHandler);

    jest.useFakeTimers();

    beforeAll(() => server.listen());
    afterEach(() => server.resetHandlers());
    afterAll(() => server.close());

    it("render", () => {
        renderWithProviders(<CreateWalletModal open={true}
                                               setOpen={jest.fn()}
                                               setNewWalletId={jest.fn()}/>);

        expect(screen.getByText("Create wallet")).toBeInTheDocument();
        expect(screen.getByText("Name")).toBeInTheDocument();
        expect(screen.getByText("Balance")).toBeInTheDocument();
        expect(screen.getByText("Currency")).toBeInTheDocument();

        expect(screen.getByText("Create")).toBeInTheDocument();

        // The Balance field must be filled with '0' by default
        expect(screen.getByDisplayValue("0") === screen.getByTestId("input-balance")).toBeTruthy();
    });

    it("fields not filled -> display required error messages", async () => {
        renderWithProviders(<CreateWalletModal open={true}
                                               setOpen={jest.fn()}
                                               setNewWalletId={jest.fn()}/>);

        // The Balance field is filled with '0' by default
        fireEvent.change(screen.getByTestId("input-balance"), { target: { value: "" } });

        act(() => screen.getByText("Create").click());

        expect(await screen.findByText("Name is required")).toBeInTheDocument();
        expect(await screen.findByText("Balance is required")).toBeInTheDocument();
        expect(await screen.findByText("Currency is required")).toBeInTheDocument();
    });

    it("filed error -> display error message", async () => {
        renderWithProviders(<CreateWalletModal open={true}
                                               setOpen={jest.fn()}
                                               setNewWalletId={jest.fn()}/>);

        fillNewWalletFrom("fieldError", 100, "AED");
        act(() => screen.getByText("Create").click());

        expect(await screen.findByText("Name error")).toBeInTheDocument();
    });

    it("create success -> save wallet and close modal", async () => {
        const elProps = {
            open: true,
            setOpen: jest.fn(),
            setNewWalletId: jest.fn(),
        };
        const store = setupStore({
            wallets: {
                wallets: [],
                isInitialized: true,
            },
        });
        const { rerenderWithProviders } = renderWithProviders(
            <CreateWalletModal {...elProps}/>, { store },
        );
        fillNewWalletFrom("test", 100, "AED");
        act(() => screen.getByText("Create").click());

        await waitFor(() => expect(screen.getByText("Creating...")).toBeInTheDocument());
        await waitForElementToBeRemoved(() => screen.getByText("Creating..."));

        await waitFor(() => {
            expect(screen.getByText("Wallet created successfully.")).toBeInTheDocument();
            expect(screen.queryByTestId("createWalletForm")).not.toBeInTheDocument();
        });
        expect(store.getState().wallets.wallets.length).toBe(1);
        expect(store.getState().wallets.wallets).toEqual([
            {
                id: 1,
                name: "test",
                balance: "100",
                currency: "AED",
            },
        ]);
        expect(elProps.setNewWalletId).toBeCalledTimes(1);
        expect(elProps.setNewWalletId).toBeCalledWith(1);
        await act(() => jest.advanceTimersByTime(1500));
        expect(elProps.setOpen).toBeCalledTimes(1);
        expect(elProps.setOpen).toBeCalledWith(false);

        elProps.open = false;
        rerenderWithProviders(<CreateWalletModal {...elProps}/>); // close modal
        await act(() => jest.advanceTimersByTime(200));
        elProps.open = true;
        rerenderWithProviders(<CreateWalletModal {...elProps}/>); // open modal

        // reset form state after modal is closed and opened again
        await waitFor(() => {
            expect(screen.queryByText("Wallet created successfully.")).not.toBeInTheDocument();
        });
    });
});

function fillNewWalletFrom(name: string, balance: number, currency: string) {
    fireEvent.change(screen.getByTestId("input-name"), { target: { value: name } });
    fireEvent.change(screen.getByTestId("input-balance"), { target: { value: balance } });

    const autocomplete = screen.getByTestId("autocomplete-currency");
    const input = autocomplete.querySelector("input") as HTMLInputElement;
    autocomplete.focus();

    fireEvent.change(input, { target: { value: currency } });
    fireEvent.keyDown(autocomplete, { key: "ArrowDown" });
    fireEvent.keyDown(autocomplete, { key: "Enter" });
}
