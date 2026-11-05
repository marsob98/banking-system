package com.bank.banking_system.account.soap;

import com.bank.banking_system.account.AccountService;
import com.bank.banking_system.account.dto.RecipientVerificationRequest;
import com.bank.banking_system.account.dto.VerificationResult;
import org.apache.cxf.binding.soap.SoapFault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PayeeVerificationEndpointTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private PayeeVerificationEndpoint payeeVerificationEndpoint;



    @Test
    void verifyRecipient_shouldDelegateToServiceAndReturnItsResult() {
        RecipientVerificationRequest request = new RecipientVerificationRequest
                ("PL123", "Jan", "Kowalski");
        when(accountService.verifyRecipient(request)).thenReturn(VerificationResult.MATCH);

        VerificationResult result = payeeVerificationEndpoint.verifyRecipient
                ("PL123", "Jan", "Kowalski");
        assertThat(result).isEqualTo(VerificationResult.MATCH);
    }


    @ParameterizedTest
    @CsvSource({
            ", Jan, Kowalski, accountNumber",
            "PL123, '', Kowalski, firstName",
            "PL123, Jan, '   ', lastName"
    })
    void verifyRecipient_shouldThrowClientFault_whenFieldIsBlank(
            String accountNumber, String firstName, String lastName, String blankField ) {
        assertThatThrownBy(() -> payeeVerificationEndpoint.verifyRecipient(accountNumber, firstName, lastName))
                .isInstanceOf(SoapFault.class)
                .hasMessageContaining(blankField);

        verifyNoInteractions(accountService);
    }



}
