package com.main.mini_bank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Map;

import com.main.mini_bank.model.entity.Account;
import com.main.mini_bank.repository.AccountRepository;
import com.main.mini_bank.service.CustomUserDetailsService;
import com.main.mini_bank.service.JwtService;
import com.main.mini_bank.repository.TransactionRepository;
import com.main.mini_bank.model.entity.User;
import com.main.mini_bank.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
class TransactionAndAccountTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void cleanDb() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void transferSuccessUpdatesBalances() throws Exception {
        User fromUser = createUser("fromUser", "from@example.com");
        User toUser = createUser("toUser", "to@example.com");

        Account fromAccount = createAccount(fromUser, "ACC-100", "Primary", new BigDecimal("100.00"));
        Account toAccount = createAccount(toUser, "ACC-200", "Savings", new BigDecimal("25.00"));

        String token = createToken(fromUser.getUsername());

        Map<String, Object> payload = Map.of(
            "fromAccountNumber", fromAccount.getNumber(),
            "toAccountNumber", toAccount.getNumber(),
            "amount", new BigDecimal("40.00")
        );

        mockMvc.perform(post("/api/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"));

        Account updatedFrom = accountRepository.findByNumber("ACC-100").orElseThrow();
        Account updatedTo = accountRepository.findByNumber("ACC-200").orElseThrow();

        assertThat(updatedFrom.getBalance()).isEqualByComparingTo("60.00");
        assertThat(updatedTo.getBalance()).isEqualByComparingTo("65.00");
    }

    @Test
    void transferInsufficientFundsReturnsFailedStatus() throws Exception {
        User fromUser = createUser("fromUser", "from@example.com");
        User toUser = createUser("toUser", "to@example.com");

        Account fromAccount = createAccount(fromUser, "ACC-300", "Primary", new BigDecimal("10.00"));
        Account toAccount = createAccount(toUser, "ACC-400", "Savings", new BigDecimal("5.00"));

        String token = createToken(fromUser.getUsername());

        Map<String, Object> payload = Map.of(
            "fromAccountNumber", fromAccount.getNumber(),
            "toAccountNumber", toAccount.getNumber(),
            "amount", new BigDecimal("25.00")
        );

        mockMvc.perform(post("/api/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("FAILED"))
            .andExpect(jsonPath("$.message").value("Insufficient funds"));

        Account updatedFrom = accountRepository.findByNumber("ACC-300").orElseThrow();
        Account updatedTo = accountRepository.findByNumber("ACC-400").orElseThrow();

        assertThat(updatedFrom.getBalance()).isEqualByComparingTo("10.00");
        assertThat(updatedTo.getBalance()).isEqualByComparingTo("5.00");
    }

    @Test
    void deleteAccountWithBalanceFails() throws Exception {
        User user = createUser("deleteUser", "delete@example.com");
        Account account = createAccount(user, "ACC-500", "Delete Me", new BigDecimal("1.00"));
        String token = createToken(user.getUsername());

        mockMvc.perform(delete("/api/accounts/{id}", account.getId())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Account balance must be zero to delete"));
    }

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Password123"));
        return userRepository.save(user);
    }

    private String createToken(String username) {
        return jwtService.generateToken(userDetailsService.loadUserByUsername(username));
    }

    private Account createAccount(User user, String number, String name, BigDecimal balance) {
        Account account = new Account();
        account.setNumber(number);
        account.setName(name);
        account.setBalance(balance);
        account.setUser(user);
        return accountRepository.save(account);
    }
}
