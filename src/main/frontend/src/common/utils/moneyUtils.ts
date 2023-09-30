export const LOCAL_STORAGE_SELECTED_WALLET_ID = "selectedWalletId";

export function addSpacesToNumber(number: number | string): string {
    // Add space between groups of three digits
    const numberParts = Number(number).toFixed(8).replace(/[.]?0+$/, "").split(".");
    const integerGroups = numberParts[0].match(/(\d+?)(?=(\d{3})+(?!\d)|$)/g) ?? [];

    const sign = number < 0 ? "-" : "";
    // Add space between groups of three digits
    return `${sign}${integerGroups?.join(" ")}`
        + `${(numberParts[1] == null ? "" : "." + numberParts[1])}`;
}
