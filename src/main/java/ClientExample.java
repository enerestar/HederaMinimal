import java.util.Map;

import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.account.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import com.hedera.hashgraph.sdk.Client;
import io.github.cdimascio.dotenv.Dotenv;


public class ClientExample {

    public static Client hederaClient() {

        // Grab configuration variables from the .env file

        var operatorId = AccountId.fromString(Dotenv.load().get("OPERATOR_ID"));
        var operatorKey = Ed25519PrivateKey.fromString(Dotenv.load().get("OPERATOR_KEY"));
        var nodeId = AccountId.fromString(Dotenv.load().get("NODE_ID"));
        var nodeAddress = Dotenv.load().get("NODE_ADDRESS");

        // Build client

        var hederaClient = new Client(Map.of(nodeId, nodeAddress));

        // Set the the account ID and private key of the operator 

        hederaClient.setOperator(operatorId, operatorKey);

        return hederaClient;
    }

    public static void main(String[] args) throws HederaStatusException {

        // 1. Generate a Ed25519 private, public key pair

        var newKey = Ed25519PrivateKey.generate();
        var newPublicKey = newKey.publicKey;

//        var newKey = Ed25519PrivateKey.fromString(Dotenv.load().get("KEYGEN_MOBILE_PRIVATE_KEY"));
//        var newPublicKey = Ed25519PublicKey.fromString(Dotenv.load().get("KEYGEN_MOBILE_PUBLIC_KEY"));

        System.out.println("private key = " + newKey);
        System.out.println("public key = " + newPublicKey);

        // 2. Initialize Hedera client

        var client = hederaClient().setMaxTransactionFee(100000000);

        // 3. Create new account on Hedera

        // In TINYBARS :D
        var initialBalance = 500000;

        TransactionId transactionId = new AccountCreateTransaction()
                .setInitialBalance(initialBalance)
                .setKey(newPublicKey)
                .execute(client);

        TransactionReceipt receipt = transactionId.getReceipt(client);
        AccountId newAccountId = receipt.getAccountId();
        System.out.println(newAccountId);

        // 4. Check new account balance 

        var hbar = new AccountBalanceQuery()
                .setAccountId(newAccountId)
                .execute(client);
        System.out.println(hbar);
    }
}