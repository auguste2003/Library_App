describe('Admin Setup Flow', () => {
    it('should redirect to /setup when no admin exists', () => {
        cy.visit('/');
        // Depending on timing, it might go to /login then /setup or directly /setup
        // Our guard redirects to /setup
        cy.url().should('include', '/setup');
        cy.contains('System Setup');
    });

    it('should create an admin user', () => {
        cy.visit('/setup');

        // Fill form
        cy.get('#firstname').type('Cypress');
        cy.get('#lastname').type('Admin');
        cy.get('#email').type('cypress_admin@test.com');
        cy.get('#password').type('Pass123!@#'); // Meets complexity requirements

        // Submit
        cy.get('button[type="submit"]').click();

        // Should redirect to login
        cy.url().should('include', '/login');
        cy.contains('Sign In');
    });

    it('should allow login with new admin', () => {
        cy.visit('/login');
        cy.get('#email').type('cypress_admin@test.com');
        cy.get('#password').type('Pass123!@#');
        cy.get('button[type="submit"]').click();

        // Should redirect to books
        cy.url().should('include', '/books');
        cy.contains('Library');
    });
});
