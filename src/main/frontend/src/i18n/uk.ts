export default {
    mainHeader: {
        loading: "Завантаження...",
        login: "Увійти",
        logout: "Вийти",
        register: "Зареєструватися",
        nav: {
            home: "Домашня",
        },
    },

    mainFooter: {
        desc: "Це футер",
    },

    mainLoader: "Завантаження...",

    form: {
        required: "Обов'язкове",
        password_show: "Показати пароль",
    },

    fetchErrors: {
        fetchError: "Сервер недоступний. Будь ласка, спробуйте пізніше",
        serverError: "Серверна помилка. Будь ласка, спробуйте пізніше."
            + " Якщо проблема не зникне, зверніться до адміністратора",
        unknownError: "Невідома помилка. Будь ласка, спробуйте пізніше."
            + " Якщо проблема не зникне, зверніться до адміністратора",
    },

    // Error pages

    errorPage: {
        header: "Помилка",
        desc: "Щось пішло не так. Будь ласка, спробуйте пізніше. Якщо проблема не зникне,"
            + " зверніться до адміністратора.",
        link: "Перейти на домашню сторінку",
    },

    forbiddenPage: {
        header: "Заборонено",
        desc: "У вас немає дозволу на доступ до цієї сторінки.",
        link: "Перейти на домашню сторінку",
    },

    notFoundPage: {
        header: "Не знайдено",
        desc: "Сторінка, яку ви шукаєте, не існує.",
        link: "Перейти на домашню сторінку",
    },

    // Pages

    homePage: {
        title: "Домашня сторінка",
    },

    loginPage: {
        title: "Вхід",
        resendVerificationEmail: "Повторно надіслати листа з підтвердженням",
        appMessages: {
            account_confirmation_success: "Аккаунт успішно активовано. Тепер ви можете увійти.",

            validation_token_wrong_type: "Невірний тип токена.",
            validation_token_not_found: "Токен не знайдено. Будь ласка, спробуйте ще раз.",
            validation_token_used: "Токен вже був використаний.",
            validation_token_expired: "Час дії токену минув",

            validation_token_verification_not_found: "Токен підтвердження не знайдено."
                + " Будь ласка, спробуйте ще раз.",
            validation_token_verification_expired: "Токен підтвердження застарів.",
            validation_token_verification_user_enabled: "Аккаунт вже активовано."
                + " Ви можете увійти.",

            password_reset_success: "Пароль успішно оновлено. Тепер ви можете увійти.",
            auth_expired: "Термін дії сесії закінчився. Будь ласка, увійдіть знову.",
        },
        form: {
            fields: {
                username: "Електронна пошта",
                password: "Пароль",
            },
            errors: {
                username: {
                    required: "Електронна пошта обов'язкова",
                },
                password: {
                    required: "Пароль обов'язковий",
                },
            },
            submit: "Увійти",
            forgotPassword: "Забули пароль?",
            loading: "Вхід...",
        },
    },

    registerPage: {
        title: "Реєстрація",
        appMessages: {
            registration_success: "Реєстрація успішна."
                + " Будь ласка, перевірте свою електронну пошту для підтвердження."
                + " Якщо ви не отримали листа, перевірте папку 'Спам'",
        },
        form: {
            fields: {
                firstName: "Ім'я",
                lastName: "Прізвище",
                email: "Електронна пошта",
                password: "Пароль",
                matchingPassword: "Підтвердження пароля",
            },
            errors: {
                firstName: {
                    required: "Ім'я обов'язкове",
                },
                lastName: {
                    required: "Прізвище обов'язкове",
                },
                email: {
                    required: "Електронна пошта обов'язкова",
                },
                password: {
                    required: "Пароль обов'язковий",
                },
                matchingPassword: {
                    required: "Підтвердження пароля обов'язкове",
                },
            },
            submit: "Зареєструватися",
            loading: "Реєстрація...",
        },
    },

    resendVerificationEmailPage: {
        title: "Повторно надіслати листа з підтвердженням",
        appMessages: {
            email_resend_success: "Листа з підтвердженням успішно надіслано."
                + " Будь ласка, перевірте свою електронну пошту."
                + " Якщо ви не отримали листа, перевірте папку 'Спам'",
        },
        form: {
            fields: {
                email: "Електронна пошта",
            },
            errors: {
                email: {
                    required: "Електронна пошта обов'язкова",
                },
            },
            submit: "Надіслати",
            loading: "Надсилання...",
        },
    },

    resetPasswordPage: {
        title: "Скидання пароля",
        appMessages: {
            reset_password_success: "Вам надіслано листа з посиланням для скидання пароля."
                + " Якщо лист не надійшов через декілька хвилин, перевірте папку 'Спам'",
        },
        form: {
            fields: {
                email: "Електронна пошта",
            },
            errors: {
                email: {
                    required: "Електронна пошта обов'язкова",
                },
            },
            submit: "Надіслати",
            loading: "Надсилання...",
        },
    },

    resetPasswordSetPage: {
        title: "Встановлення нового пароля",
        form: {
            fields: {
                newPassword: "Новий пароль",
                matchingPassword: "Підтвердження пароля",
            },
            errors: {
                newPassword: {
                    required: "Пароль обов'язковий",
                },
                matchingPassword: {
                    required: "Підтвердження пароля обов'язкове",
                },
            },
            submit: "Змінити пароль",
            loading: "Зміна пароля...",
        },
    },
};
