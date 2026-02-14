package com.library.library.bootstrap;

import com.library.library.entity.Book;
import com.library.library.repository.BookRepository;
import com.library.library.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    @Override
    public void run(String... args) throws Exception {
        if (bookRepository.count() == 0) {
            List<Book> books = List.of(
                    Book.builder().title("Rich Dad Poor Dad").author("Robert T. Kiyosaki").isbn("978-1612680194")
                            .available(true).build(),
                    Book.builder().title("Think and Grow Rich").author("Napoleon Hill").isbn("978-1585424337")
                            .available(true).build(),
                    Book.builder().title("The Psychology of Money").author("Morgan Housel").isbn("978-0857197689")
                            .available(true).build(),
                    Book.builder().title("Atomic Habits").author("James Clear").isbn("978-0735211292").available(true)
                            .build(),
                    Book.builder().title("The 4-Hour Workweek").author("Timothy Ferriss").isbn("978-0307465351")
                            .available(true).build(),
                    Book.builder().title("Zero to One").author("Peter Thiel").isbn("978-0804139298").available(true)
                            .build(),
                    Book.builder().title("The Millionaire Fastlane").author("MJ DeMarco").isbn("978-0984358106")
                            .available(true).build(),
                    Book.builder().title("Your Money or Your Life").author("Vicki Robin").isbn("978-0143115762")
                            .available(true).build(),
                    Book.builder().title("The Intelligent Investor").author("Benjamin Graham").isbn("978-0060555665")
                            .available(true).build(),
                    Book.builder().title("Principles: Life and Work").author("Ray Dalio").isbn("978-1501124020")
                            .available(true).build(),
                    Book.builder().title("The Richest Man in Babylon").author("George S. Clason").isbn("978-0451205360")
                            .available(true).build(),
                    Book.builder().title("Secrets of the Millionaire Mind").author("T. Harv Eker")
                            .isbn("978-0060763282").available(true).build(),
                    Book.builder().title("I Will Teach You to Be Rich").author("Ramit Sethi").isbn("978-0761147480")
                            .available(true).build(),
                    Book.builder().title("The Compound Effect").author("Darren Hardy").isbn("978-1593157241")
                            .available(true).build(),
                    Book.builder().title("Money: Master the Game").author("Tony Robbins").isbn("978-1476757803")
                            .available(true).build(),
                    Book.builder().title("Unshakeable").author("Tony Robbins").isbn("978-1501164583").available(true)
                            .build(),
                    Book.builder().title("The Simple Path to Wealth").author("J.L. Collins").isbn("978-1533667922")
                            .available(true).build(),
                    Book.builder().title("Financial Freedom").author("Grant Sabatier").isbn("978-0525534593")
                            .available(true).build(),
                    Book.builder().title("The Total Money Makeover").author("Dave Ramsey").isbn("978-1595555274")
                            .available(true).build(),
                    Book.builder().title("Quit Like a Millionaire").author("Kristy Shen").isbn("978-0525538690")
                            .available(true).build());

            // Save books first
            List<Book> savedBooks = bookRepository.saveAll(books);
            System.out.println("Financial motivation books initialized: " + savedBooks.size());

            // Seed Loans
            seedLoans(savedBooks);
        }
    }

    private void seedLoans(List<Book> books) {
        if (books.isEmpty())
            return;

        List<String> users = List.of(
                "alice@example.com", "bob@example.com", "charlie@example.com",
                "david@example.com", "eve@example.com");

        java.util.List<com.library.library.entity.Loan> loans = new java.util.ArrayList<>();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.util.Random random = new java.util.Random();

        for (String user : users) {
            // Each user gets 1-3 loans
            int numLoans = random.nextInt(3) + 1;
            for (int i = 0; i < numLoans; i++) {
                Book book = books.get(random.nextInt(books.size()));

                boolean isReturned = random.nextBoolean();
                java.time.LocalDateTime borrowDate = now.minusDays(random.nextInt(30) + 1);

                com.library.library.entity.Loan loan = com.library.library.entity.Loan.builder()
                        .book(book)
                        .userEmail(user)
                        .borrowDate(borrowDate)
                        .returned(isReturned)
                        .returnDate(isReturned ? borrowDate.plusDays(random.nextInt(10) + 1) : null)
                        .build();

                // If active loan, mark book as unavailable
                if (!isReturned) {
                    book.setAvailable(false);
                    bookRepository.save(book);
                }

                loans.add(loan);
            }
        }
        loanRepository.saveAll(loans);
        System.out.println("Test loans initialized: " + loans.size());
    }
}
