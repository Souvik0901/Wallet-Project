package com.rs.payments.wallet.controller;

import java.util.UUID;
import com.rs.payments.wallet.dto.CreateWalletRequest;
import com.rs.payments.wallet.dto.DepositRequest;
import com.rs.payments.wallet.dto.TransferRequest;
import com.rs.payments.wallet.dto.WithdrawRequest;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.service.WalletService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletController walletController;

    @Test
    @DisplayName("Should create wallet")
    void shouldCreateWallet() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(userId);

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.ZERO);

        when(walletService.createWalletForUser(userId)).thenReturn(wallet);

        // When
        ResponseEntity<Wallet> response = walletController.createWallet(request);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(wallet, response.getBody());
        verify(walletService, times(1)).createWalletForUser(userId);
    }


    @Test
    @DisplayName("Should deposit amount into wallet")
    void shouldDeposit() {
        UUID walletId = UUID.randomUUID();
        DepositRequest request = new DepositRequest();
        request.setAmount(BigDecimal.valueOf(100));

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(100));

        when(walletService.deposit(walletId, request.getAmount())).thenReturn(wallet);

        ResponseEntity<Wallet> response = walletController.deposit(walletId, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(wallet, response.getBody());
        verify(walletService, times(1)).deposit(walletId, request.getAmount());
    }

    @Test
    @DisplayName("Should withdraw amount from wallet")
    void shouldWithdraw() {
        UUID walletId = UUID.randomUUID();
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(BigDecimal.valueOf(50));

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(50));

        when(walletService.withdraw(walletId, request.getAmount())).thenReturn(wallet);

        ResponseEntity<Wallet> response = walletController.withdraw(walletId, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(wallet, response.getBody());
        verify(walletService, times(1)).withdraw(walletId, request.getAmount());
    }

    @Test
    @DisplayName("Should get wallet balance")
    void shouldGetBalance() {
        UUID walletId = UUID.randomUUID();
        BigDecimal balance = BigDecimal.valueOf(200);

        when(walletService.getBalance(walletId)).thenReturn(balance);

        ResponseEntity<BigDecimal> response = walletController.getBalance(walletId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(balance, response.getBody());
        verify(walletService, times(1)).getBalance(walletId);
    }

    @Test
    @DisplayName("Should transfer amount between wallets")
    void shouldTransfer() {
        UUID fromWalletId = UUID.randomUUID();
        UUID toWalletId = UUID.randomUUID();
        TransferRequest request = new TransferRequest();
        request.setFromWalletId(fromWalletId);
        request.setToWalletId(toWalletId);
        request.setAmount(BigDecimal.valueOf(75));

        // Service doesn't return anything for transfer
        doNothing().when(walletService).transfer(fromWalletId, toWalletId, request.getAmount());

        ResponseEntity<String> response = walletController.transfer(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Transfer successful", response.getBody());
        verify(walletService, times(1)).transfer(fromWalletId, toWalletId, request.getAmount());
    }
}
