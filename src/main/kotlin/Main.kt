import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import io.netty.channel.nio.NioEventLoopGroup
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import software.amazon.awssdk.http.nio.netty.SdkEventLoopGroup
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import kotlin.coroutines.resume
import kotlin.random.Random
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

private val NRO_THREADS = System.getenv("NRO_THREADS").toInt()
private val MAX_CONCURRENCY = System.getenv("MAX_CONCURRENCY").toInt()
private val NRO_EXECUTIONS = System.getenv("NRO_EXECUTIONS").toInt()

private val dynamoAsyncClient = DynamoDbAsyncClient.builder()
    .region(Region.US_EAST_1)
    .credentialsProvider(DefaultCredentialsProvider.create())
    .httpClient(
        NettyNioAsyncHttpClient.builder()
            .maxConcurrency(MAX_CONCURRENCY)
            .eventLoopGroup(SdkEventLoopGroup.create(NioEventLoopGroup(NRO_THREADS)))
            .tcpKeepAlive(true)
            .build())
    .build()

private val dynamoEnhancedAsyncClient = DynamoDbEnhancedAsyncClient.builder()
    .dynamoDbClient(dynamoAsyncClient)
    .build()

private val dynamoTable = dynamoEnhancedAsyncClient.table("teste", TableSchema.fromBean(DynamoEntity::class.java))

class Main : RequestHandler<String, String> {
    override fun handleRequest(p0: String?, p1: Context?): String {
        val time = measureTimeMillis {
            runBlocking(Dispatchers.Unconfined) {
                repeat(NRO_EXECUTIONS) {
                    launch { getItem(it) }
                }
            }
        }
        log("Tempo total de execução foi $time ms")
        return ""
    }
}

fun main() {
    Main().handleRequest("", null)
}

private suspend fun getItem(iteration: Int): Unit {
    val startTime = System.currentTimeMillis()
    dynamoTable.getItem(Key.builder()
        .partitionValue("teste")
        .build())
    .await()
    val stopTime = System.currentTimeMillis()
    log("Executado em ${stopTime - startTime} ms", iteration)
}

private fun log(message: String, iteration: Int = -1) {
    println("[$iteration] $message")
}