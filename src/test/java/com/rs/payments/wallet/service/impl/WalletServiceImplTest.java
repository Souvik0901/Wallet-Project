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
    @DisplayName("Should deposit amount and update wallet balance")
    void shouldDepositAmountSuccessfully() {

        UUID walletId = UUID.randomUUID();

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(100));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);

        Wallet result = walletService.deposit(walletId, BigDecimal.valueOf(50));

        assertEquals(BigDecimal.valueOf(150), result.getBalance());

        verify(walletRepository).findById(walletId);
        verify(walletRepository).save(wallet);
    }


    @Test
    @DisplayName("Should throw exception when depositing to non-existing wallet")
    void shouldThrowExceptionWhenDepositWalletNotFound() {

        UUID walletId = UUID.randomUUID();

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> walletService.deposit(walletId, BigDecimal.valueOf(50)));

        verify(walletRepository).findById(walletId);
        verify(walletRepository, never()).save(any());
    }


    @Test
    @DisplayName("Should withdraw amount successfully")
    void shouldWithdrawSuccessfully() {

        UUID walletId = UUID.randomUUID();

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(100));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);

        Wallet result = walletService.withdraw(walletId, BigDecimal.valueOf(40));

        assertEquals(BigDecimal.valueOf(60), result.getBalance());

        verify(walletRepository).findById(walletId);
        verify(walletRepository).save(wallet);
    }


    @Test
    @DisplayName("Should throw exception when balance is insufficient")
    void shouldThrowExceptionWhenInsufficientBalance() {

        UUID walletId = UUID.randomUUID();

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(50));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        assertThrows(RuntimeException.class,
                () -> walletService.withdraw(walletId, BigDecimal.valueOf(100)));

        verify(walletRepository).findById(walletId);
        verify(walletRepository, never()).save(any());
    }


    @Test
    @DisplayName("Should return wallet balance")
    void shouldReturnWalletBalance() {

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
    @DisplayName("Should throw exception when wallet not found during balance inquiry")
    void shouldThrowExceptionWhenWalletNotFound() {

        UUID walletId = UUID.randomUUID();

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> walletService.getBalance(walletId));
    }
}
