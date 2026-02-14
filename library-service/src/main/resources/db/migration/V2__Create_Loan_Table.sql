CREATE TABLE loan (
    id SERIAL PRIMARY KEY,
    book_id INTEGER NOT NULL REFERENCES book(id),
    user_email VARCHAR(255) NOT NULL,
    borrow_date TIMESTAMP NOT NULL,
    return_date TIMESTAMP,
    returned BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_loan_user_email ON loan(user_email);
CREATE INDEX idx_loan_book_id ON loan(book_id);
