import java.util.Map;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import com.hedera.hashgraph.sdk.Client;
import io.github.cdimascio.dotenv.Dotenv;


public class clientExample {

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

    public static void main(String[] args) throws HederaException {

        // 1. Generate a Ed25519 private, public key pair

        var newKey = Ed25519PrivateKey.generate();
        var newPublicKey = newKey.getPublicKey();

        System.out.println("private key = " + newKey);
        System.out.println("public key = " + newPublicKey);

        // 2. Initialize Hedera client

        var client = hederaClient();

        // 3. Create new account on Hedera

        var initialBalance = 10000;
        var newAccountId = client.createAccount(newPublicKey, initialBalance).toString();

        System.out.println(newAccountId);

        // 4. Check new account balance 

        var accountBalance = client.getAccountBalance(AccountId.fromString(newAccountId));

        System.out.println(accountBalance);
    }
}