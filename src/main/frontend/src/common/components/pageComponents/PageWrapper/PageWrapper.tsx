import React from "react";
import Header from "../Header/Header";

const PageWrapper = (props: any) => {
    return (
        <div id="page_wrapper">
            <Header/>
            {props.children}
        </div>
    );
};

export default PageWrapper;
