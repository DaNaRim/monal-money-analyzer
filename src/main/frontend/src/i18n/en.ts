export default {
    mainHeader: {
        loading: "Loading...",
        login: "Login",
        logout: "Logout",
        register: "Register",
        nav: {
            home: "Home",
        },
    },

    mainFooter: {
        desc: "This is a footer",
    },

    mainLoader: "Loading...",

    form: {
        required: "Required",
        password_show: "Show password",
    },

    appMessages: {

        // server

        unresolved: "Unresolved message. Please contact the administrator.",

        registration_confirmation_success: "Account activated successfully. You can now log in.",

        validation_token_wrong_type: "Wrong token type.",
        validation_token_not_found: "Token not found. Please try again.",
        validation_token_used: "Token already used.",
        validation_token_expired: "Token expired.",

        validation_token_verification_not_found: "Verification token not found. Please try again.",
        validation_token_verification_expired: "Verification token expired.",
        validation_token_verification_user_enabled: "Account already activated."
            + " You can now log in.",

        // frontend

        password_reset_success: "Password updated successfully. You can now log in.",
        auth_expired: "Session expired. Please log in again.",
    },

    fetchErrors: {
        fetchError: "Server unavailable. please try again later",
        serverError: "Server error. Please try again later. If the problem persists,"
            + " please contact the administrator",
        unknownError: "Unknown error. Please try again later. If the problem persists,"
            + " please contact the administrator",
    },

    errorPages: {
        main: {
            header: "Error",
            desc: "Something went wrong. Please try again later. If the problem persists,"
                + " please contact the administrator.",
            link: "Go to home page",
        },
        forbidden: {
            header: "Forbidden",
            desc: "You don't have permission to access this page.",
            link: "Go to home page",
        },
        notFound: {
            header: "Not found",
            desc: "The page you are looking for does not exist.",
            link: "Go to home page",
        },
    },

    homePage: {
        title: "Home page",
    },

    loginPage: {
        title: "Login",
        resendVerificationEmail: "Resend verification email",
        form: {
            fields: {
                username: "Email",
                password: "Password",
            },
            errors: {
                username: {
                    required: "Email is required",
                },
                password: {
                    required: "Password is required",
                },
            },
            submit: "Login",
            forgotPassword: "Forgot password?",
            loading: "Logging in...",
        },
    },

    registerPage: {
        title: "Register",
        success: "Registration successful."
            + " Please check your email to activate your account."
            + " If it doesn't appear within a few minutes, check your spam folder.",
        form: {
            fields: {
                firstName: "First name",
                lastName: "Last name",
                email: "Email",
                password: "Password",
                matchingPassword: "Confirm password",
            },
            errors: {
                firstName: {
                    required: "First name is required",
                },
                lastName: {
                    required: "Last name is required",
                },
                email: {
                    required: "Email is required",
                },
                password: {
                    required: "Password is required",
                },
                matchingPassword: {
                    required: "Confirm password is required",
                },
            },
            submit: "Register",
            loading: "Registering...",
        },
    },

    resendVerificationEmailPage: {
        title: "Resend verification email",
        success: "Verification email sent."
            + " Please check your email to activate your account."
            + " If it doesn't appear within a few minutes, check your spam folder.",
        form: {
            fields: {
                email: "Email",
            },
            errors: {
                email: {
                    required: "Email is required",
                },
            },
            submit: "Send",
            loading: "Processing...",
        },
    },

    resetPasswordPage: {
        title: "Reset password",
        success: "Check your email for a link to reset your password."
            + " If it doesn't appear within a few minutes, heck your spam folder.",
        form: {
            fields: {
                email: "Email",
            },
            errors: {
                email: {
                    required: "Email is required",
                },
            },
            submit: "Send",
            loading: "Processing...",
        },
    },

    resetPasswordSetPage: {
        title: "Set new password",
        form: {
            fields: {
                newPassword: "New password",
                matchingPassword: "Confirm password",
            },
            errors: {
                newPassword: {
                    required: "Password is required",
                },
                matchingPassword: {
                    required: "Confirm password is required",
                },
            },
            submit: "Set new password",
            loading: "Processing...",
        },
    },
};
