import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
class DynamoEntity {
    @get:DynamoDbPartitionKey
    var palavra: String? = null

    override fun toString(): String {
        return "DynamoEntity(palavra=$palavra)"
    }
}