import { describe } from "@jest/globals";
import { screen } from "@testing-library/dom";
import { act, fireEvent, render } from "@testing-library/react";
import DateBlock from "../../../../../common/components/money/DateBlock/DateBlock";

describe("DateBlock", () => {
    it("render", () => {
        render(<DateBlock date={"2023-08-28"} setDate={jest.fn()}/>);

        expect(screen.getByDisplayValue("2023-08-28")).toBeInTheDocument();
        expect(screen.getByText("Monday")).toBeInTheDocument();
    });

    it("set previous date", async () => {
        const setDate = jest.fn();
        render(<DateBlock date={"2023-08-28"} setDate={setDate}/>);

        await act(() => fireEvent.click(screen.getByTestId("date-previous")));
        expect(setDate).toBeCalledWith("2023-08-27");
    });

    it("set next date", async () => {
        const setDate = jest.fn();
        render(<DateBlock date={"2023-08-28"} setDate={setDate}/>);

        await act(() => fireEvent.click(screen.getByTestId("date-next")));
        expect(setDate).toBeCalledWith("2023-08-29");
    });

    it("set date in input", async () => {
        const setDate = jest.fn();
        render(<DateBlock date={"2023-08-28"} setDate={setDate}/>);

        act(() => {
            const input = screen.getByDisplayValue("2023-08-28");
            input.focus();
            fireEvent.change(input, { target: { value: "2023-07-28" } });
            input.blur(); // to trigger onChange
        });
        expect(setDate).toBeCalledWith("2023-07-28");
    });

    it("set date in input. Blur on Enter", async () => {
        const setDate = jest.fn();
        render(<DateBlock date={"2023-08-28"} setDate={setDate}/>);

        act(() => {
            const input = screen.getByDisplayValue("2023-08-28");
            input.focus();
            fireEvent.change(input, { target: { value: "2023-07-28" } });
            fireEvent.keyDown(input, { key: "Enter", code: "Enter" });
        });
        expect(setDate).toBeCalledWith("2023-07-28");
    });

    it("set invalid date in input", async () => {
        const setDate = jest.fn();
        render(<DateBlock date={"2023-08-28"} setDate={setDate}/>);

        act(() => {
            const input = screen.getByDisplayValue("2023-08-28");
            input.focus();
            fireEvent.change(input, { target: { value: "2023-07-32" } });
            input.blur(); // to trigger onChange
        });
        expect(screen.getByText("Please select a valid date to display transactions"))
            .toBeInTheDocument();
        expect(setDate).not.toBeCalled();
    });

    it("set invalid date in input. Correct date", async () => {
        const setDate = jest.fn();
        render(<DateBlock date={"2023-08-28"} setDate={setDate}/>);

        act(() => {
            const input = screen.getByDisplayValue("2023-08-28");
            input.focus();
            fireEvent.change(input, { target: { value: "2023-07-32" } });
            input.blur(); // to trigger onChange
        });
        expect(screen.getByText("Please select a valid date to display transactions"))
            .toBeInTheDocument();

        act(() => {
            const input = screen.getByDisplayValue("");
            input.focus();
            fireEvent.change(input, { target: { value: "2023-07-28" } });
            input.blur(); // to trigger onChange
        });
        expect(setDate).toBeCalledWith("2023-07-28");
        expect(screen.queryByText("Please select a valid date to display transactions"))
            .not.toBeInTheDocument();
    });
});
