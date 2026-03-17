package com.rs.payments.wallet.service.impl;

import com.rs.payments.wallet.exception.ResourceNotFoundException;
import com.rs.payments.wallet.model.User;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.repository.UserRepository;
import com.rs.payments.wallet.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    @DisplayName("Should create wallet for existing user")
    void shouldCreateWalletForExistingUser() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // The service saves the user, which cascades to wallet. 
        // We mock save to return the user.
        when(userRepository.save(user)).thenReturn(user);

        // When
        Wallet result = walletService.createWalletForUser(userId);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals(walletService.createWalletForUser(userId).getBalance(), BigDecimal.ZERO);
        
        // Verify interactions
        verify(userRepository, times(2)).findById(userId); // Called twice due to second assert
        verify(userRepository, times(2)).save(user);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> walletService.createWalletForUser(userId));
        verify(userRepository, never()).save(any());
    }
    


    @Test
    @DisplayName("Should deposit money into wallet")
    void shouldDeposit() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(50));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);

        Wallet updatedWallet = walletService.deposit(walletId, BigDecimal.valueOf(100));

        assertEquals(BigDecimal.valueOf(150), updatedWallet.getBalance());
        verify(walletRepository).findById(walletId);
        verify(walletRepository).save(wallet);
    }

    @Test
    @DisplayName("Deposit should fail when wallet not found")
    void depositWalletNotFound() {
        UUID walletId = UUID.randomUUID();
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> walletService.deposit(walletId, BigDecimal.valueOf(50)));

        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should withdraw money from wallet")
    void shouldWithdraw() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(100));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);

        Wallet updatedWallet = walletService.withdraw(walletId, BigDecimal.valueOf(50));

        assertEquals(BigDecimal.valueOf(50), updatedWallet.getBalance());
        verify(walletRepository).findById(walletId);
        verify(walletRepository).save(wallet);
    }

    @Test
    @DisplayName("Withdraw should fail when wallet not found")
    void withdrawWalletNotFound() {
        UUID walletId = UUID.randomUUID();
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> walletService.withdraw(walletId, BigDecimal.valueOf(50)));

        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("Withdraw should fail when insufficient balance")
    void withdrawInsufficientBalance() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(30));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        assertThrows(RuntimeException.class,
                () -> walletService.withdraw(walletId, BigDecimal.valueOf(50)));

        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get wallet balance")
    void shouldGetBalance() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(200));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        BigDecimal balance = walletService.getBalance(walletId);

        assertEquals(BigDecimal.valueOf(200), balance);
        verify(walletRepository).findById(walletId);
    }

    @Test
    @DisplayName("Get balance should fail when wallet not found")
    void getBalanceWalletNotFound() {
        UUID walletId = UUID.randomUUID();
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> walletService.getBalance(walletId));

        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should transfer money between wallets")
    void shouldTransfer() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        Wallet fromWallet = new Wallet();
        fromWallet.setId(fromId);
        fromWallet.setBalance(BigDecimal.valueOf(100));

        Wallet toWallet = new Wallet();
        toWallet.setId(toId);
        toWallet.setBalance(BigDecimal.valueOf(50));

        when(walletRepository.findById(fromId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(toId)).thenReturn(Optional.of(toWallet));
        when(walletRepository.save(fromWallet)).thenReturn(fromWallet);
        when(walletRepository.save(toWallet)).thenReturn(toWallet);

        walletService.transfer(fromId, toId, BigDecimal.valueOf(75));

        assertEquals(BigDecimal.valueOf(25), fromWallet.getBalance());
        assertEquals(BigDecimal.valueOf(125), toWallet.getBalance());
        verify(walletRepository).save(fromWallet);
        verify(walletRepository).save(toWallet);
    }

    @Test
    @DisplayName("Transfer should fail when source wallet not found")
    void transferSourceWalletNotFound() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        when(walletRepository.findById(fromId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> walletService.transfer(fromId, toId, BigDecimal.valueOf(50)));

        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("Transfer should fail when destination wallet not found")
    void transferDestinationWalletNotFound() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        Wallet fromWallet = new Wallet();
        fromWallet.setId(fromId);
        fromWallet.setBalance(BigDecimal.valueOf(100));

        when(walletRepository.findById(fromId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(toId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> walletService.transfer(fromId, toId, BigDecimal.valueOf(50)));

        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("Transfer should fail when insufficient balance")
    void transferInsufficientBalance() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        Wallet fromWallet = new Wallet();
        fromWallet.setId(fromId);
        fromWallet.setBalance(BigDecimal.valueOf(30));

        Wallet toWallet = new Wallet();
        toWallet.setId(toId);
        toWallet.setBalance(BigDecimal.valueOf(50));

        when(walletRepository.findById(fromId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(toId)).thenReturn(Optional.of(toWallet));

        assertThrows(RuntimeException.class,
                () -> walletService.transfer(fromId, toId, BigDecimal.valueOf(50)));

        verify(walletRepository, never()).save(any());
    }
}
