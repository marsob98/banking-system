package com.bank.banking_system.account.soap;
import com.bank.banking_system.account.dto.VerificationResult;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

@WebService
public interface PayeeVerificationService {

    @WebMethod
    @WebResult(name = "result")
    VerificationResult verifyRecipient(
            @WebParam(name = "accountNumber") String accountNumber,
            @WebParam(name = "firstName") String firstName,
            @WebParam(name = "lastName") String lastName
    );
}
