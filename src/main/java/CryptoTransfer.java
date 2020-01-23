import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;

import java.math.BigInteger;

public final class CryptoTransfer {
    private CryptoTransfer() { }

    public static void main(String[] args) throws HederaStatusException {
        var operatorId = ExampleHelper.getOperatorId();
        var client = ExampleHelper.createHederaClient();

        var recipientId = AccountId.fromString("0.0.3620");
        var amount = new BigInteger("5000000");

        var senderBalanceBefore = new AccountBalanceQuery()
                .setAccountId(operatorId)
                .execute(client);
        var receiptBalanceBefore = new AccountBalanceQuery()
                .setAccountId(recipientId)
                .execute(client);

        System.out.println("" + operatorId + " balance = " + senderBalanceBefore);
        System.out.println("" + recipientId + " balance = " + receiptBalanceBefore);

        TransactionId transactionId = new CryptoTransferTransaction()
                // .addSender and .addRecipient can be called as many times as you want as long as the total sum from
                // both sides is equivalent
                .addSender(operatorId, amount.longValue())
                .addRecipient(recipientId, amount.longValue())
                .setTransactionMemo("transfer test")
                // As we are sending from the operator we do not need to explicitly sign the transaction
                .execute(client);

        TransactionReceipt receipt = transactionId.getReceipt(client);
        System.out.println("transferred " + amount.longValue() + "...");

        var senderBalanceAfter = new AccountBalanceQuery()
                .setAccountId(operatorId)
                .execute(client);
        var receiptBalanceAfter = new AccountBalanceQuery()
                .setAccountId(recipientId)
                .execute(client);

        System.out.println("" + operatorId + " balance = " + senderBalanceAfter);
        System.out.println("" + recipientId + " balance = " + receiptBalanceAfter);
        System.out.println("Transfer memo: " + receipt.getAccountId());
    }
}
