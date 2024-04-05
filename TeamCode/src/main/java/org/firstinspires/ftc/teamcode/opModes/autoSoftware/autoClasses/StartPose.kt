package org.firstinspires.ftc.teamcode.opModes.autoSoftware.autoClasses

import com.acmerobotics.roadrunner.Pose2d
import org.firstinspires.ftc.teamcode.startEnums.Alliance
import org.firstinspires.ftc.teamcode.startEnums.StartSide
import org.firstinspires.ftc.teamcode.storage.PoseStorage

data class StartPose(
    var startLocation: StartLocation
) {
    var pose2d: Pose2d
    private var location: StartLocation = startLocation

    init {
        this.pose2d = getStartPose(location)
        PoseStorage.currentPose = pose2d
    }

    fun getPose(): Pose2d {
        return getStartPose(location)
    }

    private fun getStartPose(startLocation: StartLocation): Pose2d {
        val spot = when (startLocation.alliance) {
            Alliance.BLUE -> {
                when (startLocation.startSide) {
                    StartSide.LEFT -> Pose2d(12.0, -63.0, Math.toRadians(90.0))
                    StartSide.RIGHT -> Pose2d(12.0, -63.0, Math.toRadians(90.0))
                }
            }

            Alliance.RED -> {
                when (startLocation.startSide) {
                    StartSide.LEFT -> Pose2d(-12.0, -63.0, Math.toRadians(90.0))
                    StartSide.RIGHT -> Pose2d(-12.0, -63.0, Math.toRadians(90.0))
                }
            }
        }
        PoseStorage.currentPose = spot
        return spot
    }
}