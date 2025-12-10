import androidx.room.Embedded
import androidx.room.Relation
import com.gracechurch.gracefulgiving.data.local.entity.BatchEntity
import com.gracechurch.gracefulgiving.data.local.entity.DonationEntity

data class BatchWithDonations(
    @Embedded val batch: BatchEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "batchId"
    )
    val donations: List<DonationEntity>
)
