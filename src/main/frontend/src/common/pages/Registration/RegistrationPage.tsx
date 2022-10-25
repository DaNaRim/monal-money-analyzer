import React from "react";
import {useForm} from "react-hook-form";
import axios from "axios";

type RegistrationDto = {
    firstName: string,
    lastName: string,
    email: string,
    password: string,
    matchingPassword: string
}

interface FormValues extends RegistrationDto {
    globalError?: string;
    serverValidation?: string;
}

type ErrorField =
    "firstName"
    | "lastName"
    | "email"
    | "password"
    | "matchingPassword"
    | "globalError"
    | "serverValidation";

type GenericError = {
    type: string,
    fieldName: ErrorField,
    message: string
}

const RegistrationPage = () => {
    const {register, handleSubmit, setError, formState: {errors}} = useForm<FormValues>();

    const onSubmit = async (data: RegistrationDto) => {
        await axios.post("api/v1/registration", data)
            .then(res => console.log(res.data))
            .catch(reason => {
                const data: GenericError[] = reason.response.data;
                console.log(data);

                for (const error of data) {
                    if (error.fieldName === "globalError" && error.type === "PasswordMatches") {
                        setError("matchingPassword", {type: error.type, message: error.message});
                        continue;
                    }
                    setError(error.fieldName, {type: error.type, message: error.message});
                }
            });
    };

    return (
        <div>
            <h1>Registration Page</h1>
            <form onSubmit={handleSubmit(onSubmit)}>
                <label htmlFor="firstName">FirstName: </label>
                <input type="text" {...register("firstName", {required: true})}/><br/>
                {errors.firstName && <span>{errors.firstName.message}</span>}<br/>

                <label htmlFor="lastName">LastName: </label>
                <input type="text" {...register("lastName", {required: true})}/><br/>
                {errors.lastName && <span>{errors.lastName.message}</span>}<br/>

                <label htmlFor="email">Email: </label>
                <input type="email" {...register("email", {required: true})}/><br/>
                {errors.email && <span>{errors.email.message}</span>}<br/>

                <label htmlFor="password">Password: </label>
                <input type="password" {...register("password", {required: true})}/><br/>
                {errors.password && <span>{errors.password.message}</span>}<br/>

                <label htmlFor="matchingPassword">MatchingPassword: </label>
                <input type="password" {...register("matchingPassword", {required: true})}/><br/>
                {errors.matchingPassword && <span>{errors.matchingPassword.message}</span>}<br/>

                <input type="hidden" {...register("globalError")}/>
                {errors.globalError
                    && <span style={{fontSize: "2rem", color: "orange"}}>{errors.globalError.message}</span>
                }<br/>

                <input type="hidden" {...register("serverValidation")}/>
                {errors.serverValidation
                    && <span style={{fontSize: "2rem", color: "red"}}>{errors.serverValidation.message}</span>
                }<br/>

                <button type="submit">Submit</button>
            </form>
        </div>
    );
};

export default RegistrationPage;
