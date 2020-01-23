import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicCreateTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.mirror.MirrorClient;
import com.hedera.hashgraph.sdk.mirror.MirrorConsensusTopicQuery;
import io.github.cdimascio.dotenv.Dotenv;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;

public class HederaConsensus {
    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId NODE_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("NODE_ID")));
    private static final String NODE_ADDRESS = Objects.requireNonNull(Dotenv.load().get("NODE_ADDRESS"));

    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private static final String MIRROR_NODE_ADDRESS = Objects.requireNonNull(Dotenv.load().get("MIRROR_NODE_ADDRESS"));

    private HederaConsensus() {
    }

    public static void main(String[] args) throws InterruptedException, HederaStatusException {
        final MirrorClient mirrorClient = new MirrorClient(MIRROR_NODE_ADDRESS);

        // To improve responsiveness, you should specify multiple nodes
        Client client = new Client(new HashMap<AccountId, String>() {
            {
                put(NODE_ID, NODE_ADDRESS);
            }
        });

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        final TransactionId transactionId = new ConsensusTopicCreateTransaction()
                .setMaxTransactionFee(1_000_000_000)
                .execute(client);

        final ConsensusTopicId topicId = transactionId.getReceipt(client).getConsensusTopicId();

        new MirrorConsensusTopicQuery()
                .setTopicId(topicId)
                .subscribe(mirrorClient, resp -> {
                            String messageAsString = new String(resp.message, StandardCharsets.UTF_8);

                            System.out.println(resp.consensusTimestamp + " received topic message: " + messageAsString);
                        },
                        // On gRPC error, print the stack trace
                        Throwable::printStackTrace);

        // keep the main thread from exiting because the listeners run on daemon threads
        // noinspection InfiniteLoopStatement
        for (int i = 0; ; i++) {
            new ConsensusMessageSubmitTransaction()
                    .setTopicId(topicId)
                    .setMessage("hello, HCS! " + i)
                    .execute(client)
                    .getReceipt(client);

            Thread.sleep(2500);
        }
    }
}
