import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { useAppDispatch, useAppSelector } from "../../../../app/hooks/reduxHooks";
import { ROUTE_FORBIDDEN, ROUTE_LOGIN } from "../../../../app/routes";
import {
    addAppMessage,
    type AppMessage,
    AppMessageCode,
    AppMessageType,
} from "../../../../features/appMessages/appMessagesSlice";
import {
    Role,
    selectAuthIsInitialized,
    selectAuthRoles,
    setForceLogin,
} from "../../../../features/auth/authSlice";
import Loading from "../../base/Loading/Loading";

interface ProtectedRouteProps {
    children: React.ReactNode;
    role?: Role;
}

const ProtectedRoute = ({ role = Role.ROLE_USER, children }: ProtectedRouteProps) => {
    const navigate = useNavigate();
    const dispatch = useAppDispatch();

    const isAuthInit = useAppSelector<boolean>(selectAuthIsInitialized);

    const roles = useAppSelector<Role[]>(selectAuthRoles);

    const [preventRender, setPreventRender] = useState<boolean>(true);

    useEffect(() => {
        if (!isAuthInit) {
            return;
        }
        if (roles.length < 1) {
            const appMessage: AppMessage = {
                type: AppMessageType.WARNING,
                messageCode: AppMessageCode.AUTH_NEEDED,
                page: "login",
            };
            dispatch(addAppMessage(appMessage));
            dispatch(setForceLogin(true));
            navigate(ROUTE_LOGIN);
            return;
        }
        if (!roles.includes(role)) {
            navigate(ROUTE_FORBIDDEN);
        }
        setPreventRender(false);
    }, [dispatch, navigate, role, roles, isAuthInit]);

    return (
        <>
            {isAuthInit && !preventRender ? children : <Loading/>}
        </>
    );
};

export default ProtectedRoute;
