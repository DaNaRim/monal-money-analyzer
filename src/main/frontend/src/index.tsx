import "normalize.css";
import React from "react";
import { createRoot } from "react-dom/client";
import { Provider } from "react-redux";
import { BrowserRouter } from "react-router-dom";
import App from "./app/App";
import LanguageContextProvider from "./app/contexts/LanguageContext";
import { setupStore } from "./app/store";
import reportWebVitals from "./reportWebVitals";

const container = document.getElementById("root");

if (container === null) {
    throw new Error("Root element not found");
}
const root = createRoot(container);

root.render(
    <React.StrictMode>
        <Provider store={setupStore()}>
            <BrowserRouter basename={process.env.PUBLIC_URL}>
                <LanguageContextProvider>
                    <App/>
                </LanguageContextProvider>
            </BrowserRouter>
        </Provider>
    </React.StrictMode>,
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
