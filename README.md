# Transaction validation failure scenario

The risk engine returns two decisions, but `TransactionValidationService` reads the missing third (sanctions) decision. This produces an `IndexOutOfBoundsException` for request `REQ-1003` and payment `PAY-482912`.

`FailedRequestRepository` catches that failed validation at the application boundary and updates the matching SQLite records to `FAILED`. The transaction carries amount `540.75 USD`. Query it with:

```sql
SELECT request_id, payment_id, amount, processing_status, validation_result
FROM processing_transactions
WHERE processing_status = 'FAILED';
```

To retrieve the amount for the simulated code failure only:

```sql
SELECT amount
FROM processing_transactions
WHERE request_id = 'REQ-1003'
  AND payment_id = 'PAY-482912'
  AND processing_status = 'FAILED';
```
