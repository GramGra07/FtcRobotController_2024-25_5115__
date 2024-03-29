package org.firstinspires.ftc.teamcode.subsystems

import CancelableFollowTrajectoryAction
import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.Pose2d
import com.arcrobotics.ftclib.command.CommandBase
import com.arcrobotics.ftclib.command.SubsystemBase
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.util.Range
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.UtilClass.FileWriterFTC
import org.firstinspires.ftc.teamcode.UtilClass.varStorage.varConfig
import org.firstinspires.ftc.teamcode.UtilClass.varStorage.varConfig.usingAvoidance
import org.firstinspires.ftc.teamcode.extensions.MotorExtensions
import org.firstinspires.ftc.teamcode.opModes.DistanceStorage
import org.firstinspires.ftc.teamcode.opModes.HardwareConfig
import org.firstinspires.ftc.teamcode.opModes.PoseStorage
import org.firstinspires.ftc.teamcode.rr.MecanumDrive
import org.firstinspires.ftc.teamcode.subsystems.avoidance.AvoidanceSubsystem
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt


@Config
class DriveSubsystem(ahwMap: HardwareMap) : SubsystemBase() {

    var drive: MecanumDrive

    private var motorFrontLeft: DcMotorEx
    private var motorBackLeft: DcMotorEx
    private var motorFrontRight: DcMotorEx
    private var motorBackRight: DcMotorEx

    //    var odometrySubsystem: OdometrySubsystem3Wheel? = null
    var avoidanceSubsystem: AvoidanceSubsystem = AvoidanceSubsystem()
    lateinit var cancelableFollowing: CancelableFollowTrajectoryAction

    init {
//        if (odometrySubsystem == null) {
//            odometrySubsystem = OdometrySubsystem3Wheel(ahwMap, 0.0, 0.0, 0.0)
//        }

        drive = MecanumDrive(ahwMap, Pose2d(0.0, 0.0, 0.0))

        motorFrontLeft =
            MotorExtensions.initMotor(
                ahwMap,
                "motorFrontLeft",
                DcMotor.RunMode.RUN_WITHOUT_ENCODER
            )
        motorBackLeft = MotorExtensions.initMotor(
            ahwMap,
            "motorBackLeft",
            DcMotor.RunMode.RUN_WITHOUT_ENCODER,
        )
        motorBackLeft.direction = DcMotorSimple.Direction.REVERSE
        motorFrontRight =
            MotorExtensions.initMotor(
                ahwMap,
                "motorFrontRight",
                DcMotor.RunMode.RUN_WITHOUT_ENCODER
            )
        motorBackRight =
            MotorExtensions.initMotor(
                ahwMap,
                "motorBackRight",
                DcMotor.RunMode.RUN_WITHOUT_ENCODER
            )
        reset()
    }

    private var thisDist = 0.0
    private var slowMult: Int = varConfig.slowMult
    private var frontRightPower = 0.0
    private var frontLeftPower = 0.0
    private var backRightPower = 0.0
    private var backLeftPower = 0.0
    var slowModeIsOn = false
    var reverse = false
    var isAutoInTeleop = false
    var goZero: Action? = null
    fun driveByGamepads(fieldCentric: Boolean, myOpMode: OpMode) {
        // Retrieve gamepad values
        val leftStickX = myOpMode.gamepad1.left_stick_x.toDouble()
        val leftStickY = -myOpMode.gamepad1.left_stick_y.toDouble()
        val rightStickX = -myOpMode.gamepad1.right_stick_x.toDouble()

        // Determine slow power based on slowModeIsOn
        val slowPower = if (slowModeIsOn) slowMult else 1

        if (fieldCentric) {
            // Compute controller angle
            val controllerAngle = Math.toDegrees(atan2(leftStickY, leftStickX))
            val robotDegree = Math.toDegrees(drive.pose.heading.toDouble())
            val movementDegree = controllerAngle - robotDegree
            val gamepadHypot = Range.clip(hypot(leftStickX, leftStickY), 0.0, 1.0)

            // Compute x and y controls
            val xControl = cos(Math.toRadians(movementDegree)) * gamepadHypot
            val yControl = sin(Math.toRadians(movementDegree)) * gamepadHypot

            // Compute powers
            val turn = rightStickX
            frontRightPower =
                (yControl * abs(yControl) - xControl * abs(xControl) + turn) / slowPower
            backRightPower =
                (yControl * abs(yControl) + xControl * abs(xControl) + turn) / slowPower
            frontLeftPower =
                (yControl * abs(yControl) + xControl * abs(xControl) - turn) / slowPower
            backLeftPower = (yControl * abs(yControl) - xControl * abs(xControl) - turn) / slowPower
        } else {
            // Compute powers for non-field centric mode
            val turn = rightStickX
            frontRightPower =
                (leftStickY * abs(leftStickY) - leftStickX * abs(leftStickX) + turn) / slowPower
            backRightPower =
                (leftStickY * abs(leftStickY) + leftStickX * abs(leftStickX) + turn) / slowPower
            frontLeftPower =
                (leftStickY * abs(leftStickY) + leftStickX * abs(leftStickX) - turn) / slowPower
            backLeftPower =
                (leftStickY * abs(leftStickY) - leftStickX * abs(leftStickX) - turn) / slowPower
        }

        // Update pose estimate
        drive.updatePoseEstimate()

        // Update distance traveled
        updateDistTraveled(PoseStorage.currentPose, drive.pose)
        FileWriterFTC.writeToFile(
            HardwareConfig.fileWriter,
            drive.pose.position.x.toInt(),
            drive.pose.position.y.toInt()
        )
        PoseStorage.currentPose = drive.pose
    }


    private fun updateDistTraveled(before: Pose2d, after: Pose2d) {
        // Calculate the change in position along x and y axes
        val deltaX = after.position.x - before.position.x
        val deltaY = after.position.y - before.position.y

        // Calculate the distance traveled using Euclidean distance formula
        val dist = sqrt(deltaX * deltaX + deltaY * deltaY)

        // Update the distance traveled
        thisDist += dist
        DistanceStorage.totalDist += dist
    }


    fun stop() {
        motorFrontLeft.power = 0.0
        motorBackLeft.power = 0.0
        motorFrontRight.power = 0.0
        motorBackRight.power = 0.0
    }

    fun reset() {
        thisDist = 0.0
    }

    fun resetHeading() {
        drive.pose = Pose2d(drive.pose.position.x, drive.pose.position.y, 0.0)
    }

    private fun power() {
        if (!isAutoInTeleop) {
            val addedPowers: Map<String, Double?>? = avoidanceSubsystem.powers
            var flP = addedPowers?.getOrDefault("FL", 0.0) ?: 0.0
            var frP = addedPowers?.getOrDefault("FR", 0.0) ?: 0.0
            var rlP = addedPowers?.getOrDefault("RL", 0.0) ?: 0.0
            var rrP = addedPowers?.getOrDefault("RR", 0.0) ?: 0.0

            if (!usingAvoidance) {
                flP = 0.0
                frP = 0.0
                rlP = 0.0
                rrP = 0.0
            }

            frontLeftPower = Range.clip(frontLeftPower + flP, -1.0, 1.0)
            frontRightPower = Range.clip(frontRightPower + frP, -1.0, 1.0)
            backLeftPower = Range.clip(backLeftPower + rlP, -1.0, 1.0)
            backRightPower = Range.clip(backRightPower + rrP, -1.0, 1.0)

            motorFrontLeft.power = frontLeftPower
            motorBackLeft.power = backLeftPower
            motorFrontRight.power = frontRightPower
            motorBackRight.power = backRightPower
        }
    }


    private fun update() {
        power()
        drive.updatePoseEstimate()
        avoidanceSubsystem.update(drive)
//        odometrySubsystem!!.update()
    }

    class driveDefault : CommandBase() {
        private fun update() {
            this.update()
        }

        override fun execute() {
            update()
        }
    }

    fun telemetry(telemetry: Telemetry) {
        if (reverse) {
            telemetry.addData("reversed", "")
        }
        if (slowModeIsOn) {
            telemetry.addData("slowMode", "")
        }
        telemetry.addData("thisDistance (in)", "%.1f", thisDist)
        telemetry.addData("totalDistance (in)", "%.1f", DistanceStorage.totalDist)
        telemetry.addData("addPowers", avoidanceSubsystem.powers)
//        odometrySubsystem!!.telemetry(telemetry)
    }
}