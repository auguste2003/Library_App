export interface User {
    id?: number;
    firstname?: string;
    lastname?: string;
    email?: string;
    role?: string;
    sub?: string;
    iat?: number;
    exp?: number;
}

export interface AuthenticationRequest {
    email?: string;
    password?: string;
}

export interface RegisterRequest {
    firstname?: string;
    lastname?: string;
    email?: string;
    password?: string;
}

export interface AuthenticationResponse {
    accessToken: string;
}
