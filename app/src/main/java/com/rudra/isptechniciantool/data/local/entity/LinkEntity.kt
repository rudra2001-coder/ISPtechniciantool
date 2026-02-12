package com.rudra.isptechniciantool.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rudra.isptechniciantool.domain.model.Link
import com.rudra.isptechniciantool.domain.model.LinkStatus
import com.rudra.isptechniciantool.domain.model.LinkType

/**
 * Room entity representing a connection between two network devices.
 */
@Entity(
    tableName = "links",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["id"],
            childColumns = ["source_device_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["id"],
            childColumns = ["target_device_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["source_device_id"]),
        Index(value = ["target_device_id"])
    ]
)
data class LinkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "source_device_id")
    val sourceDeviceId: Long,
    
    @ColumnInfo(name = "target_device_id")
    val targetDeviceId: Long,
    
    @ColumnInfo(name = "link_type")
    val linkType: String = LinkType.FIBER.name,
    
    @ColumnInfo(name = "bandwidth")
    val bandwidth: String? = null,
    
    @ColumnInfo(name = "status")
    val status: String = LinkStatus.ACTIVE.name,
    
    @ColumnInfo(name = "color")
    val color: String = "#4CAF50",
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): Link {
        return Link(
            id = id,
            sourceDeviceId = sourceDeviceId,
            targetDeviceId = targetDeviceId,
            linkType = LinkType.valueOf(linkType),
            bandwidth = bandwidth,
            status = LinkStatus.valueOf(status),
            color = color,
            createdAt = createdAt
        )
    }
    
    companion object {
        fun fromDomainModel(link: Link): LinkEntity {
            return LinkEntity(
                id = link.id,
                sourceDeviceId = link.sourceDeviceId,
                targetDeviceId = link.targetDeviceId,
                linkType = link.linkType.name,
                bandwidth = link.bandwidth,
                status = link.status.name,
                color = link.color,
                createdAt = link.createdAt
            )
        }
    }
}
