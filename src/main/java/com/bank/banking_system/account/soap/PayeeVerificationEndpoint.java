package com.bank.banking_system.account.soap;

import com.bank.banking_system.account.AccountService;
import com.bank.banking_system.account.dto.RecipientVerificationRequest;
import com.bank.banking_system.account.dto.VerificationResult;
import jakarta.jws.WebService;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.interceptor.Fault;
import org.springframework.stereotype.Component;

@Component
@WebService(
        endpointInterface = "com.bank.banking_system.account.soap.PayeeVerificationService",
        serviceName = "PayeeVerificationService"
)
public class PayeeVerificationEndpoint implements PayeeVerificationService {

    private final AccountService accountService;

    public PayeeVerificationEndpoint(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public VerificationResult verifyRecipient(String accountNumber, String firstName, String lastName) {
        requireText(accountNumber, "accountNumber");
        requireText(firstName, "firstName");
        requireText(lastName, "lastName");
        RecipientVerificationRequest request = new RecipientVerificationRequest(accountNumber, firstName, lastName);
        return accountService.verifyRecipient(request);
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new SoapFault(fieldName + " must not be blank", Fault.FAULT_CODE_CLIENT);
        }
    }
}
